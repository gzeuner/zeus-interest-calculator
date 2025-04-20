/*
 * Zeus Interest Calculator – InterestController
 * ---------------------------------------------
 * Hauptcontroller für die Benutzerinteraktion im Zinsrechner.
 * Steuert Formularanzeige, Berechnung, Validierung und Ergebnisanzeige.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */
package de.zeus.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zeus.interest.dto.PaymentPlanRequest;
import de.zeus.interest.dto.PaymentPlanResponse;
import de.zeus.interest.model.CalculationMode;
import de.zeus.interest.model.PaymentPlanElement;
import de.zeus.interest.service.CalculationService;
import de.zeus.interest.service.DepositCalculationService;
import de.zeus.interest.service.LoanCalculationService;
import de.zeus.interest.service.PaymentPlanStorageService;
import de.zeus.interest.util.InterestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller zur Steuerung des Zinsrechners (Formularanzeige, Berechnung, Sondertilgungen, Import/Export).
 * Verwendet das Strategy-Pattern (Loan/DepositService) und unterstützt Upload/Download im JSON-Format.
 */
@Controller
@RequestMapping("/interest")
@RequiredArgsConstructor
public class InterestController {

    private final LoanCalculationService loanService;
    private final DepositCalculationService depositService;
    private final MessageSource messageSource;
    private final PaymentPlanStorageService storageService;

    /* -------------------------------------------------- Init‑Binder -------------------------------------------------- */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Double‑Editor
        PropertyEditorSupport doubleEditor = new PropertyEditorSupport() {
            @Override public void setAsText(String text) {
                setValue((text == null || text.isBlank()) ? null
                        : Double.parseDouble(text.trim().replace(',', '.')));
            }
        };
        binder.registerCustomEditor(Double.class, doubleEditor);
        binder.registerCustomEditor(double.class, doubleEditor);

        // LocalDate‑Editor
        PropertyEditorSupport dateEditor = new PropertyEditorSupport() {
            @Override public void setAsText(String text) {
                setValue((text == null || text.isBlank()) ? null : LocalDate.parse(text));
            }
        };
        binder.registerCustomEditor(LocalDate.class, dateEditor);
    }

    /* -------------------------------------------------- Formular anzeigen -------------------------------------------------- */
    @GetMapping
    public String showForm(Model model, HttpServletRequest request,
                           @RequestParam(value = "lang", required = false) String lang) {

        HttpSession session = request.getSession();
        PaymentPlanRequest dto = (PaymentPlanRequest) session.getAttribute("origRequest");
        if (dto == null) { dto = new PaymentPlanRequest(); dto.setExtraPayments(new HashMap<>()); }
        session.setAttribute("origRequest", dto);

        if (lang != null && ("de".equals(lang) || "en".equals(lang))) {
            session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale(lang));
        }

        model.addAttribute("paymentRequest", dto);
        model.addAttribute("currentPath", request.getRequestURI());
        Object err = session.getAttribute("errorMessage");
        if (err != null) { model.addAttribute("errorMessage", err); session.removeAttribute("errorMessage"); }
        return "interest/form";
    }

    /* -------------------------------------------------- Formular absenden -------------------------------------------------- */
    @PostMapping
    public String handleForm(@ModelAttribute @Valid PaymentPlanRequest request,
                             BindingResult result,
                             HttpServletRequest httpRequest,
                             @RequestParam(value = "lang", required = false) String lang,
                             Model model) {

        HttpSession session = httpRequest.getSession();
        session.setAttribute("origRequest", request);  // immer merken

        if (lang != null && ("de".equals(lang) || "en".equals(lang))) {
            session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale(lang));
            model.addAttribute("paymentRequest", request);
            model.addAttribute("currentPath", httpRequest.getRequestURI());
            return "interest/form";
        }

        if (result.hasErrors()) {
            model.addAttribute("paymentRequest", request);
            model.addAttribute("currentPath", httpRequest.getRequestURI());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("validation.errors", null, Locale.getDefault()));
            return "interest/form";
        }

        try {
            validateRequest(request);
            List<PaymentPlanResponse> results = doCalculation(request);
            session.setAttribute("results", results);
            session.setAttribute("firstDate", results.isEmpty() ? "" : results.get(0).getRepaymentDate());
            session.setAttribute("monthlyRate", String.format("%.2f", request.getPaymentAmount()));
            return "redirect:/interest/result";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("paymentRequest", request);
            model.addAttribute("currentPath", httpRequest.getRequestURI());
            model.addAttribute("errorMessage", ex.getMessage());
            return "interest/form";
        }
    }

    /* -------------------------------------------------- Ergebnis‑Seite -------------------------------------------------- */
    @GetMapping("/result")
    public String showResultPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/interest";

        @SuppressWarnings("unchecked")
        List<PaymentPlanResponse> results = (List<PaymentPlanResponse>) session.getAttribute("results");
        if (results == null) return "redirect:/interest";

        PaymentPlanRequest orig = (PaymentPlanRequest) session.getAttribute("origRequest");
        if (orig.getExtraPayments() == null) orig.setExtraPayments(new HashMap<>());

        model.addAttribute("paymentRequest", orig);
        model.addAttribute("groupedResults", groupByYear(results));
        model.addAttribute("firstDate", session.getAttribute("firstDate"));
        model.addAttribute("monthlyRate", session.getAttribute("monthlyRate"));
        model.addAttribute("currentPath", request.getRequestURI());

        Object err = session.getAttribute("errorMessage");
        if (err != null) { model.addAttribute("errorMessage", err); session.removeAttribute("errorMessage"); }
        return "interest/result";
    }

    /* -------------------------------------------------- Sondertilgungen anwenden -------------------------------------------------- */
    @PostMapping("/result")
    public String applyExtras(@ModelAttribute("paymentRequest") @Valid PaymentPlanRequest req,
                              BindingResult result,
                              HttpServletRequest httpRequest,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (req.getExtraPayments() == null) req.setExtraPayments(new HashMap<>());
        HttpSession session = httpRequest.getSession(true); // ← wichtig!

        session.setAttribute("origRequest", req);

        if (result.hasErrors()) {
            return reRender(req, (List<PaymentPlanResponse>) session.getAttribute("results"),
                    httpRequest, model,
                    messageSource.getMessage("validation.errors", null, Locale.getDefault()));
        }

        try {
            validateRequest(req);
            validateExtraPayments(req);

            List<PaymentPlanResponse> results = doCalculation(req);

            session.setAttribute("results", results); // ✅ aktualisierter Plan
            session.setAttribute("firstDate", results.isEmpty() ? "" : results.get(0).getRepaymentDate());
            session.setAttribute("monthlyRate", String.format("%.2f", req.getPaymentAmount()));

            // ✅ Erfolgsmeldung übergeben – survives redirect
            redirectAttributes.addFlashAttribute("successMessage", "Sondertilgungen angewendet. Du kannst den neuen Plan speichern.");

            return "redirect:/interest/result";

        } catch (IllegalArgumentException ex) {
            return reRender(req, (List<PaymentPlanResponse>) session.getAttribute("results"),
                    httpRequest, model, ex.getMessage());
        }
    }

    /* -------------------------------------------------- Validierungen -------------------------------------------------- */
    private void validateRequest(PaymentPlanRequest request) {
        if (request.getInterestRate() < 0 || request.getInterestRate() > 100) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("validation.interestRate.range", null, Locale.getDefault()));
        }
        if (request.getContractDate() == null || request.getFirstPaymentDate() == null) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("validation.dates.required", null, Locale.getDefault()));
        }
    }

    /** prüft, dass jede Sondertilgung <= Restschuld vor Abzug ist */
    private void validateExtraPayments(PaymentPlanRequest req) {
        if (req.getMode() != CalculationMode.LOAN || req.getExtraPayments().isEmpty()) return;

        // Summe für Fehlermeldung
        double totalExtra = req.getExtraPayments().values().stream()
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        if (totalExtra == 0) return;

        // Basisplan ohne Extras
        Map<Integer, Double> backup = new HashMap<>(req.getExtraPayments());
        req.setExtraPayments(Collections.emptyMap());
        List<PaymentPlanResponse> base = doCalculation(req);
        req.setExtraPayments(backup);

        Map<Integer, Double> debtByRun = base.stream()
                .collect(Collectors.toMap(r -> Integer.parseInt(r.getRunNumber()),
                        r -> Double.parseDouble(r.getFutureValue().replace(",", "."))));

        for (var e : backup.entrySet()) {
            double extra = e.getValue() == null ? 0 : e.getValue();
            if (extra > debtByRun.getOrDefault(e.getKey(), Double.MAX_VALUE)) {
                throw new IllegalArgumentException(messageSource.getMessage(
                        "validation.extraPayments.tooHigh",
                        new Object[]{
                                String.format("%.2f", totalExtra),
                                String.format("%.2f", debtByRun.getOrDefault(e.getKey(), 0.0))
                        },
                        Locale.getDefault()));
            }
        }
    }

    /* ---------- SPEICHERN  (Download) ---------- */
    @PostMapping("/save")
    public ResponseEntity<Resource> downloadPlan(HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null) return ResponseEntity.badRequest().build();

        // Ursprüngliche Anfrage wiederherstellen
        PaymentPlanRequest orig = (PaymentPlanRequest) session.getAttribute("origRequest");
        @SuppressWarnings("unchecked")
        List<PaymentPlanResponse> currentResults =
                (List<PaymentPlanResponse>) session.getAttribute("results");

        // Falls Sondertilgungen gesetzt sind → Plan neu berechnen
        List<PaymentPlanResponse> results;
        if (orig != null && orig.getExtraPayments() != null && !orig.getExtraPayments().isEmpty()) {
            results = doCalculation(orig);
            session.setAttribute("results", results); // neuen Plan auch in der Session aktualisieren
        } else {
            results = currentResults;
        }

        if (results == null || results.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileId = storageService.save(results);
        byte[] data   = storageService.loadRaw(fileId);

        ByteArrayResource res = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"payment-plan-" + fileId + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(data.length)
                .body(res);
    }

    /* ---------- LADEN  (Upload) ---------- */
    @PostMapping("/load")
    public String uploadPlan(@RequestParam("planFile") MultipartFile file,
                             HttpServletRequest request,
                             Model model) {

        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Keine Datei ausgewählt.");
            return showResultPage(request, model);
        }
        try {
            PaymentPlanResponse[] arr =
                    new ObjectMapper().readValue(file.getInputStream(), PaymentPlanResponse[].class);

            request.getSession().setAttribute("results", List.of(arr));
            model.addAttribute("successMessage", "Plan erfolgreich geladen.");
        } catch (IOException ex) {
            model.addAttribute("errorMessage", "Datei konnte nicht gelesen werden.");
        }
        return showResultPage(request, model);
    }

    /* -------------------------------------------------- Helfer‑Methoden -------------------------------------------------- */
    private String errorSave(Model model) {
        model.addAttribute("errorMessage", "Kein Zahlungsplan zum Speichern vorhanden.");
        return "interest/result";
    }

    private String reRender(PaymentPlanRequest req,
                            List<PaymentPlanResponse> current,
                            HttpServletRequest httpRequest,
                            Model model,
                            String error) {
        model.addAttribute("paymentRequest", req);
        model.addAttribute("groupedResults", groupByYear(current));
        model.addAttribute("firstDate", httpRequest.getSession().getAttribute("firstDate"));
        model.addAttribute("monthlyRate", httpRequest.getSession().getAttribute("monthlyRate"));
        model.addAttribute("currentPath", httpRequest.getRequestURI());
        model.addAttribute("errorMessage", error);
        return "interest/result";
    }

    private List<PaymentPlanResponse> doCalculation(PaymentPlanRequest req) {
        CalculationService svc = req.getMode() == CalculationMode.LOAN ? loanService : depositService;

        List<PaymentPlanResponse> list = new ArrayList<>();
        PaymentPlanElement plan = createInitialPlan(req);
        Map<Integer, Double> extras = Optional.ofNullable(req.getExtraPayments())
                .orElse(Collections.emptyMap());

        for (int month = 1; month <= req.getPaymentMonths(); month++) {

            if (!plan.isFirstRun()) plan.setTimeInDays(30);

            svc.calculate(plan);

            // manuelle Zinsen (nur im 1. Monat)
            if (month == 1 && req.getManualFirstInterest() != null) {
                double delta = req.getManualFirstInterest() - plan.getInterestAmount();
                plan.setInterestAmount(req.getManualFirstInterest());
                if (req.getMode() == CalculationMode.DEPOSIT) {
                    plan.setFutureValue(plan.getFutureValue() + delta);
                    plan.setAmountChangeValue(plan.getAmountChangeValue() + delta);
                } else {
                    plan.setFutureValue(plan.getFutureValue() - delta);
                    plan.setAmountChangeValue(plan.getAmountChangeValue() + delta);
                }
            }

            plan.setFirstRun(false);

            // Sondertilgung
            double extra = Optional.ofNullable(extras.get(month)).orElse(0.0);
            if (extra != 0.0) {
                if (req.getMode() == CalculationMode.DEPOSIT) {
                    plan.setFutureValue(plan.getFutureValue() + extra);
                    plan.setAmountChangeValue(plan.getAmountChangeValue() + extra);
                } else {
                    plan.setFutureValue(plan.getFutureValue() - extra);
                    plan.setAmountChangeValue(plan.getAmountChangeValue() + extra);
                }
            }

            // Kredit-spezifischer Abbruch
            if (req.getMode() == CalculationMode.LOAN) {
                if (plan.getFutureValue() < 0) {
                    double corrected = plan.getRegularPaymentAmount() + plan.getFutureValue();
                    plan.setRegularPaymentAmount(Math.max(0, corrected));
                    svc.calculate(plan);
                    list.add(mapToResponse(plan, extra)); // ← extra mitgeben
                    break;
                }
                if (plan.getInitialValue() < plan.getRegularPaymentAmount()) {
                    plan.setRegularPaymentAmount(plan.getInitialValue());
                    svc.calculate(plan);
                    list.add(mapToResponse(plan, extra)); // ← extra mitgeben
                    break;
                }
            }

            list.add(mapToResponse(plan, extra)); // ← extra mitgeben
            plan = plan.copyNextRun();
        }
        return list;
    }

    private PaymentPlanElement createInitialPlan(PaymentPlanRequest req) {
        PaymentPlanElement plan = new PaymentPlanElement();
        plan.setInitialValue(req.getInitialValue());
        plan.setInterestRate(req.getInterestRate());
        plan.setRegularPaymentAmount(req.getPaymentAmount());
        plan.setRepaymentDate(req.getFirstPaymentDate());
        plan.setTimeInDays((int) ChronoUnit.DAYS.between(req.getContractDate(), req.getFirstPaymentDate()));
        plan.setFirstRun(true);
        plan.setTotalRuns(req.getPaymentMonths());

        if (req.getManualFirstInterest() == null) {
            BigDecimal proRata = InterestUtils.calculateProRataInterest(
                    req.getInitialValue(), req.getInterestRate(),
                    req.getContractDate(), req.getFirstPaymentDate());
            plan.setInterestAmount(proRata.doubleValue());
        }
        return plan;
    }

    private PaymentPlanResponse mapToResponse(PaymentPlanElement e, double extraPayment) {
        Locale loc = Locale.getDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(loc);

        PaymentPlanResponse r = new PaymentPlanResponse();
        r.setInitialValue(String.format("%.2f", e.getInitialValue()));
        r.setInterestAmount(String.format("%.2f", e.getInterestAmount()));

        double change = Math.abs(Math.abs(e.getAmountChangeValue()) < 0.005 ? 0 : e.getAmountChangeValue());
        r.setAmountChangeValue(String.format("%.2f", change));

        double future = Math.abs(Math.abs(e.getFutureValue()) < 0.005 ? 0 : e.getFutureValue());
        r.setFutureValue(String.format("%.2f", future));

        r.setRegularPaymentAmount(String.format("%.2f", e.getRegularPaymentAmount()));
        r.setRunNumber(String.valueOf(e.getRunNumber()));
        r.setTotalRuns(String.valueOf(e.getTotalRuns()));
        r.setTimeInDays(String.valueOf(e.getTimeInDays()));
        r.setRepaymentDate(e.getRepaymentDate().format(fmt));
        r.setIsGroup(String.valueOf(e.isLastDayOfYear()));
        r.setIsLastRun(String.valueOf(e.isLastRun()));
        r.setYear(e.getRepaymentDate().getYear());

        // 🟢 Sonderzahlung explizit hinzufügen
        r.setExtraPayment(String.format("%.2f", extraPayment));

        return r;
    }


    private Map<Integer, List<PaymentPlanResponse>> groupByYear(List<PaymentPlanResponse> list) {
        return list.stream()
                .collect(Collectors.groupingBy(PaymentPlanResponse::getYear,
                        LinkedHashMap::new, Collectors.toList()));
    }
}

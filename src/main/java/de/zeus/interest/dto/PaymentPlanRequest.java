/*
 * Zeus Interest Calculator – PaymentPlanRequest
 * ---------------------------------------------
 * Eingabedaten für die Zinsberechnung eines Zahlungsplans.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.dto;

import de.zeus.interest.model.CalculationMode;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO für die Eingabe eines Zins- oder Tilgungsplans.
 * Wird über ein Formular aus dem Frontend befüllt.
 */
@Data
public class PaymentPlanRequest {

    /** Optional: Manueller Zinswert für Monat 1 (z.B. bei pro-rata-Anpassung) */
    private Double manualFirstInterest;

    /** Kapitalbetrag zum Start (Kreditsumme oder Sparbetrag) */
    private double initialValue;

    /** Zinssatz pro Jahr in Prozent (z.B. 4.5 für 4,5%) */
    private double interestRate;

    /** Regelmäßige monatliche Zahlung (z.B. Rate oder Einzahlung) */
    private double paymentAmount;

    /** Optionale Begrenzung der Laufzeit in Monaten (z.B. 24 Monate) */
    private Integer paymentMonths;

    /** Vertragsdatum (Start der Zinsberechnung), Standard = heute */
    private LocalDate contractDate = LocalDate.now();

    /** Datum der ersten Abbuchung / Einzahlung, Standard = heute */
    private LocalDate firstPaymentDate = LocalDate.now();

    /** Berechnungsmodus: Kredit (LOAN) oder Einlage (DEPOSIT), Standard = Kredit */
    private CalculationMode mode = CalculationMode.LOAN;

    /**
     * Sonderzahlungen – Mapping: Periode (Monatsnummer) → Zusätzlicher Betrag.
     * Beispiel: {1 → 100.0, 3 → 50.0}
     */
    private Map<Integer, Double> extraPayments = new HashMap<>();
}

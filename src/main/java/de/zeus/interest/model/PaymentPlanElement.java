/*
 * Zeus Interest Calculator – PaymentPlanElement
 * ---------------------------------------------
 * Datenmodell für eine einzelne Periode im Zahlungsplan.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.model;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Repräsentiert ein einzelnes Element (Monat) im Zahlungsplan – für Einlagen oder Kredite.
 * Enthält Zinsen, Rate, Veränderung und Kapitalstände.
 */
@Data
public class PaymentPlanElement {

    private CalculationMode mode;

    private double initialValue;
    private double futureValue;

    private double interestRate;
    private double interestAmount;

    private double regularPaymentAmount;
    private double amountChangeValue;

    private LocalDate repaymentDate;
    private int runNumber = 1;
    private int totalRuns;
    private int timeInDays;

    /**
     * Gibt an, ob es sich um den ersten Planungsmonat handelt.
     * Wird für anteilige Zinsberechnung benötigt (z. B. 17 Tage statt 30).
     */
    private boolean firstRun = true;

    /**
     * Gibt zurück, ob der aktuelle Monat der letzte im Jahr ist (31.12.).
     *
     * @return true, wenn Datum der 31.12., sonst false
     */
    public boolean isLastDayOfYear() {
        return repaymentDate != null
                && repaymentDate.getMonthValue() == 12
                && repaymentDate.getDayOfMonth() == 31;
    }

    /**
     * Prüft, ob dies die letzte Periode des Plans ist.
     * Dies ist der Fall, wenn entweder:
     * - die gesamte Schuld getilgt ist (Veränderung = Startwert), oder
     * - die Laufzeit erreicht ist (letzter Durchlauf).
     *
     * @return true, wenn letzte Periode erreicht
     */
    public boolean isLastRun() {
        return amountChangeValue == initialValue
                || runNumber == totalRuns;
    }

    /**
     * Erstellt eine Kopie dieses Elements für den nächsten Monat.
     * Dabei wird das Kapital fortgeschrieben, der Zinssatz übernommen und
     * Standardwerte gesetzt (z. B. 30 Tage Laufzeit).
     *
     * @return Neues Planungsobjekt für nächste Periode
     */
    public PaymentPlanElement copyNextRun() {
        PaymentPlanElement copy = new PaymentPlanElement();
        copy.setMode(this.mode);
        copy.setInitialValue(this.futureValue);
        copy.setInterestRate(this.interestRate);
        copy.setRegularPaymentAmount(this.regularPaymentAmount);
        copy.setRunNumber(this.runNumber + 1);
        // Ab dem zweiten Lauf immer 30 Tage
        copy.setTimeInDays(30);
        copy.setFirstRun(false);
        copy.setTotalRuns(this.totalRuns);
        copy.setRepaymentDate(this.repaymentDate.plusMonths(1));
        return copy;
    }

    /**
     * Setzt den Veränderungswert (Tilgung oder Zuwachs).
     * Rundet dabei auf 2 Nachkommastellen und begrenzt bei Krediten
     * die maximale Veränderung auf den aktuellen Kapitalwert.
     *
     * @param changeValue Veränderung des Kapitals in dieser Periode
     */
    public void setAmountChangeValue(double changeValue) {
        double rounded = BigDecimal.valueOf(changeValue)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        if (mode == CalculationMode.LOAN && rounded > initialValue) {
            this.amountChangeValue = initialValue;
        } else {
            this.amountChangeValue = rounded;
        }
    }
}

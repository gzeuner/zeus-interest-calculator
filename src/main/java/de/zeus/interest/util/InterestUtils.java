/*
 * Zeus Interest Calculator – Utility zur Zinsberechnung
 * -----------------------------------------------------
 * Berechnung nach der 30/360-Methode (banküblicher Standard).
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Utility-Klasse zur Berechnung von Zinsen nach 30/360-Methode (banküblich).
 */
public final class InterestUtils {

    private InterestUtils() {
        // Utility class – kein Konstruktor erlaubt
    }

    /**
     * Berechnet Zinsen zwischen zwei Daten nach der 30/360-Methode.
     * Diese Methode wird typischerweise für den ersten Monat verwendet,
     * wenn Vertragsdatum und erste Zahlung auseinanderliegen.
     *
     * @param capital     Kapitalbetrag (z. B. Kreditsumme)
     * @param annualRate  Zinssatz pro Jahr (z. B. 5.63 für 5,63 %)
     * @param startDate   Vertragsbeginn
     * @param endDate     Erste Abbuchung / Zinsstichtag
     * @return Berechneter Zinsbetrag, kaufmännisch gerundet auf 2 Nachkommastellen
     */
    public static BigDecimal calculateProRataInterest(double capital, double annualRate, LocalDate startDate, LocalDate endDate) {
        int days = calculateDays30_360(startDate, endDate);
        return BigDecimal.valueOf(capital)
                .multiply(BigDecimal.valueOf(annualRate))
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(36000), 5, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Zählt die Anzahl der Tage zwischen zwei Daten nach der 30/360-Methode.
     * Dabei zählen alle Monate als 30 Tage, und Tage über 30/31 werden auf 30 gekappt.
     *
     * @param start Startdatum (z. B. Vertragsbeginn)
     * @param end   Enddatum (z. B. erste Abbuchung)
     * @return Anzahl Tage im 30/360-Modell
     */
    public static int calculateDays30_360(LocalDate start, LocalDate end) {
        int d1 = Math.min(start.getDayOfMonth(), 30);
        int d2 = end.getDayOfMonth() == 31 ? 30 : end.getDayOfMonth();

        return (end.getYear() - start.getYear()) * 360
                + (end.getMonthValue() - start.getMonthValue()) * 30
                + (d2 - d1);
    }
}

/*
 * Zeus Interest Calculator – CalculationService
 * ---------------------------------------------
 * Interface zur Definition von Zinsberechnungsstrategien.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.service;

import de.zeus.interest.model.PaymentPlanElement;

/**
 * Schnittstelle für verschiedene Berechnungstypen (z. B. Kredit, Einlage).
 * Ermöglicht eine einheitliche Verarbeitung von Zahlungsplänen.
 */
public interface CalculationService {

    /**
     * Führt die vollständige Berechnung für eine Periode durch.
     *
     * @param value Das zu berechnende Plan-Element
     */
    void calculate(PaymentPlanElement value);

    /**
     * Berechnet die anfallenden Zinsen für eine Periode.
     *
     * @param initialValue Kapitalbetrag (z. B. Restschuld)
     * @param interestRate Zinssatz pro Jahr (in Prozent)
     * @param timeInDays   Dauer der Periode in Tagen
     * @return Zinsbetrag
     */
    double calculateInterestAmount(double initialValue, double interestRate, int timeInDays);

    /**
     * Berechnet die Veränderung des Kapitals durch Zahlung und Zinsen.
     *
     * @param paymentAmount   Gesamtzahlung (z. B. Monatsrate)
     * @param interestAmount  Anteil Zinsen
     * @return Veränderung des Kapitals (positiv oder negativ)
     */
    double calculateAmountChange(double paymentAmount, double interestAmount);

    /**
     * Berechnet den neuen Kapitalwert nach Anwendung der Kapitalveränderung.
     *
     * @param initialValue Ursprünglicher Kapitalwert
     * @param repayment     Veränderungsbetrag (z. B. Tilgung oder Zuwachs)
     * @return Neuer Kapitalwert
     */
    double calculateFutureAmount(double initialValue, double repayment);
}

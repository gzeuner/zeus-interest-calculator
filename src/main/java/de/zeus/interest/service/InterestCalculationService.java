/*
 * Zeus Interest Calculator – InterestCalculationService
 * -----------------------------------------------------
 * Abstrakte Basisklasse für Zinsberechnungen.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.service;

/**
 * Abstrakte Basisimplementierung für Berechnungs-Services mit Zinslogik.
 * Nutzt die vereinfachte 30/360-Methode zur Zinsberechnung.
 */
public abstract class InterestCalculationService implements CalculationService {

    /**
     * Berechnet den Zinsbetrag für einen Zeitraum in Tagen nach der 30/360-Methode.
     *
     * @param initialValue Kapitalbetrag (Startwert)
     * @param interestRate Zinssatz pro Jahr in Prozent (z. B. 5.25 für 5,25 %)
     * @param timeInDays   Anzahl Tage für die Zinsberechnung
     * @return Zinsbetrag als double
     */
    @Override
    public double calculateInterestAmount(double initialValue, double interestRate, int timeInDays) {
        return (initialValue * interestRate * timeInDays) / (100 * 360.0);
    }
}

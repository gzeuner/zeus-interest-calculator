/*
 * Zeus Interest Calculator – CalculationMode
 * ------------------------------------------
 * Enum zur Auswahl der Berechnungsart (Kredit oder Einlage).
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.model;

/**
 * Berechnungsmodus zur Auswahl zwischen Kredit- und Einlagenberechnung.
 */
public enum CalculationMode {
    /** Kredit-Modus (Zahlung reduziert Kapital) */
    LOAN,

    /** Einlage-Modus (Zahlung erhöht Kapital) */
    DEPOSIT
}

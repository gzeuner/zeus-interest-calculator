/*
 * Zeus Interest Calculator – PaymentPlanResponse
 * ----------------------------------------------
 * Ausgabe-Datensatz für eine einzelne Zeile im Zahlungsplan (zur Darstellung im UI).
 *
 * © 2025 Guido Zeuner  (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO für die Darstellung eines Monats im Zahlungsplan.
 * Alle Werte sind als formatierte Strings vorbereitet (z. B. "1.000,00 €").
 */
@Data
public class PaymentPlanResponse {

    /** Jahr, dem dieser Monat zugeordnet ist (z. B. 2025) */
    private int year;

    /** Startwert (formatiert als String, z. B. "1.000,00 €") */
    private String initialValue;

    /** Kapital am Ende der Periode */
    private String futureValue;

    /** Zinsen in dieser Periode */
    private String interestAmount;

    /** Regelmäßige Zahlung (Rate oder Einzahlung) */
    private String regularPaymentAmount;

    /** Veränderung (Tilgung oder Zuwachs) */
    private String amountChangeValue;

    /** Laufnummer der Periode (z. B. "1", "2", …) */
    private String runNumber;

    /** Gesamtzahl der Perioden */
    private String totalRuns;

    /** Anzahl Tage in dieser Periode */
    private String timeInDays;

    /** Startdatum als formatiertes Datum */
    private String startDate;

    /** Datum der Zahlung / Abbuchung */
    private String repaymentDate;

    /** Kennzeichen, ob dieser Eintrag ein Gruppenheader (Jahreszeile) ist */
    private String isGroup;

    /** Kennzeichen, ob dies die letzte Periode ist */
    private String isLastRun;

    /** Sondertilgung in dieser Periode (z.B. "0,00" oder "3.000,00") */
    private String extraPayment;
}

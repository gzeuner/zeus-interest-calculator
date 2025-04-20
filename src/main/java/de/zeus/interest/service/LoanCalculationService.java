/*
 * Zeus Interest Calculator – LoanCalculationService
 * --------------------------------------------------
 * Berechnet den Verlauf eines Kreditplans mit Tilgung und Zinsen.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.service;

import de.zeus.interest.model.PaymentPlanElement;
import org.springframework.stereotype.Service;

/**
 * Service zur Berechnung von Kreditraten (Annuitätendarlehen).
 * Verwendet Zinsberechnung nach Tagen und ermittelt monatliche Tilgung + Restschuld.
 */
@Service
public class LoanCalculationService extends InterestCalculationService {

    /**
     * Führt die Zins- und Tilgungsberechnung für eine einzelne Periode durch.
     *
     * @param value Plan-Element mit Eingabewerten (Startwert, Zinssatz, Zahlung etc.)
     */
    @Override
    public void calculate(PaymentPlanElement value) {
        double interest = calculateInterestAmount(value.getInitialValue(), value.getInterestRate(), value.getTimeInDays());
        double change = calculateAmountChange(value.getRegularPaymentAmount(), interest);
        value.setInterestAmount(interest);
        value.setAmountChangeValue(change);
        value.setFutureValue(calculateFutureAmount(value.getInitialValue(), value.getAmountChangeValue()));
    }

    /**
     * Berechnet die Veränderung des Kapitals in dieser Periode.
     *
     * @param paymentAmount     Zahlung für diese Periode
     * @param interestAmount    angefallene Zinsen
     * @return Tilgungsanteil (Zahlung minus Zinsen)
     */
    @Override
    public double calculateAmountChange(double paymentAmount, double interestAmount) {
        return paymentAmount - interestAmount;
    }

    /**
     * Berechnet den neuen Kapitalstand nach Abzug der Tilgung.
     *
     * @param initialValue Startkapital zu Beginn der Periode
     * @param repayment    Tilgungsanteil der Zahlung
     * @return verbleibender Kapitalbetrag (Restschuld)
     */
    @Override
    public double calculateFutureAmount(double initialValue, double repayment) {
        return initialValue - repayment;
    }
}

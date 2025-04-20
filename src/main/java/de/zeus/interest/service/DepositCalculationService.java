/*
 * Zeus Interest Calculator – DepositCalculationService
 * -----------------------------------------------------
 * Berechnet den Verlauf eines Sparplans mit Zinsen und Einzahlungen.
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
 * Service zur Berechnung von Einlagen (z. B. Sparplänen).
 * Führt Zinsen und Einzahlung als Kapitalzuwachs zusammen.
 */
@Service
public class DepositCalculationService extends InterestCalculationService {

    /**
     * Berechnet Zinsen und Zuwachs für eine einzelne Periode.
     *
     * @param value Plan-Element mit Eingabewerten
     */
    @Override
    public void calculate(PaymentPlanElement value) {
        double interest = calculateInterestAmount(value.getInitialValue(), value.getInterestRate(), value.getTimeInDays());
        double change = calculateAmountChange(value.getRegularPaymentAmount(), interest);
        value.setInterestAmount(interest);
        value.setAmountChangeValue(change);
        value.setFutureValue(calculateFutureAmount(value.getInitialValue(), change));
    }

    /**
     * Berechnet die Kapitalveränderung (Zinsen + Einzahlung).
     *
     * @param paymentAmount     Einzahlung für diese Periode
     * @param interestAmount    Zinsen für diese Periode
     * @return Kapitalzuwachs
     */
    @Override
    public double calculateAmountChange(double paymentAmount, double interestAmount) {
        return paymentAmount + interestAmount;
    }

    /**
     * Berechnet den neuen Kapitalstand.
     *
     * @param initialValue Startwert der Periode
     * @param changeAmount Kapitalzuwachs (Zinsen + Einzahlung)
     * @return Neuer Kapitalstand
     */
    @Override
    public double calculateFutureAmount(double initialValue, double changeAmount) {
        return initialValue + changeAmount;
    }
}

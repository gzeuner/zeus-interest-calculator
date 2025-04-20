/*
 * Zeus Interest Calculator
 * -------------------------
 * Ein einfacher Spring Boot Starter für den Zinsrechner.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hauptklasse des Zinsrechner-Projekts.
 *
 * Startet die Spring Boot Anwendung.
 */
@SpringBootApplication
public class InterestCalculatorApplication {

    /**
     * Main-Methode – Einstiegspunkt der Anwendung.
     *
     * @param args Kommandozeilenargumente (nicht genutzt)
     */
    public static void main(String[] args) {
        SpringApplication.run(InterestCalculatorApplication.class, args);
    }
}

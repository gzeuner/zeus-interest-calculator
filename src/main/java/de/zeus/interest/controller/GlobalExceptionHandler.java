/*
 * Zeus Interest Calculator – GlobalExceptionHandler
 * --------------------------------------------------
 * Zentrale Fehlerbehandlung für Laufzeitfehler (IllegalArgumentException).
 * Leitet Benutzer bei Fehlern elegant zurück zur vorherigen Ansicht.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Globaler Fehler-Handler für bekannte Ausnahmen (z. B. Validierungsfehler).
 * Leitet zurück zur Ursprungsseite und zeigt eine freundliche Fehlermeldung an.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Fängt ungültige Argumente ab (z. B. bei validierter Benutzereingabe) und
     * leitet zurück zur vorherigen Seite. Die Fehlermeldung wird in der Session gespeichert.
     *
     * @param ex       Die ausgelöste Ausnahme
     * @param request  Das zugehörige HTTP-Request-Objekt
     * @return Redirect-URL zur vorherigen Seite (Formular oder Ergebnis)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("errorMessage", ex.getMessage());

        String referer = request.getHeader("referer");
        boolean isResult = referer != null && referer.contains("/interest/result");

        return "redirect:" + (isResult ? "/interest/result" : "/interest");
    }
}

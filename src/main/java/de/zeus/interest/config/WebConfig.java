/*
 * Zeus Interest Calculator – WebConfig
 * -------------------------------------
 * Konfiguriert Internationalisierung (i18n) für Spracheinstellungen per URL-Parameter.
 *
 * © 2025 Zeus (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Locale;

/**
 * Spring Web MVC Konfiguration für Internationalisierung (i18n).
 * Aktiviert Sprachwechsel per URL-Parameter ?lang=de|en.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Legt den Locale-Resolver auf Session-Basis fest.
     * Standard-Sprache: Deutsch.
     *
     * @return LocaleResolver-Bean
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.GERMAN);
        return slr;
    }

    /**
     * Interceptor zur Erkennung des URL-Parameters "lang".
     *
     * @return LocaleChangeInterceptor-Bean
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Registriert den Sprach-Interceptor im Web-MVC-Kontext.
     *
     * @param registry InterceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

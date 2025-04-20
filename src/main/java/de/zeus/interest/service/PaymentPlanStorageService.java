/*
 * Zeus Interest Calculator – PaymentPlanStorageService
 * -----------------------------------------------------
 * Speichert und lädt Zahlungspläne als JSON-Dateien.
 *
 * © 2025 Guido Zeuner (https://tiny-tool.de)
 *
 * Lizenz: Apache License, Version 2.0
 * Siehe LICENSE-Datei oder https://www.apache.org/licenses/LICENSE-2.0
 */

package de.zeus.interest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zeus.interest.dto.PaymentPlanResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service zur Speicherung und Wiederherstellung von Zahlungsplänen.
 * Die Pläne werden als JSON-Dateien im konfigurierbaren Verzeichnis gespeichert.
 */
@Service
public class PaymentPlanStorageService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path storageDir;

    /**
     * Konstruktor, der das Speicherverzeichnis initialisiert (Default: "payment-plans").
     *
     * @param storageDir Speicherpfad (kann via `paymentplan.storage.dir` gesetzt werden)
     */
    public PaymentPlanStorageService(
            @Value("${paymentplan.storage.dir:payment-plans}") String storageDir) {
        this.storageDir = Paths.get(storageDir);
        try {
            // Verzeichnis bei Bedarf anlegen
            Files.createDirectories(this.storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    /**
     * Speichert einen Zahlungsplan als JSON-Datei und gibt eine eindeutige File-ID zurück.
     *
     * @param plan Liste mit Monatsraten etc.
     * @return generierte File-ID (Timestamp + UUID)
     */
    public String save(List<PaymentPlanResponse> plan) {
        String fileId = Instant.now().toEpochMilli() + "-" + UUID.randomUUID();
        Path file = storageDir.resolve(fileId + ".json");
        try {
            mapper.writeValue(file.toFile(), plan);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payment plan", e);
        }
        return fileId;
    }

    /**
     * Lädt einen zuvor gespeicherten Zahlungsplan anhand der File-ID.
     *
     * @param fileId ID der Datei ohne Endung
     * @return Liste der gespeicherten Monatsdaten
     */
    public List<PaymentPlanResponse> load(String fileId) {
        Path file = storageDir.resolve(fileId + ".json");
        if (!Files.exists(file))
            throw new RuntimeException("Payment plan file not found: " + fileId);
        try {
            PaymentPlanResponse[] arr = mapper.readValue(file.toFile(), PaymentPlanResponse[].class);
            return List.of(arr);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load payment plan", e);
        }
    }

    /**
     * Gibt den rohen JSON-Inhalt einer Datei zurück (für Downloads).
     *
     * @param fileId ID der Datei
     * @return Byte-Array mit JSON-Inhalt
     * @throws IOException bei Zugriffsfehlern
     */
    public byte[] loadRaw(String fileId) throws IOException {
        return Files.readAllBytes(storageDir.resolve(fileId + ".json"));
    }
}

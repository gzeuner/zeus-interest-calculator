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
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service zur Speicherung und Wiederherstellung von Zahlungsplänen.
 * Die Pläne werden als JSON-Dateien im konfigurierbaren Verzeichnis gespeichert.
 */
@Service
public class PaymentPlanStorageService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path storageDir;

    public PaymentPlanStorageService(
            @Value("${paymentplan.storage.dir:payment-plans}") String storageDir) {
        this.storageDir = Paths.get(storageDir);
        try {
            Files.createDirectories(this.storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

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

    public byte[] loadRaw(String fileId) throws IOException {
        return Files.readAllBytes(storageDir.resolve(fileId + ".json"));
    }

    /**
     * Löscht einen gespeicherten Plan.
     *
     * @param fileId Datei-ID ohne Endung
     * @return true, wenn erfolgreich gelöscht
     */
    public boolean delete(String fileId) {
        try {
            return Files.deleteIfExists(storageDir.resolve(fileId + ".json"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete payment plan", e);
        }
    }

    /**
     * Gibt eine Liste aller gespeicherten File-IDs zurück.
     */
    public List<String> listAll() {
        try (Stream<Path> stream = Files.list(storageDir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .sorted(Comparator.reverseOrder()) // neueste zuerst
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list payment plans", e);
        }
    }

    public void saveTo(String fileId, List<PaymentPlanResponse> plan) {
        Path file = storageDir.resolve(fileId + ".json");
        try {
            mapper.writeValue(file.toFile(), plan);
        } catch (IOException e) {
            throw new RuntimeException("Failed to overwrite payment plan", e);
        }
    }

    // Optional: Metadaten speichern
    public void saveMeta(String fileId, Map<String, String> meta) {
        Path metaFile = storageDir.resolve(fileId + ".meta.json");
        try {
            mapper.writeValue(metaFile.toFile(), meta);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save metadata", e);
        }
    }

    public Map<String, String> loadMeta(String fileId) {
        Path metaFile = storageDir.resolve(fileId + ".meta.json");
        try {
            return mapper.readValue(metaFile.toFile(), Map.class);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

}

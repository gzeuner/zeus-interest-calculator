package de.zeus.interest.api;

import de.zeus.interest.service.PaymentPlanStorageService;
import de.zeus.interest.dto.PaymentPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PaymentPlanApiController {

    private final PaymentPlanStorageService storageService;

    @GetMapping
    public ResponseEntity<List<String>> listAllPlans() {
        return ResponseEntity.ok(storageService.listAll());
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<List<PaymentPlanResponse>> loadPlan(@PathVariable String fileId) {
        try {
            List<PaymentPlanResponse> plan = storageService.load(fileId);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{fileId}/raw")
    public ResponseEntity<ByteArrayResource> downloadRaw(@PathVariable String fileId) {
        try {
            byte[] raw = storageService.loadRaw(fileId);
            ByteArrayResource res = new ByteArrayResource(raw);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plan-" + fileId + ".json\"")
                    .contentLength(raw.length)
                    .body(res);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deletePlan(@PathVariable String fileId) {
        boolean deleted = storageService.delete(fileId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}

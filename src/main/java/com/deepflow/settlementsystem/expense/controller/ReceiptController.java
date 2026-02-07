package com.deepflow.settlementsystem.expense.controller;

import com.deepflow.settlementsystem.expense.dto.OcrStatusResponse;
import com.deepflow.settlementsystem.expense.dto.ReceiptUploadResponse;
import com.deepflow.settlementsystem.expense.entity.Receipt;
import com.deepflow.settlementsystem.expense.repository.ReceiptRepository;
import com.deepflow.settlementsystem.expense.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ReceiptRepository receiptRepository;

    @PostMapping
    public ResponseEntity<ReceiptUploadResponse> uploadReceipt(
            @RequestParam("image") MultipartFile image
    ) {
        Long receiptId = receiptService.uploadReceipt(image);
        return ResponseEntity.ok(new ReceiptUploadResponse(receiptId));
    }

    @GetMapping("/{receiptId}/status")
    public ResponseEntity<OcrStatusResponse> getOcrStatus(@PathVariable Long receiptId) {
        Receipt receipt = findReceipt(receiptId);
        return ResponseEntity.ok(new OcrStatusResponse(receipt.getOcrStatus()));
    }

    @GetMapping("/{receiptId}/analysis")
    public ResponseEntity<String> getOcrAnalysis(@PathVariable Long receiptId) {
        Receipt receipt = findReceipt(receiptId);
        if (!"SUCCESS".equalsIgnoreCase(receipt.getOcrStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "OCR is not successful");
        }
        if (receipt.getOcrResult() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OCR result not found");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(receipt.getOcrResult());
    }

    private Receipt findReceipt(Long receiptId) {
        return receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found"));
    }
}

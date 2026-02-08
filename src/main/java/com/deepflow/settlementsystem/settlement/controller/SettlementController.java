package com.deepflow.settlementsystem.settlement.controller;

import com.deepflow.settlementsystem.settlement.dto.request.SettlementSendRequest;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementResponse;
import com.deepflow.settlementsystem.settlement.service.SettlementService;
import com.deepflow.settlementsystem.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Settlement", description = "정산 관련 API")
@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @Operation(summary = "정산 목록 조회", description = "사용자와 관련된 정산 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<SettlementListResponse> getSettlementList(
            @AuthenticationPrincipal @NotNull User user) {
        SettlementListResponse response = settlementService.getSettlementList(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "정산 상태 조회", description = "특정 정산의 상태를 조회합니다.")
    @GetMapping("/{allocationId}")
    public ResponseEntity<SettlementResponse> getSettlementStatus(
            @PathVariable Long allocationId,
            @AuthenticationPrincipal @NotNull User user) {
        SettlementResponse response = settlementService.getSettlementStatus(allocationId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "정산 요청 메시지 전송", description = "카카오톡으로 정산 요청 메시지를 전송합니다. (돈을 받는 사람이 요청)")
    @PostMapping("/send")
    public ResponseEntity<Void> sendSettlementMessage(
            @Valid @RequestBody SettlementSendRequest request,
            @AuthenticationPrincipal @NotNull User receiver) {
        settlementService.sendSettlementMessage(
                request.getAllocationId(),
                receiver.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "정산 완료 처리", description = "정산을 완료 처리합니다. (돈을 받는 사람이 완료 처리)")
    @PatchMapping("/{allocationId}/complete")
    public ResponseEntity<Void> completeSettlement(
            @PathVariable Long allocationId,
            @AuthenticationPrincipal @NotNull User receiver) {
        settlementService.completeSettlement(allocationId, receiver.getId());
        return ResponseEntity.noContent().build();
    }
}

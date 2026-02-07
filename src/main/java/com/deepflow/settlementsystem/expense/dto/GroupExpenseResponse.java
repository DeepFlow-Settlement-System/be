package com.deepflow.settlementsystem.expense.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupExpenseResponse {

    private Long groupId;
    private List<ExpenseResponse> expenses;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseResponse {
        private Long expenseId;
        private String title;
        private LocalDateTime spentAt;
        private Integer totalAmount;
        private String settlementType;
        private Long payerUserId;
        private Long receiptId;
        private List<ParticipantResponse> participants;
        private List<ExpenseItemResponse> items;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantResponse {
        private Long userId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseItemResponse {
        private Long itemId;
        private String itemName;
        private Integer lineAmount;
        private List<ItemParticipantResponse> participants;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemParticipantResponse {
        private Long userId;
    }
}

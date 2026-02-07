package com.deepflow.settlementsystem.expense.dto;

import com.deepflow.settlementsystem.expense.entity.SettlementType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class CreateExpenseRequest {

    private String title; // 가게명
    private LocalDateTime spentAt; // 지출등록일
    private Long payerUserId; // 결제자

    private Long receiptImageId = null; // 영수증 사진 id(선택)
    private SettlementType settlementType; // 정산 타입 (n빵 OR 품목별)

    private List<Item> items; // 품목 리스트
    private List<Participant> participants; // 결제 참여자

    private String totalAmount; // n빵인 경우 총가격

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Participant {
        private Long userId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
      private String itemName;    // 품목명
      private String price;      // 항목 가격
      private List<Participant> itemParticipants; // 각 품목의 결제 참여자
    }
}

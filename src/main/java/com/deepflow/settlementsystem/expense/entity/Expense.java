package com.deepflow.settlementsystem.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "spent_at")
    private LocalDateTime spentAt; // 지출일

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "total_amount")
    private Integer totalAmount; // 영수증 총합 가격

    @Lob
    @Column(name = "receipt_image")
    private byte[] receiptImage; // 영수증 사진 원본

    @Column(name = "ocr_status", length = 20)
    private String ocrStatus = "FAILED"; // OCR 진행상태

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "expense")
    private List<ExpenseItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "expense")
    private List<Participant> participants = new ArrayList<>();
}

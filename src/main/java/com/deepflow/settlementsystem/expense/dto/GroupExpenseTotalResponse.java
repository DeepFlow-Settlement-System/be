package com.deepflow.settlementsystem.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupExpenseTotalResponse {

    private Long groupId;
    private Long totalAmount;
}

package com.deepflow.settlementsystem.expense.controller;

import com.deepflow.settlementsystem.expense.dto.CreateExpenseRequest;
import com.deepflow.settlementsystem.expense.dto.CreateExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseResponse;
import com.deepflow.settlementsystem.expense.dto.GroupExpenseTotalResponse;
import com.deepflow.settlementsystem.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExpenseController {

  private final ExpenseService expenseService;

  @PostMapping("/groups/{groupId}/expenses")
  public ResponseEntity<CreateExpenseResponse> createExpense( // 지출 등록
          @PathVariable Long groupId,
          @RequestBody CreateExpenseRequest request
  ) {
      CreateExpenseResponse response = expenseService.createExpense(groupId, request);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/expenses/{groupId}/total") // 그룹별 총 지출 금액
  public ResponseEntity<GroupExpenseTotalResponse> getGroupTotal(@PathVariable Long groupId) {
      return ResponseEntity.ok(expenseService.getGroupTotal(groupId));
  }

  // 그룹 지출내역 조회
  @GetMapping("/groups/{groupId}/expenses")
  public ResponseEntity<GroupExpenseResponse> getExpenses(@PathVariable Long groupId){
    return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
  }


}

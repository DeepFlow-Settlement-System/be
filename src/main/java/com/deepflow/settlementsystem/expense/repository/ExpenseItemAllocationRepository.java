package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.ExpenseItemAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseItemAllocationRepository extends JpaRepository<ExpenseItemAllocation, Long> {
}

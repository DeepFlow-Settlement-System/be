package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByGroup_GroupId(Long groupId);
}

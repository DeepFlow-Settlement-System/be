package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}

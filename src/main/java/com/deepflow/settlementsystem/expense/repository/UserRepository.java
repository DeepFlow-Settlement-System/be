package com.deepflow.settlementsystem.expense.repository;

import com.deepflow.settlementsystem.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

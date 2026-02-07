package com.deepflow.settlementsystem.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "user")
    private List<ExpenseParticipant> expenseParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "sender") // 돈을 보낼 내역들
    private List<ExpenseItemAllocation> sendList = new ArrayList<>();

    @OneToMany(mappedBy = "receiver") // 돈을 받을 내역들
    private List<ExpenseItemAllocation> receiveList = new ArrayList<>();

    @OneToMany(mappedBy = "payerUser")
    private List<Expense> paidExpenses = new ArrayList<>();


    @OneToMany(mappedBy = "user")
    private List<ExpenseItemsParticipant> itemsParticipants = new ArrayList<>();

}

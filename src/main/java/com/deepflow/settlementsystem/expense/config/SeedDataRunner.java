package com.deepflow.settlementsystem.expense.config;

import com.deepflow.settlementsystem.expense.entity.Group;
import com.deepflow.settlementsystem.expense.entity.User;
import com.deepflow.settlementsystem.expense.repository.GroupRepository;
import com.deepflow.settlementsystem.expense.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
// 부트 실행 시, H2 user, group 초기 데이터 저장
public class SeedDataRunner implements ApplicationRunner {

    private static final int DEFAULT_USER_COUNT = 5;

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        groupRepository.save(new Group());

        List<User> users = new ArrayList<>();
        for (long i = 0; i < DEFAULT_USER_COUNT; i++) {
            users.add(new User());
        }
        userRepository.saveAll(users);

    }
}

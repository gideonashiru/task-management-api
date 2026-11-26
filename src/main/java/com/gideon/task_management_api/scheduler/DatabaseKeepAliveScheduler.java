package com.gideon.task_management_api.scheduler;

import com.gideon.task_management_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseKeepAliveScheduler {

    private final UserRepository userRepository;

    @Scheduled(fixedRate = 600000)
    public void keepDbWarm() {
        userRepository.count();
    }
}

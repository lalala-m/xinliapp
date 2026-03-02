package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.UserOnlineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserOnlineStatusRepository extends JpaRepository<UserOnlineStatus, Long> {
    Optional<UserOnlineStatus> findByUserId(Long userId);
    Optional<UserOnlineStatus> findBySocketId(String socketId);
}

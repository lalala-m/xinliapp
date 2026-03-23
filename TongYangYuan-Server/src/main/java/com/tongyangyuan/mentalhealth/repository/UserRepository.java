package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    long countByUserType(User.UserType userType);
    long countByStatus(User.UserStatus status);
    List<User> findByUserType(User.UserType userType);
    Optional<User> findByWxOpenId(String wxOpenId);
    Optional<User> findByWxUnionId(String wxUnionId);
}

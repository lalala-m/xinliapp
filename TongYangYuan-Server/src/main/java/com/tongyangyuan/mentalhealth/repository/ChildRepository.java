package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByParentUserId(Long parentUserId);
    void deleteByParentUserId(Long parentUserId);
}

package com.example.tongyangyuan.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tongyangyuan.database.entity.ConsultantEntity;

import java.util.List;

@Dao
public interface ConsultantDao {
    @Query("SELECT * FROM consultants")
    List<ConsultantEntity> getAllConsultants();

    @Query("SELECT * FROM consultants WHERE name = :name LIMIT 1")
    ConsultantEntity getConsultantByName(String name);

    @Query("SELECT * FROM consultants WHERE serverId = :serverId LIMIT 1")
    ConsultantEntity getConsultantByServerId(long serverId);

    @Query("SELECT * FROM consultants WHERE userId = :userId LIMIT 1")
    ConsultantEntity getConsultantByUserId(long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConsultant(ConsultantEntity consultant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConsultants(List<ConsultantEntity> consultants);

    @Query("DELETE FROM consultants")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM consultants")
    int getCount();
}

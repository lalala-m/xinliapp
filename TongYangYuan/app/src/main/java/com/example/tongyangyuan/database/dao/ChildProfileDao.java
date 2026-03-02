package com.example.tongyangyuan.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tongyangyuan.database.entity.ChildProfileEntity;

import java.util.List;

@Dao
public interface ChildProfileDao {
    @Query("SELECT * FROM child_profiles WHERE user_phone = :userPhone")
    List<ChildProfileEntity> getChildProfilesByUser(String userPhone);

    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    ChildProfileEntity getChildProfileById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChildProfile(ChildProfileEntity profile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChildProfiles(List<ChildProfileEntity> profiles);

    @Update
    void updateChildProfile(ChildProfileEntity profile);

    @Query("DELETE FROM child_profiles WHERE id = :id")
    void deleteChildProfile(String id);

    @Query("DELETE FROM child_profiles WHERE user_phone = :userPhone")
    void deleteByUser(String userPhone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChildProfileEntity profile);

    @Query("DELETE FROM child_profiles WHERE user_phone = :userPhone")
    void deleteChildProfilesByUser(String userPhone);

    @Query("DELETE FROM child_profiles")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM child_profiles WHERE user_phone = :userPhone")
    int getCountByUser(String userPhone);
}

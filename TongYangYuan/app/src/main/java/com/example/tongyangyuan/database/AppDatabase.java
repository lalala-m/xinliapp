package com.example.tongyangyuan.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.tongyangyuan.database.converter.StringListConverter;
import com.example.tongyangyuan.database.dao.ChildProfileDao;
import com.example.tongyangyuan.database.dao.ConsultantDao;
import com.example.tongyangyuan.database.entity.ChildProfileEntity;
import com.example.tongyangyuan.database.entity.ConsultantEntity;

@Database(entities = {ConsultantEntity.class, ChildProfileEntity.class}, version = 3, exportSchema = false)
@TypeConverters({StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ConsultantDao consultantDao();
    public abstract ChildProfileDao childProfileDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "tongyuan_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

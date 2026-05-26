package com.example.tongyangyuan.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.tongyangyuan.database.converter.StringListConverter;
import com.example.tongyangyuan.database.dao.ChildProfileDao;
import com.example.tongyangyuan.database.dao.ConsultantDao;
import com.example.tongyangyuan.database.entity.ChildProfileEntity;
import com.example.tongyangyuan.database.entity.ConsultantEntity;

@Database(entities = {ConsultantEntity.class, ChildProfileEntity.class}, version = 4, exportSchema = false)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration from version 1 to 2: add serverId column to consultants
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE consultants ADD COLUMN serverId INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration from version 2 to 3: add child_profiles table
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS child_profiles ("
                    + "id TEXT PRIMARY KEY NOT NULL,"
                    + "user_phone TEXT,"
                    + "name TEXT,"
                    + "gender TEXT,"
                    + "birth_date TEXT,"
                    + "ethnicity TEXT,"
                    + "native_place TEXT,"
                    + "family_rank TEXT,"
                    + "birth_place TEXT,"
                    + "language_env TEXT,"
                    + "school TEXT,"
                    + "home_address TEXT,"
                    + "interests TEXT,"
                    + "activities TEXT,"
                    + "body_status TEXT,"
                    + "body_status_detail TEXT,"
                    + "medical_history TEXT,"
                    + "medical_history_other TEXT,"
                    + "father_phone TEXT,"
                    + "mother_phone TEXT,"
                    + "guardian_phone TEXT)");
        }
    };

    // Migration from version 3 to 4: add stages column to consultants
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE consultants ADD COLUMN stages TEXT");
        }
    };
}

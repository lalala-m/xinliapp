package com.example.tongyangyuan.consult;

import android.content.Context;

import com.example.tongyangyuan.database.AppDatabase;
import com.example.tongyangyuan.database.dao.ConsultantDao;
import com.example.tongyangyuan.database.entity.ConsultantEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConsultantRepository {

    private static ConsultantRepository instance;
    private final Context context;
    private final ExecutorService executorService;
    private List<Consultant> cachedConsultants;

    private ConsultantRepository(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.cachedConsultants = new ArrayList<>();
    }

    public static synchronized ConsultantRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ConsultantRepository(context);
        }
        return instance;
    }

    public interface ConsultantCallback {
        void onSuccess(List<Consultant> consultants);
        void onError(Exception e);
    }

    public interface SingleConsultantCallback {
        void onSuccess(Consultant consultant);
        void onError(Exception e);
    }

    public void getAll(ConsultantCallback callback) {
        if (!cachedConsultants.isEmpty()) {
            callback.onSuccess(cachedConsultants);
            return;
        }

        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ConsultantDao dao = db.consultantDao();
                List<ConsultantEntity> entities = dao.getAllConsultants();

                List<Consultant> consultants = new ArrayList<>();
                for (ConsultantEntity entity : entities) {
                    consultants.add(entityToConsultant(entity));
                }

                consultants.sort(Comparator
                        .comparingInt((Consultant c) -> c.getIdentityTier().getPriority())
                        .thenComparing((Consultant c) -> -c.getRating()));

                cachedConsultants = Collections.unmodifiableList(consultants);
                callback.onSuccess(cachedConsultants);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public List<Consultant> getAllSync() {
        if (!cachedConsultants.isEmpty()) {
            return cachedConsultants;
        }
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            ConsultantDao dao = db.consultantDao();
            List<ConsultantEntity> entities = dao.getAllConsultants();

            List<Consultant> consultants = new ArrayList<>();
            for (ConsultantEntity entity : entities) {
                consultants.add(entityToConsultant(entity));
            }

            consultants.sort(Comparator
                    .comparingInt((Consultant c) -> c.getIdentityTier().getPriority())
                    .thenComparing((Consultant c) -> -c.getRating()));

            cachedConsultants = Collections.unmodifiableList(consultants);
            return cachedConsultants;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void findByName(String name, SingleConsultantCallback callback) {
        if (name == null) {
            callback.onSuccess(null);
            return;
        }

        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ConsultantDao dao = db.consultantDao();
                ConsultantEntity entity = dao.getConsultantByName(name);

                if (entity != null) {
                    callback.onSuccess(entityToConsultant(entity));
                } else {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public Consultant findByNameSync(String name) {
        if (name == null) {
            return null;
        }
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            ConsultantDao dao = db.consultantDao();
            ConsultantEntity entity = dao.getConsultantByName(name);
            return entity != null ? entityToConsultant(entity) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Consultant getFallback(String name) {
        // 如果没有找到咨询师，不再返回硬编码的默认咨询师
        // 而是返回null，让调用者处理（例如显示空状态）
        return null;
    }

    private Consultant entityToConsultant(ConsultantEntity entity) {
        return new Consultant(
                entity.getUserId(),
                entity.getName(),
                entity.getTitle(),
                entity.getSpecialty(),
                entity.getRating(),
                entity.getServedCount(),
                entity.getAvatarColor(),
                entity.getIdentityTags() != null ? entity.getIdentityTags() : new ArrayList<>(),
                entity.getIntro(),
                entity.getReviews() != null ? entity.getReviews() : new ArrayList<>()
        );
    }

    public void clearCache() {
        cachedConsultants = new ArrayList<>();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

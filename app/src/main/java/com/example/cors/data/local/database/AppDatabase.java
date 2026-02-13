package com.example.cors.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.cors.data.local.dao.CourseDao;
import com.example.cors.data.local.entity.CourseEntity;

/**
   Класс базы данных Room - точка входа для работы с локальным хранилищем.
 */
@Database(entities = {CourseEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    /**
     * Абстрактный метод для получения DAO.
     * Room автоматически генерирует реализацию при компиляции.
     */
    public abstract CourseDao courseDao();
    
    /**
     * volatile гарантирует, что изменения instance видны во всех потокаш
     */
    private static volatile AppDatabase INSTANCE;
    
    /**
     * Имя файла базы данных на устройстве
     */
    private static final String DATABASE_NAME = "courses_database";
    
    /**
     * Получает экземпляр базы данных (Singleton pattern).
     */
    public static AppDatabase getInstance(Context context) {
        // Первая проверка без блокировки (быстрая)
        if (INSTANCE == null) {
            // Синхронизация - только один поток может войти
            synchronized (AppDatabase.class) {
                // Вторая проверка - другой поток мог уже создать instance
                if (INSTANCE == null) {
                    // Создаём базу данных через Room.databaseBuilder
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(), // Используем application context чтобы избежать утечек памяти
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    // fallbackToDestructiveMigration - при изменении версии БД пересоздаёт таблицы
                    // В production приложениях лучше использовать Migration
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Очищает singleton instance.
     * Используется в тестах для пересоздания чистой БД.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}

package com.example.cors.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cors.data.local.entity.CourseEntity;

import java.util.List;

/**
 * DAO интерфейс для работы с таблицей курсов.
 * Room автоматически генерирует реализацию этого интерфейса во время компиляции.
 * Все методы представляют собой SQL операции над таблицей courses.
 */
@Dao
public interface CourseDao {
    
    /**
     * Вставляет новый курс в базу данных.
     * OnConflictStrategy.REPLACE - если курс с таким ID уже существует, он будет перезаписан.
     * @param course Объект курса для вставки
     * @return ID вставленной записи
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCourse(CourseEntity course);
    
    /**
     * Вставляет список курсов в базу данных.
     * Используется для массовой загрузки данных (например, после получения с сервера).
     * 
     * @param courses Список курсов для вставки
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourses(List<CourseEntity> courses);
    
    /**
     * Обновляет существующий курс в базе данных.
     * Room автоматически находит курс по Primary Key (id) и обновляет все поля.
     * 
     * @param course Курс с обновленными данными
     */
    @Update
    void updateCourse(CourseEntity course);
    
    /**
     * Получает все курсы из базы данных.
     * LiveData - это observable wrapper, который автоматически уведомляет UI об изменениях.
     * Запрос выполняется в фоновом потоке, результат приходит в главный поток.
     * 
     * @return LiveData со списком всех курсов
     */
    @Query("SELECT * FROM courses ORDER BY title ASC")
    LiveData<List<CourseEntity>> getAllCourses();
    
    /**
     * Получает только избранные курсы.
     * Фильтрует курсы по полю isFavorite = 1 (true в SQLite).
     * Сортировка по названию для удобного отображения.
     * 
     * @return LiveData со списком избранных курсов
     */
    @Query("SELECT * FROM courses WHERE isFavorite = 1 ORDER BY title ASC")
    LiveData<List<CourseEntity>> getFavoriteCourses();
    
    /**
     * Ищет курсы по названию (поиск с учетом части строки).
     * LIKE '%' || :query || '%' - позволяет найти курсы, содержащие query в любой части названия.
     * COLLATE NOCASE - поиск без учета регистра (case-insensitive).
     * 
     * @param query Поисковый запрос
     * @return LiveData со списком найденных курсов
     */
    @Query("SELECT * FROM courses WHERE title LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY title ASC")
    LiveData<List<CourseEntity>> searchCourses(String query);
    
    /**
     * Фильтрует курсы по уровню сложности.
     * Используется для реализации фильтров через Material Chips.
     * 
     * @param level Уровень сложности ("Beginner", "Intermediate", "Advanced")
     * @return LiveData со списком курсов указанного уровня
     */
    @Query("SELECT * FROM courses WHERE level = :level ORDER BY title ASC")
    LiveData<List<CourseEntity>> getCoursesByLevel(String level);
    
    /**
     * Получает один курс по его ID.
     * Используется для отображения деталей курса.
     * 
     * @param courseId ID курса
     * @return LiveData с данными курса
     */
    @Query("SELECT * FROM courses WHERE id = :courseId")
    LiveData<CourseEntity> getCourseById(int courseId);
    
    /**
     * Обновляет статус избранного для курса.
     * Выполняет точечное обновление только одного поля вместо обновления всей записи.
     * 
     * @param courseId ID курса
     * @param isFavorite Новый статус избранного
     */
    @Query("UPDATE courses SET isFavorite = :isFavorite WHERE id = :courseId")
    void updateFavoriteStatus(int courseId, boolean isFavorite);
    
    /**
     * Обновляет комментарий и оценку пользователя для курса.
     * Используется на экране деталей курса.
     * 
     * @param courseId ID курса
     * @param comment Комментарий пользователя
     * @param rating Оценка пользователя
     */
    @Query("UPDATE courses SET comment = :comment, userRating = :rating WHERE id = :courseId")
    void updateCourseReview(int courseId, String comment, float rating);
    
    /**
     * Удаляет все курсы из базы данных.
     * Используется при полном обновлении данных с сервера.
     */
    @Query("DELETE FROM courses")
    void deleteAllCourses();
}

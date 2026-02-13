package com.example.cors.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.cors.data.repository.CourseRepository;
import com.example.cors.domain.model.Course;

/**
 * ViewModel для экрана деталей курса.
 * Управляет данными одного курса, сохранением комментариев и оценок.
 */
public class CourseDetailViewModel extends AndroidViewModel {
    
    /**
     * Repository для работы с данными
     */
    private final CourseRepository repository;
    
    /**
     * LiveData с данными курса.
     * Будет установлена после вызова loadCourse().
     */
    private LiveData<Course> courseLiveData;
    
    /**
     * ID текущего курса
     */
    private int currentCourseId;
    
    /**
     * Конструктор ViewModel.
     * 
     * @param application Application context для создания Repository
     */
    public CourseDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new CourseRepository(application);
    }
    

    public void loadCourse(int courseId) {
        this.currentCourseId = courseId;
        
        // Получаем LiveData с курсом из Repository
        // Room автоматически обновит данные при любых изменениях в БД
        courseLiveData = repository.getCourseById(courseId);
    }
    
    /**
     * Переключает статус избранного для текущего курса.
     * Вызывается при клике на кнопку "Добавить в избранное".
     */
    public void toggleFavorite(boolean isFavorite) {
        repository.updateFavoriteStatus(currentCourseId, isFavorite);
    }
    
    /**
     * Сохраняет комментарий и оценку пользователя.
     * Вызывается когда пользователь нажимает кнопку "Сохранить" после ввода комментария.
     */
    public void saveCourseReview(String comment, float rating) {
        // Валидация данных перед сохранением
        if (comment == null) {
            comment = ""; // Защита от null
        }
        
        // Ограничиваем рейтинг диапазоном 0-5
        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;
        
        // Сохраняем через Repository
        repository.saveCourseReview(currentCourseId, comment, rating);
    }
    
    /**
     * Геттер для LiveData с данными курса.
     */
    public LiveData<Course> getCourseLiveData() {
        return courseLiveData;
    }
    
    /**
     * Очистка ресурсов при уничтожении ViewModel.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}

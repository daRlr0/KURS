package com.example.cors.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.cors.data.Resource;
import com.example.cors.data.repository.CourseRepository;
import com.example.cors.domain.model.Course;

import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {
    
    /**
     * Repository для работы с данными
     */
    private final CourseRepository repository;
    
    /**
     * MediatorLiveData со списком избранных курсов обернутый в Resource.
     */
    private final MediatorLiveData<Resource<List<Course>>> favoritesLiveData = new MediatorLiveData<>();
    
    /**
     * Конструктор ViewModel.
     * Сразу загружает список избранных курсов из Room БД.
     */
    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        
        // Создаём Repository
        repository = new CourseRepository(application);
        
        // Инициализируем состояние загрузки
        favoritesLiveData.setValue(Resource.loading(null));
        
        // Подписываемся на изменения избранных курсов из Room
        observeFavorites();
    }
    

    private void observeFavorites() {
        // Получаем LiveData с избранными курсами из Repository
        LiveData<List<Course>> source = repository.getFavoriteCourses();
        
        // Подписываемся на изменения
        favoritesLiveData.addSource(source, courses -> {
            if (courses != null) {
                // Room всегда возвращает не-null список (может быть пустым)
                // Оборачиваем в Resource.success независимо от размера списка
                // UI сам решит показывать Empty State или RecyclerView
                favoritesLiveData.setValue(Resource.success(courses));
            } else {
                // Этот случай не должен происходить с Room, но на всякий случай
                favoritesLiveData.setValue(Resource.error("Ошибка загрузки избранного", null));
            }
        });
    }
    

    public void removeFromFavorites(Course course) {
        // Устанавливаем isFavorite = false через Repository
        // Repository выполнит это в фоновом потоке через ExecutorService
        repository.updateFavoriteStatus(course.getId(), false);
    }
    

    public LiveData<Resource<List<Course>>> getFavoritesLiveData() {
        return favoritesLiveData;
    }
    
    /**
     * Очистка ресурсов при уничтожении ViewModel.
     * Останавливает ExecutorService в Repository.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}

package com.example.cors.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cors.data.Resource;
import com.example.cors.data.repository.CourseRepository;
import com.example.cors.domain.model.Course;

import java.util.List;

/**
 * ViewModel для экрана списка курсов.
 * ViewModel - это часть MVVM паттерна
 */
public class CoursesViewModel extends AndroidViewModel {
    
    /**
     * Repository - источник данных
     */
    private final CourseRepository repository;

    private final MediatorLiveData<Resource<List<Course>>> coursesLiveData = new MediatorLiveData<>();
    
    /**
     * Текущий источник данных (источник, на который подписан MediatorLiveData).
     * Нужен для отписки от предыдущего источника при переключении.
     */
    private LiveData<List<Course>> currentSource;
    
    /**
     * LiveData с ошибками из Repository.
     * Храним ссылку чтобы не добавлять повторно в MediatorLiveData.
     */
    private LiveData<String> errorLiveDataSource;
    
    /**
     * LiveData с состоянием загрузки из Repository.
     * Храним ссылку чтобы не добавлять повторно в MediatorLiveData.
     */
    private LiveData<Boolean> loadingLiveDataSource;
    
    /**
     * Состояние поиска - текущий поисковый запрос.
     */
    private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>("");
    
    /**
     * Состояние фильтра - текущий выбранный уровень или null (все уровни).
     */
    private final MutableLiveData<String> selectedLevelLiveData = new MutableLiveData<>(null);
    
    /**
     * Конструктор ViewModel - инициализирует Repository и загружает данные.
     * 
     * Важно: Подписываемся на errorLiveData и loadingLiveData только один раз здесь,
     * чтобы избежать ошибки "This source was already added with the different observer".
     * 
     * @param application Application context для Repository
     */
    public CoursesViewModel(@NonNull Application application) {
        super(application);
        
        // Создаём Repository
        repository = new CourseRepository(application);
        
        // Подписываемся на ошибки и загрузку ОДИН РАЗ
        observeErrorState();
        observeLoadingState();
        
        // Загружаем все курсы при создании ViewModel
        loadAllCourses();
    }
    
    /**
     * Загружает все курсы без фильтров.
     * Устанавливает новый источник данных для MediatorLiveData.
     * 
     * Алгоритм:
     * 1. Устанавливаем состояние LOADING (показываем ProgressBar)
     * 2. Получаем LiveData из Repository
     * 3. Переключаем источник данных
     * 4. Repository автоматически обновит данные через LiveData
     */
    private void loadAllCourses() {
        // Показываем состояние загрузки
        coursesLiveData.setValue(Resource.loading(null));
        
        // Получаем LiveData со всеми курсами из Repository
        LiveData<List<Course>> source = repository.getAllCourses();
        
        // Переключаем источник данных
        switchSource(source);
    }
    

    public void searchCourses(String query) {
        // Сохраняем текущий поисковый запрос
        searchQueryLiveData.setValue(query);
        
        // Сбрасываем фильтр по уровню при поиске
        selectedLevelLiveData.setValue(null);
        
        // Показываем состояние загрузки
        coursesLiveData.setValue(Resource.loading(null));
        
        if (query == null || query.trim().isEmpty()) {
            // Если запрос пустой - показываем все курсы
            loadAllCourses();
        } else {
            // Иначе - выполняем поиск через Repository
            LiveData<List<Course>> source = repository.searchCourses(query);
            switchSource(source);
        }
    }
    

    public void filterByLevel(String level) {
        // Сохраняем выбранный уровень
        selectedLevelLiveData.setValue(level);
        
        // Сбрасываем поиск при фильтрации
        searchQueryLiveData.setValue("");
        
        // Показываем состояние загрузки
        coursesLiveData.setValue(Resource.loading(null));
        
        if (level == null) {
            // Если уровень не выбран - показываем все курсы
            loadAllCourses();
        } else {
            // Иначе - фильтруем по уровню через Repository
            LiveData<List<Course>> source = repository.getCoursesByLevel(level);
            switchSource(source);
        }
    }
    

    private void switchSource(LiveData<List<Course>> newSource) {
        // Отписываемся от предыдущего источника
        if (currentSource != null) {
            coursesLiveData.removeSource(currentSource);
        }
        
        // Сохраняем ссылку на новый источник
        currentSource = newSource;
        
        // Подписываемся на новый источник
        coursesLiveData.addSource(newSource, courses -> {
            // Когда приходят данные из Room - оборачиваем в Resource
            if (courses != null && !courses.isEmpty()) {
                // Успех - данные загружены
                coursesLiveData.setValue(Resource.success(courses));
            } else if (courses != null && courses.isEmpty()) {
                // Пустой список - показываем Empty State
                coursesLiveData.setValue(Resource.success(courses));
            } else {
                // null обычно не приходит из Room, но на всякий случай
                coursesLiveData.setValue(Resource.error("Ошибка загрузки данных", null));
            }
        });
    }
    
    /**
     * Подписывается на ошибки из Repository ОДИН РАЗ.
     * Вызывается только из конструктора.
     * 
     * Когда Repository получает ошибку от Retrofit - публикуем её в UI.
     * Сохраняем текущие данные чтобы не скрывать список при ошибке обновления.
     */
    private void observeErrorState() {
        errorLiveDataSource = repository.getErrorLiveData();
        
        coursesLiveData.addSource(errorLiveDataSource, error -> {
            if (error != null && !error.isEmpty()) {
                // Произошла ошибка - показываем её пользователю
                // Передаем текущие данные (если есть) чтобы не скрывать список при ошибке обновления
                Resource<List<Course>> currentResource = coursesLiveData.getValue();
                List<Course> currentData = currentResource != null ? currentResource.getData() : null;
                coursesLiveData.setValue(Resource.error(error, currentData));
            }
        });
    }

    private void observeLoadingState() {
        loadingLiveDataSource = repository.getLoadingLiveData();
        
        // Добавляем источник для отслеживания загрузки
        coursesLiveData.addSource(loadingLiveDataSource, isLoading -> {
            if (isLoading != null && isLoading) {
                // Если идёт загрузка с сервера
                Resource<List<Course>> currentResource = coursesLiveData.getValue();
                
                if (currentResource == null || currentResource.getData() == null) {
                    // Нет закешированных данных - показываем полноэкранный Loading
                    coursesLiveData.setValue(Resource.loading(null));
                } else {
                    // Есть старые данные - показываем их + индикатор обновления
                    coursesLiveData.setValue(Resource.loading(currentResource.getData()));
                }
            }
        });
    }

    public void toggleFavorite(Course course) {
        // Инвертируем статус избранного
        boolean newFavoriteStatus = !course.isFavorite();
        
        // Обновляем через Repository
        repository.updateFavoriteStatus(course.getId(), newFavoriteStatus);
    }
    
    // Геттеры для LiveData - Activity подписывается на эти поля
    

    public LiveData<Resource<List<Course>>> getCoursesLiveData() {
        return coursesLiveData;
    }
    
    /**
     * @return LiveData с текущим поисковым запросом
     */
    public LiveData<String> getSearchQueryLiveData() {
        return searchQueryLiveData;
    }
    
    /**
     * @return LiveData с выбранным уровнем фильтра
     */
    public LiveData<String> getSelectedLevelLiveData() {
        return selectedLevelLiveData;
    }
    
    /**
     * Очистка ресурсов при уничтожении ViewModel.
     * Вызывается системой когда ViewModel больше не нужна
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Останавливаем ExecutorService в Repository
        repository.cleanup();
    }
}

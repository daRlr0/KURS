package com.example.cors.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cors.data.local.dao.CourseDao;
import com.example.cors.data.local.database.AppDatabase;
import com.example.cors.data.local.entity.CourseEntity;
import com.example.cors.data.mapper.CourseMapper;
import com.example.cors.data.remote.api.CourseApiService;
import com.example.cors.data.remote.api.RetrofitClient;
import com.example.cors.data.remote.dto.CourseDto;
import com.example.cors.domain.model.Course;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository - единая точка доступа к данным для ViewModel.
 * Инкапсулирует логику получения данных из разных источников (БД, API).
 * 
 * Repository Pattern обеспечивает:
 * - Абстракцию источника данных (ViewModel не знает откуда данные)
 * - Кеширование (сначала показываем данные из БД, потом обновляем с сервера)
 * - Offline-first подход (приложение работает без интернета)
 * 
 * Стратегия работы:
 * 1. Возвращаем данные из локальной БД (быстро)
 * 2. Параллельно делаем запрос к API
 * 3. Обновляем БД новыми данными
 * 4. LiveData автоматически уведомляет UI об обновлении
 */
public class CourseRepository {
    
    /**
     * DAO для работы с локальной БД
     */
    private final CourseDao courseDao;
    
    /**
     * API сервис для сетевых запросов
     */
    private final CourseApiService apiService;
    
    /**
     * Executor для выполнения операций БД в фоновом потоке.
     * Room не позволяет выполнять операции записи в главном потоке.
     */
    private final ExecutorService executorService;
    
    /**
     * LiveData для отслеживания ошибок.
     * ViewModel подписывается на это поле для показа ошибок пользователю.
     */
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    /**
     * LiveData для отслеживания состояния загрузки.
     * true - идёт загрузка, false - загрузка завершена.
     */
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    
    /**
     * Конструктор Repository - инициализирует все зависимости.
     * 
     * @param context Контекст для получения instance БД
     */
    public CourseRepository(Context context) {
        // Получаем DAO из singleton instance БД
        AppDatabase database = AppDatabase.getInstance(context);
        courseDao = database.courseDao();
        
        // Получаем API сервис из Retrofit client
        apiService = RetrofitClient.getApiService();
        
        // Создаём ExecutorService с одним потоком для последовательного выполнения операций БД
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Получает все курсы (из БД) и запускает обновление с сервера.
     * 
     * Алгоритм работы:
     * 1. Возвращаем LiveData из БД (UI сразу получает закешированные данные)
     * 2. Параллельно делаем API запрос для обновления
     * 3. Сохраняем новые данные в БД
     * 4. Room автоматически обновляет LiveData, UI получает свежие данные
     * 
     * @return LiveData со списком курсов в формате Domain модели
     */
    public LiveData<List<Course>> getAllCourses() {
        // Запускаем фоновое обновление с сервера
        refreshCoursesFromApi();
        
        // Получаем LiveData<List<CourseEntity>> из DAO
        LiveData<List<CourseEntity>> entityLiveData = courseDao.getAllCourses();
        
        // Трансформируем LiveData<List<Entity>> в LiveData<List<Domain>>
        // Transformations.map применяется каждый раз при изменении данных
        return Transformations.map(entityLiveData, entityList -> 
            CourseMapper.entityListToDomainList(entityList)
        );
    }
    
    /**
     * Получает избранные курсы из БД.
     * Работает полностью offline - не делает сетевых запросов.
     * 
     * @return LiveData со списком избранных курсов
     */
    public LiveData<List<Course>> getFavoriteCourses() {
        LiveData<List<CourseEntity>> entityLiveData = courseDao.getFavoriteCourses();
        
        // Маппим Entity список в Domain список
        return Transformations.map(entityLiveData, entityList -> 
            CourseMapper.entityListToDomainList(entityList)
        );
    }
    
    /**
     * Ищет курсы по названию в локальной БД.
     * 
     * @param query Поисковый запрос (часть названия курса)
     * @return LiveData со списком найденных курсов
     */
    public LiveData<List<Course>> searchCourses(String query) {
        LiveData<List<CourseEntity>> entityLiveData = courseDao.searchCourses(query);
        
        return Transformations.map(entityLiveData, entityList -> 
            CourseMapper.entityListToDomainList(entityList)
        );
    }
    
    /**
     * Фильтрует курсы по уровню сложности.
     * 
     * @param level Уровень ("Beginner", "Intermediate", "Advanced")
     * @return LiveData со списком курсов указанного уровня
     */
    public LiveData<List<Course>> getCoursesByLevel(String level) {
        LiveData<List<CourseEntity>> entityLiveData = courseDao.getCoursesByLevel(level);
        
        return Transformations.map(entityLiveData, entityList -> 
            CourseMapper.entityListToDomainList(entityList)
        );
    }
    
    /**
     * Получает детальную информацию о курсе по ID.
     * 
     * @param courseId ID курса
     * @return LiveData с данными курса
     */
    public LiveData<Course> getCourseById(int courseId) {
        LiveData<CourseEntity> entityLiveData = courseDao.getCourseById(courseId);
        
        // Трансформируем LiveData<Entity> в LiveData<Domain>
        return Transformations.map(entityLiveData, entity -> {
            if (entity != null) {
                return CourseMapper.entityToDomain(entity);
            }
            return null;
        });
    }
    
    /**
     * Обновляет статус избранного для курса.
     * Операция выполняется в фоновом потоке (через ExecutorService).
     * 
     * @param courseId ID курса
     * @param isFavorite Новый статус избранного
     */
    public void updateFavoriteStatus(int courseId, boolean isFavorite) {
        executorService.execute(() -> {
            // Выполняем обновление в фоновом потоке
            courseDao.updateFavoriteStatus(courseId, isFavorite);
        });
    }
    
    /**
     * Сохраняет комментарий и оценку пользователя для курса.
     * 
     * @param courseId ID курса
     * @param comment Комментарий пользователя
     * @param rating Оценка (0-5)
     */
    public void saveCourseReview(int courseId, String comment, float rating) {
        executorService.execute(() -> {
            // Обновляем комментарий и рейтинг в БД
            courseDao.updateCourseReview(courseId, comment, rating);
        });
    }
    
    /**
     * Обновляет данные курсов с сервера и сохраняет в БД.
     * Это приватный метод, вызывается автоматически при getAllCourses().
     * 
     * В DEMO режиме (когда API - заглушка) этот метод НЕ делает сетевые запросы,
     * чтобы избежать ошибок "Unable to connect" при первом запуске.
     * Данные уже есть в локальной БД благодаря DatabaseInitializer.
     * 
     * Алгоритм (в production с реальным API):
     * 1. Устанавливаем loadingLiveData = true
     * 2. Делаем асинхронный запрос к API
     * 3. При успехе - сохраняем данные в БД
     * 4. При ошибке - публикуем сообщение об ошибке
     * 5. В любом случае устанавливаем loadingLiveData = false
     */
    private void refreshCoursesFromApi() {
        // DEMO MODE: API - это заглушка, не делаем реальных запросов
        // В production приложении раскомментируйте код ниже и укажите реальный BASE_URL
        
        // Сразу устанавливаем loading = false, т.к. запрос не делаем
        loadingLiveData.postValue(false);
        
        // Закомментированный код для production с реальным API:
        /*
        // Индикатор загрузки - показываем прогресс в UI
        loadingLiveData.postValue(true);
        
        // Создаём асинхронный запрос к API
        Call<List<CourseDto>> call = apiService.getCourses();
        
        // Выполняем запрос асинхронно
        call.enqueue(new Callback<List<CourseDto>>() {
            @Override
            public void onResponse(Call<List<CourseDto>> call, Response<List<CourseDto>> response) {
                loadingLiveData.postValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    // Получили данные - конвертируем DTO в Entity
                    List<CourseDto> dtoList = response.body();
                    List<CourseEntity> entityList = CourseMapper.dtoListToEntityList(dtoList);
                    
                    // Сохраняем в БД в фоновом потоке
                    executorService.execute(() -> {
                        courseDao.insertCourses(entityList);
                    });
                } else {
                    // Сервер вернул ошибку (например, 404, 500)
                    errorLiveData.postValue("Ошибка сервера: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<List<CourseDto>> call, Throwable t) {
                loadingLiveData.postValue(false);
                
                // Публикуем сообщение об ошибке для отображения в UI
                errorLiveData.postValue("Ошибка сети: " + t.getMessage());
            }
        });
        */
    }
    
    /**
     * Геттер для LiveData с ошибками.
     * ViewModel подписывается на это поле для показа ошибок пользователю.
     * 
     * @return LiveData с сообщениями об ошибках
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    
    /**
     * Геттер для LiveData состояния загрузки.
     * ViewModel использует для показа/скрытия прогресс-бара.
     * 
     * @return LiveData с флагом загрузки
     */
    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }
    
    /**
     * Очищает ExecutorService при уничтожении Repository.
     * Вызывается при закрытии приложения для освобождения ресурсов.
     */
    public void cleanup() {
        executorService.shutdown();
    }
}

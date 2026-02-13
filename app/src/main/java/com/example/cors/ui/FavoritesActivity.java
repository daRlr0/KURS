package com.example.cors.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cors.data.Resource;
import com.example.cors.databinding.ActivityFavoritesBinding;
import com.example.cors.domain.model.Course;
import com.example.cors.ui.adapter.CourseAdapter;
import com.example.cors.viewmodel.FavoritesViewModel;

/**
 * Экран избранных курсов.
 * 
 * Функционал:
 * - Отображение списка избранных курсов
 * - Возможность удаления из избранного
 * - Переход к деталям курса при клике
 * - Отображение Empty State если нет избранных курсов
 * - Работает полностью offline (без сетевых запросов)
 */
public class FavoritesActivity extends AppCompatActivity {
    
    /**
     * ViewBinding для доступа к View элементам
     */
    private ActivityFavoritesBinding binding;
    
    /**
     * ViewModel для управления списком избранного
     */
    private FavoritesViewModel viewModel;
    
    /**
     * Adapter для RecyclerView с избранными курсами
     */
    private CourseAdapter adapter;
    
    /**
     * Метод onCreate - инициализация Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализируем ViewBinding
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Настраиваем ActionBar
        setupActionBar();
        
        // Инициализируем ViewModel
        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        
        // Настраиваем RecyclerView
        setupRecyclerView();
        
        // Подписываемся на изменения данных
        observeViewModel();
    }
    
    /**
     * Настраивает ActionBar с кнопкой "Назад".
     */
    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Избранное");
        }
    }
    
    /**
     * Настраивает RecyclerView для отображения списка избранных курсов.
     */
    private void setupRecyclerView() {
        // Создаём adapter (переиспользуем тот же adapter, что и на главном экране)
        adapter = new CourseAdapter();
        
        // Устанавливаем LayoutManager
        binding.favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Подключаем adapter
        binding.favoritesRecyclerView.setAdapter(adapter);
        
        // Listener на клик по курсу - открываем детали
        adapter.setOnCourseClickListener(course -> {
            openCourseDetail(course);
        });
        
        // Listener на клик по кнопке избранного - удаляем из избранного
        adapter.setOnFavoriteClickListener(course -> {
            // Удаляем из избранного через ViewModel
            viewModel.removeFromFavorites(course);
            
            // Показываем подтверждение
            Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Подписывается на LiveData из ViewModel.
     * 
     * Обработка Resource<List<Course>> для избранных курсов:
     * 1. LOADING - показываем ProgressBar (обычно очень быстро, т.к. Room локальный)
     * 2. SUCCESS + пустой список - показываем Empty State с подсказкой
     * 3. SUCCESS + есть курсы - показываем RecyclerView со списком
     * 4. ERROR - не должно происходить в offline режиме, но обрабатываем на всякий случай
     * 
     * Особенности экрана избранного:
     * - Полностью offline (данные только из Room)
     * - Автоматические обновления при изменениях в БД
     * - DiffUtil анимирует добавление/удаление курсов
     */
    private void observeViewModel() {
        // Подписываемся на Resource с избранными курсами
        viewModel.getFavoritesLiveData().observe(this, resource -> {
            if (resource == null) {
                return; // Защита от null
            }
            
            // Обрабатываем состояние через switch
            switch (resource.getStatus()) {
                case LOADING:
                    // Состояние загрузки (обычно мгновенное для Room)
                    // Можно не показывать ProgressBar т.к. Room очень быстрый
                    break;
                    
                case SUCCESS:
                    // Успешная загрузка - проверяем есть ли данные
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        // Есть избранные курсы - показываем список
                        showSuccessState();
                        adapter.setCourses(resource.getData());
                    } else {
                        // Нет избранных курсов - показываем Empty State
                        showEmptyState();
                    }
                    break;
                    
                case ERROR:
                    // Ошибка не должна происходить при чтении из Room
                    // Но на всякий случай показываем Empty State
                    showEmptyState();
                    
                    // Показываем Toast с ошибкой
                    String errorMessage = resource.getMessage();
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
    }
    
    /**
     * Показывает состояние успеха (RecyclerView со списком избранных).
     */
    private void showSuccessState() {
        binding.favoritesRecyclerView.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
    }
    
    /**
     * Показывает Empty State (нет избранных курсов).
     * Отображает иконку и текст с подсказкой как добавить курсы в избранное.
     */
    private void showEmptyState() {
        binding.favoritesRecyclerView.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
    }
    
    /**
     * Открывает экран деталей курса.
     * 
     * @param course Курс для отображения
     */
    private void openCourseDetail(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }
    
    /**
     * Обрабатывает нажатие на элементы меню.
     * 
     * @param item Элемент меню
     * @return true если событие обработано
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обрабатываем кнопку "Назад"
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Освобождаем ресурсы при уничтожении Activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

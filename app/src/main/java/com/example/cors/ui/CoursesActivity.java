package com.example.cors.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cors.R;
import com.example.cors.data.Resource;
import com.example.cors.databinding.ActivityCoursesBinding;
import com.example.cors.domain.model.Course;
import com.example.cors.ui.adapter.CourseAdapter;
import com.example.cors.viewmodel.CoursesViewModel;
import com.google.android.material.chip.Chip;

/**
 * Главный экран приложения - список курсов.
 * 
 * Функционал:
 * - Отображение списка курсов в RecyclerView
 * - Поиск по названию через SearchView
 * - Фильтрация по уровню сложности через Material Chips
 * - Добавление/удаление из избранного
 * - Переход к деталям курса при клике
 * - Обработка состояний Loading, Empty, Error
 * 
 * Использует ViewBinding для доступа к View элементам.
 */
public class CoursesActivity extends AppCompatActivity {
    
    /**
     * ViewBinding - автоматически генерируемый класс для доступа к View.
     * Заменяет findViewById, обеспечивает type-safety.
     */
    private ActivityCoursesBinding binding;
    
    /**
     * ViewModel для управления данными и логикой экрана
     */
    private CoursesViewModel viewModel;
    
    /**
     * Adapter для RecyclerView со списком курсов
     */
    private CourseAdapter adapter;
    
    /**
     * Метод onCreate - точка входа при создании Activity.
     * Вызывается системой при первом создании экрана.
     * 
     * @param savedInstanceState Сохранённое состояние (при пересоздании экрана)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализируем ViewBinding
        // Создаёт экземпляр binding класса и инфлейтит layout
        binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Инициализируем БД тестовыми данными (только при первом запуске)
        // DatabaseInitializer проверяет наличие данных и добавляет их если БД пустая
        initializeDatabaseIfNeeded();
        
        // Настраиваем ActionBar
        setupActionBar();
        
        // Инициализируем ViewModel через ViewModelProvider
        // ViewModelProvider гарантирует, что ViewModel переживёт пересоздание Activity
        viewModel = new ViewModelProvider(this).get(CoursesViewModel.class);
        
        // Настраиваем UI компоненты
        setupRecyclerView();
        setupChips();
        setupFab();
        
        // Подписываемся на изменения данных из ViewModel
        observeViewModel();
    }
    
    /**
     * Инициализирует БД тестовыми данными при первом запуске.
     * Проверяет SharedPreferences - если приложение запускается впервые,
     * вызывает DatabaseInitializer для заполнения БД примерами курсов.
     */
    private void initializeDatabaseIfNeeded() {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("is_first_run", true);
        
        if (isFirstRun) {
            // Заполняем БД тестовыми данными
            com.example.cors.utils.DatabaseInitializer.populateDatabase(this);
            
            // Сохраняем флаг что приложение уже запускалось
            prefs.edit().putBoolean("is_first_run", false).apply();
        }
    }
    
    /**
     * Настраивает ActionBar (верхнюю панель).
     */
    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Каталог курсов");
        }
    }
    
    /**
     * Настраивает RecyclerView для отображения списка курсов.
     * 
     * RecyclerView - эффективный компонент для отображения больших списков.
     * Переиспользует View элементы для экономии памяти.
     */
    private void setupRecyclerView() {
        // Создаём adapter
        adapter = new CourseAdapter();
        
        // Устанавливаем LayoutManager - определяет как располагать элементы
        // LinearLayoutManager - вертикальный список
        binding.coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Подключаем adapter к RecyclerView
        binding.coursesRecyclerView.setAdapter(adapter);
        
        // Устанавливаем listener на клик по курсу
        adapter.setOnCourseClickListener(course -> {
            // При клике открываем экран деталей курса
            openCourseDetail(course);
        });
        
        // Устанавливаем listener на клик по кнопке избранного
        adapter.setOnFavoriteClickListener(course -> {
            // Переключаем статус избранного через ViewModel
            viewModel.toggleFavorite(course);
            
            // Показываем Toast с подтверждением
            String message = course.isFavorite() ? 
                    "Удалено из избранного" : "Добавлено в избранное";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Настраивает Material Chips для фильтрации по уровню с цветовой индикацией.
     * 
     * Material Design 3 Filter Chips:
     * - Single-selection mode (только один выбран)
     * - Цветовая кодировка для быстрой идентификации уровней
     * - Ripple эффект при нажатии
     * - Плавная анимация переключения состояний
     * 
     * Цветовая схема уровней:
     * - "Все" = Primary Indigo (нейтральный)
     * - "Начальный" = Зелёный (безопасно, можно начинать)
     * - "Средний" = Оранжевый (внимание, нужен опыт)
     * - "Продвинутый" = Красный (сложно, для экспертов)
     */
    private void setupChips() {
        // Применяем цветовые схемы для Chips
        applyChipColors();
        
        // Chip "Все" - показывает все курсы без фильтра
        binding.chipAll.setOnClickListener(v -> {
            viewModel.filterByLevel(null); // null = все уровни
        });
        
        // Chip "Начальный" - фильтрует по Beginner
        binding.chipBeginner.setOnClickListener(v -> {
            viewModel.filterByLevel("Beginner");
        });
        
        // Chip "Средний" - фильтрует по Intermediate
        binding.chipIntermediate.setOnClickListener(v -> {
            viewModel.filterByLevel("Intermediate");
        });
        
        // Chip "Продвинутый" - фильтрует по Advanced
        binding.chipAdvanced.setOnClickListener(v -> {
            viewModel.filterByLevel("Advanced");
        });
        
        // По умолчанию выбран chip "Все"
        binding.chipAll.setChecked(true);
    }
    
    /**
     * Применяет цветовые схемы к Filter Chips для визуального различия уровней.
     * 
     * ColorStateList для Chips:
     * - Checked state: цветной фон (chipBackgroundColor) + цветной текст
     * - Unchecked state: белый фон + серый текст
     * 
     * Это улучшает UX - пользователь мгновенно понимает:
     * - Какой уровень выбран (цветной chip)
     * - Что означают разные уровни (цветовая кодировка)
     */
    private void applyChipColors() {
        // Chip "Все" - Primary цвет (Indigo)
        android.content.res.ColorStateList allColors = android.content.res.ColorStateList.valueOf(
            getColor(R.color.md_theme_light_primaryContainer)
        );
        binding.chipAll.setChipBackgroundColor(allColors);
        
        // Chip "Начальный" - Зелёный
        android.content.res.ColorStateList beginnerColors = android.content.res.ColorStateList.valueOf(
            getColor(R.color.level_beginner_container)
        );
        binding.chipBeginner.setChipBackgroundColor(beginnerColors);
        binding.chipBeginner.setTextColor(getColor(R.color.level_beginner));
        
        // Chip "Средний" - Оранжевый
        android.content.res.ColorStateList intermediateColors = android.content.res.ColorStateList.valueOf(
            getColor(R.color.level_intermediate_container)
        );
        binding.chipIntermediate.setChipBackgroundColor(intermediateColors);
        binding.chipIntermediate.setTextColor(getColor(R.color.level_intermediate));
        
        // Chip "Продвинутый" - Красный
        android.content.res.ColorStateList advancedColors = android.content.res.ColorStateList.valueOf(
            getColor(R.color.level_advanced_container)
        );
        binding.chipAdvanced.setChipBackgroundColor(advancedColors);
        binding.chipAdvanced.setTextColor(getColor(R.color.level_advanced));
    }
    
    /**
     * Настраивает FloatingActionButton для перехода к экрану избранного.
     * 
     * FAB - кнопка Material Design для основного действия на экране.
     */
    private void setupFab() {
        binding.fab.setOnClickListener(v -> {
            // Открываем экран избранных курсов
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Подписывается на LiveData из ViewModel для обновления UI.
     * 
     * Observer pattern с Resource<T>:
     * 1. Activity подписывается на LiveData<Resource<List<Course>>>
     * 2. ViewModel публикует изменения состояния и данных
     * 3. Observer callback вызывается автоматически
     * 4. Activity обрабатывает все три состояния: Loading/Success/Error
     * 
     * Преимущества Resource<T>:
     * - Один observer вместо трех (courses, uiState, error)
     * - Гарантированная консистентность (данные и состояние всегда синхронны)
     * - Невозможно забыть обработать какое-то состояние
     */
    private void observeViewModel() {
        // Подписываемся на Resource с курсами
        // Resource содержит и данные, и состояние, и ошибку в одном объекте
        viewModel.getCoursesLiveData().observe(this, resource -> {
            if (resource == null) {
                return; // Защита от null (обычно не происходит)
            }
            
            // Обрабатываем состояние через switch
            switch (resource.getStatus()) {
                case LOADING:
                    // Состояние загрузки - показываем ProgressBar
                    showLoadingState();
                    
                    // Если есть старые данные - показываем их во время загрузки
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        adapter.setCourses(resource.getData());
                        binding.coursesRecyclerView.setVisibility(View.VISIBLE);
                    }
                    break;
                    
                case SUCCESS:
                    // Успешная загрузка - показываем данные или Empty State
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        // Есть данные - показываем список
                        showSuccessState();
                        adapter.setCourses(resource.getData());
                    } else {
                        // Нет данных - показываем Empty State
                        showEmptyState();
                    }
                    break;
                    
                case ERROR:
                    // Ошибка - показываем Toast и пытаемся показать закешированные данные
                    String errorMessage = resource.getMessage();
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                    
                    // Если есть старые данные - показываем их несмотря на ошибку
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        showSuccessState();
                        adapter.setCourses(resource.getData());
                    } else {
                        // Нет даже старых данных - показываем Empty State
                        showEmptyState();
                    }
                    break;
            }
        });
    }
    
    /**
     * Показывает состояние загрузки (ProgressBar).
     * Вызывается когда Resource.Status == LOADING.
     * 
     * Анимация: ProgressBar появляется без анимации для мгновенной обратной связи
     */
    private void showLoadingState() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        // RecyclerView не скрываем - если есть старые данные, они останутся видимыми
    }
    
    /**
     * Показывает состояние успеха (RecyclerView со списком) с плавной анимацией.
     * Вызывается когда Resource.Status == SUCCESS и есть данные.
     * 
     * Alpha Animation (Fade In):
     * - Создает плавный переход из Loading в Success
     * - RecyclerView появляется с эффектом fade in (300ms)
     * - Улучшает UX, убирая резкие переключения
     * - ProgressBar исчезает с fade out (200ms)
     */
    private void showSuccessState() {
        // Скрываем ProgressBar с анимацией fade out
        if (binding.progressBar.getVisibility() == View.VISIBLE) {
            binding.progressBar.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_out)
            );
            binding.progressBar.setVisibility(View.GONE);
        }
        
        binding.emptyStateLayout.setVisibility(View.GONE);
        
        // Показываем RecyclerView с анимацией fade in
        if (binding.coursesRecyclerView.getVisibility() != View.VISIBLE) {
            binding.coursesRecyclerView.setVisibility(View.VISIBLE);
            binding.coursesRecyclerView.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
            );
        }
    }
    
    /**
     * Показывает Empty State (заглушка "Ничего не найдено") с плавной анимацией.
     * Вызывается когда Resource.Status == SUCCESS но данных нет,
     * или когда Resource.Status == ERROR и нет даже старых данных.
     * 
     * Alpha Animation: Empty State появляется плавно для приятного UX
     */
    private void showEmptyState() {
        // Скрываем ProgressBar с анимацией
        if (binding.progressBar.getVisibility() == View.VISIBLE) {
            binding.progressBar.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_out)
            );
            binding.progressBar.setVisibility(View.GONE);
        }
        
        binding.coursesRecyclerView.setVisibility(View.GONE);
        
        // Показываем Empty State с анимацией fade in
        if (binding.emptyStateLayout.getVisibility() != View.VISIBLE) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
            );
        }
    }
    
    /**
     * Открывает экран деталей курса.
     * 
     * @param course Курс для отображения
     */
    private void openCourseDetail(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        // Передаём ID курса через Intent
        intent.putExtra("COURSE_ID", course.getId());
        startActivity(intent);
    }
    
    /**
     * Создаёт меню в ActionBar.
     * Вызывается системой при создании меню.
     * 
     * @param menu Меню для заполнения
     * @return true если меню создано
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Инфлейтим меню из XML
        getMenuInflater().inflate(R.menu.menu_courses, menu);
        
        // Настраиваем SearchView для поиска курсов
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
        // Настраиваем hint и поведение SearchView
        searchView.setQueryHint("Поиск курсов...");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        
        // Устанавливаем listener на изменение текста поиска
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Вызывается при отправке запроса (нажатие Enter).
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Выполняем поиск
                viewModel.searchCourses(query);
                return true;
            }
            
            /**
             * Вызывается при каждом изменении текста.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                // Выполняем поиск при каждом изменении (живой поиск)
                viewModel.searchCourses(newText);
                return true;
            }
        });
        
        return true;
    }
    
    /**
     * Освобождаем ресурсы при уничтожении Activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Очищаем binding для предотвращения утечек памяти
        binding = null;
    }
}

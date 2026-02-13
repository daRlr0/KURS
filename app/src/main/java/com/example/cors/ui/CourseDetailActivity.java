package com.example.cors.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cors.R;
import com.example.cors.databinding.ActivityCourseDetailBinding;
import com.example.cors.domain.model.Course;
import com.example.cors.utils.GlideHelper;
import com.example.cors.viewmodel.CourseDetailViewModel;

/**
 * Экран деталей курса.
 * 
 * Функционал:
 * - Отображение полной информации о курсе
 * - Показ изображения, названия, описания, длительности, уровня
 * - Отображение комментария и оценки пользователя
 * - Возможность редактирования комментария и оценки
 * - Добавление/удаление из избранного через FAB
 * - Кнопка "Назад" в ActionBar
 */
public class CourseDetailActivity extends AppCompatActivity {
    
    /**
     * ViewBinding для доступа к View элементам
     */
    private ActivityCourseDetailBinding binding;
    
    /**
     * ViewModel для управления данными курса
     */
    private CourseDetailViewModel viewModel;
    
    /**
     * ID курса, переданный через Intent
     */
    private int courseId;
    
    /**
     * Текущий курс (для обновления UI)
     */
    private Course currentCourse;
    
    /**
     * Метод onCreate - инициализация Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализируем ViewBinding
        binding = ActivityCourseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Настраиваем ActionBar с кнопкой "Назад"
        setupActionBar();
        
        // Получаем ID курса из Intent
        courseId = getIntent().getIntExtra("COURSE_ID", -1);
        
        // Проверяем валидность ID
        if (courseId == -1) {
            // ID не передан - показываем ошибку и закрываем Activity
            Toast.makeText(this, "Ошибка: курс не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Инициализируем ViewModel
        viewModel = new ViewModelProvider(this).get(CourseDetailViewModel.class);
        
        // Загружаем данные курса
        viewModel.loadCourse(courseId);
        
        // Настраиваем кнопки и действия
        setupButtons();
        
        // Подписываемся на изменения данных
        observeViewModel();
    }
    
    /**
     * Настраивает ActionBar с кнопкой "Назад".
     */
    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Показываем кнопку "Назад"
            getSupportActionBar().setTitle("Детали курса");
        }
    }
    
    /**
     * Настраивает кнопки на экране.
     */
    private void setupButtons() {
        // FAB для добавления/удаления из избранного
        binding.fabFavorite.setOnClickListener(v -> {
            if (currentCourse != null) {
                // Переключаем статус избранного
                boolean newStatus = !currentCourse.isFavorite();
                viewModel.toggleFavorite(newStatus);
                
                // Показываем подтверждение
                String message = newStatus ? 
                        "Добавлено в избранное" : "Удалено из избранного";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Кнопка "Сохранить" для комментария и оценки
        binding.saveReviewButton.setOnClickListener(v -> {
            saveReview();
        });
    }
    
    /**
     * Подписывается на LiveData из ViewModel.
     */
    private void observeViewModel() {
        // Подписываемся на данные курса
        viewModel.getCourseLiveData().observe(this, course -> {
            if (course != null) {
                // Сохраняем текущий курс
                currentCourse = course;
                
                // Обновляем UI с данными курса
                displayCourseData(course);
            } else {
                // Курс не найден в БД
                Toast.makeText(this, "Курс не найден", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    /**
     * Отображает данные курса на экране с применением стилей и анимаций.
     * Заполняет все View элементы информацией о курсе.
     * 
     * Особенности реализации:
     * - Glide с высоким качеством для header изображения
     * - Цветовая индикация уровня сложности
     * - Форматирование текста для читабельности
     * 
     * @param course Курс для отображения
     */
    private void displayCourseData(Course course) {
        // Устанавливаем заголовок
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(course.getTitle());
        }
        
        /**
         * Загрузка Hero Header Image через GlideHelper в высоком качестве
         * 
         * GlideHelper.loadHeaderImage() использует оптимизированную конфигурацию:
         * - Высокое качество 1200x800px для Full HD дисплеев
         * - CrossFade 400ms для premium плавного появления
         * - Disk + Memory кеширование для offline режима
         * 
         * Преимущества использования helper вместо прямого Glide:
         * 1. Консистентность настроек во всем приложении
         * 2. Единая точка изменения конфигурации
         * 3. Переиспользование кода (DRY principle)
         * 4. Подробная документация в одном месте
         * 
         * Hero Header лучшие практики:
         * - Большое изображение для immersive эффекта
         * - centerCrop для заполнения без искажений
         * - Parallax scroll для depth ощущения
         * - Gradient scrim для читабельности Toolbar
         */
        GlideHelper.loadHeaderImage(
            this,
            course.getImageUrl(),
            binding.courseImageView
        );
        
        // Заполняем текстовые поля
        binding.courseTitleTextView.setText(course.getTitle());
        binding.providerTextView.setText("Провайдер: " + course.getProvider());
        binding.durationTextView.setText("Длительность: " + course.getFormattedDuration());
        
        // Устанавливаем уровень с цветовой индикацией
        binding.levelTextView.setText("Уровень: " + course.getLocalizedLevel());
        setLevelColor(course.getLevel());
        
        binding.descriptionTextView.setText(course.getDescription());
        
        // Заполняем поля комментария и оценки (если они уже есть)
        binding.commentEditText.setText(course.getComment());
        binding.ratingBar.setRating(course.getUserRating());
        
        // Обновляем иконку FAB в зависимости от статуса избранного
        updateFavoriteIcon(course.isFavorite());
    }
    
    /**
     * Устанавливает цвет для TextView уровня сложности.
     * 
     * Цветовая индикация помогает быстро оценить сложность:
     * - Зелёный (Beginner) = подходит для новичков
     * - Оранжевый (Intermediate) = требуется базовый опыт
     * - Красный (Advanced) = только для экспертов
     * 
     * @param level Уровень сложности курса
     */
    private void setLevelColor(String level) {
        int color;
        switch (level) {
            case "Beginner":
                color = getColor(R.color.level_beginner);
                break;
            case "Intermediate":
                color = getColor(R.color.level_intermediate);
                break;
            case "Advanced":
                color = getColor(R.color.level_advanced);
                break;
            default:
                color = getColor(R.color.md_theme_light_onSurfaceVariant);
        }
        binding.levelTextView.setTextColor(color);
    }
    
    /**
     * Обновляет иконку FAB для избранного.
     * 
     * @param isFavorite true - курс в избранном, false - не в избранном
     */
    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            binding.fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            binding.fabFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }
    
    /**
     * Сохраняет комментарий и оценку пользователя.
     * Вызывается при клике на кнопку "Сохранить".
     */
    private void saveReview() {
        // Получаем введённый комментарий
        String comment = binding.commentEditText.getText().toString().trim();
        
        // Получаем оценку из RatingBar
        float rating = binding.ratingBar.getRating();
        
        // Сохраняем через ViewModel
        viewModel.saveCourseReview(comment, rating);
        
        // Показываем подтверждение
        Toast.makeText(this, "Отзыв сохранён", Toast.LENGTH_SHORT).show();
        
        // Скрываем клавиатуру
        hideKeyboard();
    }
    
    /**
     * Скрывает программную клавиатуру.
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) 
                getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    /**
     * Обрабатывает нажатие на элементы меню.
     * 
     * @param item Элемент меню, на который нажали
     * @return true если событие обработано
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обрабатываем нажатие на кнопку "Назад"
        if (item.getItemId() == android.R.id.home) {
            // Закрываем Activity и возвращаемся назад
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

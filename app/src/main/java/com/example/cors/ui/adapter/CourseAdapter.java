package com.example.cors.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cors.R;
import com.example.cors.domain.model.Course;
import com.example.cors.utils.GlideHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter для RecyclerView со списком курсов.
 * Отвечает за создание и обновление элементов списка.
 * 
 * Использует ViewHolder паттерн для эффективного переиспользования View.
 * DiffUtil для оптимизации обновлений списка (анимации вставки/удаления).
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    
    /**
     * Список курсов для отображения
     */
    private List<Course> courses = new ArrayList<>();
    
    /**
     * Listener для обработки кликов на элемент списка
     */
    private OnCourseClickListener clickListener;
    
    /**
     * Listener для обработки кликов на кнопку "Избранное"
     */
    private OnFavoriteClickListener favoriteClickListener;
    
    /**
     * Устанавливает новый список курсов с использованием DiffUtil.
     * DiffUtil вычисляет разницу между старым и новым списком,
     * и обновляет только изменившиеся элементы (эффективно и с анимацией).
     * 
     * @param newCourses Новый список курсов
     */
    public void setCourses(List<Course> newCourses) {
        // Создаём callback для DiffUtil
        CourseDiffCallback diffCallback = new CourseDiffCallback(this.courses, newCourses);
        
        // Вычисляем разницу между списками
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        
        // Обновляем список
        this.courses = new ArrayList<>(newCourses);
        
        // Применяем изменения к RecyclerView (с анимацией)
        diffResult.dispatchUpdatesTo(this);
    }
    
    /**
     * Устанавливает listener для кликов на элемент курса.
     * 
     * @param listener Callback для обработки клика
     */
    public void setOnCourseClickListener(OnCourseClickListener listener) {
        this.clickListener = listener;
    }
    
    /**
     * Устанавливает listener для кликов на кнопку избранного.
     * 
     * @param listener Callback для обработки клика на избранное
     */
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }
    
    /**
     * Создаёт новый ViewHolder.
     * Вызывается RecyclerView когда нужно создать новый элемент списка.
     * 
     * @param parent Родительский ViewGroup
     * @param viewType Тип view (не используется, т.к. все элементы одинаковые)
     * @return Новый ViewHolder
     */
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Инфлейтим layout для одного элемента списка
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        
        return new CourseViewHolder(view);
    }
    
    /**
     * Связывает данные курса с ViewHolder.
     * Вызывается RecyclerView для заполнения элемента данными.
     * 
     * @param holder ViewHolder для заполнения
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        // Получаем курс из списка
        Course course = courses.get(position);
        
        // Передаём данные в ViewHolder для отображения
        holder.bind(course);
    }
    
    /**
     * Возвращает количество элементов в списке.
     * 
     * @return Размер списка курсов
     */
    @Override
    public int getItemCount() {
        return courses.size();
    }
    
    /**
     * ViewHolder - держатель View элементов для одного курса.
     * Кеширует ссылки на View чтобы не вызывать findViewById повторно.
     */
    class CourseViewHolder extends RecyclerView.ViewHolder {
        
        // View элементы из layout
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView providerTextView;
        private final TextView durationTextView;
        private final TextView levelTextView;
        private final ImageButton favoriteButton;
        
        /**
         * Конструктор ViewHolder.
         * Инициализирует все View элементы и устанавливает listeners.
         * 
         * @param itemView Корневой View элемента списка
         */
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Находим все View элементы (вызывается только один раз при создании)
            imageView = itemView.findViewById(R.id.courseImageView);
            titleTextView = itemView.findViewById(R.id.courseTitleTextView);
            providerTextView = itemView.findViewById(R.id.providerTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            levelTextView = itemView.findViewById(R.id.levelTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            
            // Устанавливаем listener на клик по всему элементу
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    Course course = courses.get(position);
                    clickListener.onCourseClick(course);
                }
            });
            
            // Устанавливаем listener на кнопку избранного
            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && favoriteClickListener != null) {
                    Course course = courses.get(position);
                    favoriteClickListener.onFavoriteClick(course);
                }
            });
        }
        
        /**
         * Заполняет View элементы данными курса с применением стилей.
         * Вызывается каждый раз когда элемент нужно обновить (прокрутка, изменение данных).
         * 
         * Особенности реализации:
         * - Профессиональная настройка Glide с кешированием
         * - Цветовая индикация уровня сложности
         * - Плавная анимация смены иконки избранного
         * 
         * @param course Курс для отображения
         */
        public void bind(Course course) {
            // Устанавливаем текстовые данные
            titleTextView.setText(course.getTitle());
            providerTextView.setText(course.getProvider());
            durationTextView.setText(course.getFormattedDuration());
            levelTextView.setText(course.getLocalizedLevel());
            
            /**
             * Загрузка изображения для preview в списке через GlideHelper
             * 
             * GlideHelper.loadThumbnail() инкапсулирует профессиональную настройку Glide:
             * 
             * 1. Кеширование (двухуровневое):
             *    a) Memory Cache (RAM):
             *       - LruCache с размером ~15% доступной памяти
             *       - Bitmap хранится в памяти для мгновенного доступа (0ms)
             *       - Автоматически очищается при нехватке памяти (LRU = Least Recently Used)
             *       - Пример: прокрутили список вниз, вернулись вверх - картинки уже в памяти
             * 
             *    b) Disk Cache (Storage):
             *       - DiskLruCache с размером ~250MB по умолчанию
             *       - Сохраняет изображения на диск в папке app cache
             *       - Работает offline после первой загрузки
             *       - Стратегия AUTOMATIC:
             *         * Если трансформации (resize, crop) - кеширует результат
             *         * Если без трансформаций - кеширует оригинал
             *       - Преимущество: экономит трафик и ускоряет повторные загрузки
             * 
             * 2. Placeholder изображение:
             *    - Векторная иконка ic_course_placeholder
             *    - Показывается МГНОВЕННО при начале загрузки
             *    - Предотвращает "прыжки" UI (layout shift):
             *       * Без placeholder: пустое место → изображение появляется → UI сдвигается
             *       * С placeholder: иконка → изображение заменяет иконку → UI стабилен
             *    - Малый размер (векторный drawable ~2KB)
             * 
             * 3. Error изображение:
             *    - Та же иконка что placeholder для консистентности
             *    - Показывается при:
             *       * 404 Not Found (URL неверный)
             *       * Нет интернета (IOException)
             *       * Timeout (медленный интернет)
             *       * Corrupted image (битое изображение)
             *    - Приложение НЕ крашится при ошибке загрузки!
             * 
             * 4. CenterCrop трансформация:
             *    - Масштабирует изображение чтобы заполнить весь ImageView (100x100dp)
             *    - Обрезает излишки по краям для perfect fit
             *    - Сохраняет пропорции (aspect ratio) оригинала
             *    - Пример: фото 1920x1080 → масштабируется и обрезается до 100x100
             * 
             * 5. Override(400, 400):
             *    - Glide загрузит изображение размером 400x400px
             *    - Не весь оригинал 4000x4000px!
             *    - Экономит память: 400x400 ARGB = ~640KB vs 4000x4000 = ~64MB
             *    - Экономит трафик (Unsplash auto-resize по URL параметрам)
             * 
             * 6. CrossFade transition (200ms):
             *    - Плавное появление вместо резкого переключения
             *    - Alpha 0 → 1 за 200 миллисекунд
             *    - Создает premium feel
             * 
             * Производительность в RecyclerView:
             * - Glide автоматически cancel'ит загрузки при быстром scroll
             * - Приоритизирует видимые элементы
             * - Переиспользует Bitmap объекты (BitmapPool)
             * - Lifecycle-aware: pause/resume/destroy автоматически
             */
            GlideHelper.loadThumbnail(
                itemView.getContext(),
                course.getImageUrl(),
                imageView
            );
            
            /**
             * Установка цвета уровня сложности для визуального различия
             * 
             * Material Design рекомендует использовать цвет для быстрой идентификации:
             * - Зелёный (Beginner) = безопасно, можно начинать
             * - Оранжевый (Intermediate) = внимание, нужен опыт
             * - Красный (Advanced) = сложно, для экспертов
             */
            int levelColor;
            switch (course.getLevel()) {
                case "Beginner":
                    levelColor = itemView.getContext().getColor(R.color.level_beginner);
                    break;
                case "Intermediate":
                    levelColor = itemView.getContext().getColor(R.color.level_intermediate);
                    break;
                case "Advanced":
                    levelColor = itemView.getContext().getColor(R.color.level_advanced);
                    break;
                default:
                    levelColor = itemView.getContext().getColor(R.color.md_theme_light_onSurfaceVariant);
            }
            levelTextView.setTextColor(levelColor);
            
            /**
             * Установка иконки избранного с анимацией
             * 
             * Две иконки:
             * - ic_favorite_border (пустое сердце) = не в избранном
             * - ic_favorite_filled (красное сердце) = в избранном
             * 
             * Иконка меняется мгновенно при клике благодаря LiveData в ViewModel
             */
            if (course.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            }
        }
    }
    
    /**
     * Интерфейс для обработки кликов на элемент курса.
     * Реализуется в Activity/Fragment.
     */
    public interface OnCourseClickListener {
        /**
         * Вызывается при клике на элемент курса.
         * 
         * @param course Курс, на который кликнули
         */
        void onCourseClick(Course course);
    }
    
    /**
     * Интерфейс для обработки кликов на кнопку избранного.
     */
    public interface OnFavoriteClickListener {
        /**
         * Вызывается при клике на кнопку избранного.
         * 
         * @param course Курс, для которого нажата кнопка
         */
        void onFavoriteClick(Course course);
    }
    
    /**
     * DiffUtil.Callback для эффективного обновления списка.
     * Сравнивает старый и новый список, определяет какие элементы изменились.
     */
    private static class CourseDiffCallback extends DiffUtil.Callback {
        
        private final List<Course> oldList;
        private final List<Course> newList;
        
        /**
         * Конструктор callback.
         * 
         * @param oldList Старый список курсов
         * @param newList Новый список курсов
         */
        public CourseDiffCallback(List<Course> oldList, List<Course> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        
        /**
         * Возвращает размер старого списка.
         */
        @Override
        public int getOldListSize() {
            return oldList.size();
        }
        
        /**
         * Возвращает размер нового списка.
         */
        @Override
        public int getNewListSize() {
            return newList.size();
        }
        
        /**
         * Проверяет, являются ли элементы одним и тем же объектом.
         * Сравнивает по ID - если ID совпадают, это один и тот же курс.
         * 
         * @param oldItemPosition Позиция в старом списке
         * @param newItemPosition Позиция в новом списке
         * @return true если это один и тот же элемент
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == 
                   newList.get(newItemPosition).getId();
        }
        
        /**
         * Проверяет, одинаково ли содержимое элементов.
         * Вызывается только если areItemsTheSame вернул true.
         * Если содержимое разное - элемент будет обновлён.
         * 
         * @param oldItemPosition Позиция в старом списке
         * @param newItemPosition Позиция в новом списке
         * @return true если содержимое одинаковое
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Course oldCourse = oldList.get(oldItemPosition);
            Course newCourse = newList.get(newItemPosition);
            
            // Сравниваем все поля, которые отображаются в UI
            return oldCourse.getTitle().equals(newCourse.getTitle()) &&
                   oldCourse.getProvider().equals(newCourse.getProvider()) &&
                   oldCourse.getDuration() == newCourse.getDuration() &&
                   oldCourse.getLevel().equals(newCourse.getLevel()) &&
                   oldCourse.isFavorite() == newCourse.isFavorite();
        }
    }
}

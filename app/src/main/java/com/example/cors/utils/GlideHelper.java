package com.example.cors.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cors.R;

/**
 * Утилитный класс для настройки Glide - библиотеки загрузки изображений.
 * 
 * Glide - это мощная библиотека для загрузки, кеширования и отображения изображений.
 * Этот helper предоставляет предустановленные конфигурации для различных сценариев.
 * 
 * Основные возможности Glide:
 * 1. Автоматическое кеширование (memory + disk)
 * 2. Lifecycle-aware (останавливает загрузку при pause)
 * 3. Оптимизация памяти (pooling, downsampling)
 * 4. Трансформации (resize, crop, blur и др.)
 * 5. Анимации (crossfade, custom transitions)
 * 6. Placeholder и error handling
 */
public class GlideHelper {
    
    /**
     * Загружает изображение для превью в списке (RecyclerView).
     * 
     * Оптимизация для списков:
     * - Малый размер (400x400px) для экономии памяти
     * - Быстрая загрузка (200ms fade)
     * - centerCrop для заполнения ImageView
     * - AUTOMATIC кеширование (Glide решает что кешировать)
     * 
     * Кеширование в Glide:
     * 1. Memory Cache (LruCache):
     *    - Хранит Bitmap в памяти
     *    - Мгновенный доступ (0ms)
     *    - Автоматически очищается при нехватке памяти
     * 
     * 2. Disk Cache (DiskLruCache):
     *    - Хранит на диске устройства
     *    - Работает offline после первой загрузки
     *    - Стратегия AUTOMATIC:
     *      * Кеширует оригинал если не было трансформаций
     *      * Кеширует результат если были трансформации (resize, crop)
     * 
     * 3. Resource Cache:
     *    - Кеширует drawable ресурсы (placeholder, error)
     *    - Не перезагружает их каждый раз
     * 
     * @param context Context для Glide
     * @param imageUrl URL изображения для загрузки
     * @param imageView ImageView для отображения результата
     */
    public static void loadThumbnail(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_course_placeholder)  // Пока загружается
                .error(R.drawable.ic_course_placeholder)        // При ошибке
                .centerCrop()                                    // Заполнить без искажений
                .override(400, 400)                              // Оптимальный размер для превью
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)  // Умное кеширование
                .transition(DrawableTransitionOptions.withCrossFade(200)) // Плавное появление
                .into(imageView);
    }
    
    /**
     * Загружает изображение в высоком качестве для Hero Header (экран деталей).
     * 
     * Оптимизация для header:
     * - Высокое качество (1200x800px) для Full HD экранов
     * - Медленная анимация (400ms) для premium эффекта
     * - centerCrop для заполнения всей области
     * - AUTOMATIC кеширование
     * 
     * Трансформация centerCrop в Glide:
     * 1. Масштабирует изображение чтобы ПОЛНОСТЬЮ заполнить ImageView
     * 2. Сохраняет пропорции (aspect ratio) оригинала
     * 3. Обрезает излишки по краям (crop)
     * 
     * Альтернативы:
     * - fitCenter: масштабирует чтобы поместиться, добавляет отступы
     * - centerInside: не масштабирует если изображение меньше ImageView
     * 
     * Почему centerCrop для hero header:
     * - Заполняет весь header без пустот
     * - Создает immersive эффект
     * - Работает с любыми пропорциями изображений
     * 
     * @param context Context для Glide
     * @param imageUrl URL изображения для загрузки
     * @param imageView ImageView для отображения результата
     */
    public static void loadHeaderImage(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_course_placeholder)  // Пока загружается
                .error(R.drawable.ic_course_placeholder)        // При ошибке
                .centerCrop()                                    // Заполнить весь header
                .override(1200, 800)                             // Высокое качество
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)  // Умное кеширование
                .transition(DrawableTransitionOptions.withCrossFade(400)) // Медленный fade для эффекта
                .into(imageView);
    }
    
    /**
     * Предзагружает изображения в кеш.
     * 
     * Используется для preloading изображений следующих элементов списка
     * пока пользователь скроллит. Когда он дойдет до элемента - изображение
     * уже будет в cache (мгновенное отображение).
     * 
     * Вызов:
     * <code>
     * GlideHelper.preloadImage(context, imageUrl);
     * </code>
     * 
     * @param context Context для Glide
     * @param imageUrl URL для предзагрузки
     */
    public static void preloadImage(Context context, String imageUrl) {
        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .preload(400, 400);  // Размер для thumbnail
    }
    
    /**
     * Очищает кеш Glide (memory + disk).
     * 
     * Использование:
     * - Опция "Очистить кеш" в настройках
     * - При нехватке места на устройстве
     * - При обновлении версии приложения
     * 
     * ВАЖНО: Вызывать в фоновом потоке (clearDiskCache блокирующий)!
     * 
     * @param context Context для Glide
     */
    public static void clearCache(Context context) {
        // Memory cache можно очистить в main thread
        Glide.get(context).clearMemory();
        
        // Disk cache нужно очищать в background thread
        new Thread(() -> {
            Glide.get(context).clearDiskCache();
        }).start();
    }
}

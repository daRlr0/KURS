package com.example.cors.utils;

import android.content.Context;

import com.example.cors.data.local.database.AppDatabase;
import com.example.cors.data.local.entity.CourseEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Утилитный класс для инициализации базы данных тестовыми данными.
 * 
 * Этот класс заполняет БД примерами курсов при первом запуске приложения.
 * В production приложении данные загружались бы с сервера.
 * 
 * Вызывается из Application класса или из главной Activity при первом запуске.
 */
public class DatabaseInitializer {
    
    /**
     * Инициализирует БД тестовыми данными.
     * Проверяет, есть ли уже данные в БД, и если нет - добавляет примеры курсов.
     * 
     * Метод выполняется асинхронно в фоновом потоке, чтобы не блокировать UI.
     * 
     * @param context Контекст приложения
     */
    public static void populateDatabase(Context context) {
        // Создаём executor для выполнения операций БД в фоновом потоке
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        executor.execute(() -> {
            // Получаем instance базы данных
            AppDatabase database = AppDatabase.getInstance(context);
            
            // Создаём массив тестовых курсов
            CourseEntity[] sampleCourses = createSampleCourses();
            
            // Вставляем курсы в БД
            // Room автоматически обработает конфликты благодаря OnConflictStrategy.REPLACE
            for (CourseEntity course : sampleCourses) {
                database.courseDao().insertCourse(course);
            }
        });
    }
    
    /**
     * Создаёт массив тестовых курсов для демонстрации работы приложения.
     * 
     * В реальном приложении эти данные приходили бы с сервера через Retrofit.
     * Здесь создаём hardcoded данные для offline режима и тестирования.
     * 
     * Изображения взяты из Unsplash - бесплатного источника качественных фото.
     * Параметры URL:
     * - ?auto=format&fit=crop - автоматическая оптимизация и обрезка
     * - &w=800&q=80 - ширина 800px, качество 80% (баланс качества и размера файла)
     * 
     * @return Массив CourseEntity с тестовыми данными
     */
    private static CourseEntity[] createSampleCourses() {
        return new CourseEntity[]{
                // Курс 1: Android разработка для начинающих
                new CourseEntity(
                        1,
                        "Android Development для начинающих",
                        "Udemy",
                        40,
                        "Beginner",
                        "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?auto=format&fit=crop&w=800&q=80",
                        "Полный курс Android разработки для начинающих. " +
                        "Изучите основы Java, XML layouts, Activity, Fragment, " +
                        "работу с базами данных и создание красивого UI. " +
                        "К концу курса вы создадите своё первое приложение для Android.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 2: Kotlin для Android разработчиков
                new CourseEntity(
                        2,
                        "Kotlin для Android разработчиков",
                        "Coursera",
                        30,
                        "Intermediate",
                        "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=800&q=80",
                        "Изучите современный язык программирования Kotlin для Android. " +
                        "Курс охватывает синтаксис Kotlin, coroutines, extensions, " +
                        "lambdas и другие продвинутые возможности языка. " +
                        "Практические задания помогут закрепить материал.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 3: MVVM архитектура в Android
                new CourseEntity(
                        3,
                        "MVVM архитектура и Clean Architecture",
                        "Udacity",
                        25,
                        "Advanced",
                        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80",
                        "Глубокое погружение в архитектурные паттерны Android приложений. " +
                        "Изучите MVVM, Clean Architecture, Dependency Injection, " +
                        "Unit Testing и лучшие практики разработки. " +
                        "Курс подходит для опытных разработчиков.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 4: Jetpack Compose
                new CourseEntity(
                        4,
                        "Jetpack Compose - Современный UI для Android",
                        "Google Codelabs",
                        20,
                        "Intermediate",
                        "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80",
                        "Научитесь создавать красивые UI с помощью Jetpack Compose - " +
                        "нового декларативного фреймворка от Google. " +
                        "Курс включает основы Compose, state management, " +
                        "navigation и интеграцию с существующими приложениями.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 5: Room Database
                new CourseEntity(
                        5,
                        "Room Database и Data Persistence",
                        "Udemy",
                        15,
                        "Beginner",
                        "https://images.unsplash.com/photo-1544256718-3bcf237f3974?auto=format&fit=crop&w=800&q=80",
                        "Полное руководство по работе с Room Database в Android. " +
                        "Изучите создание Entity, DAO, Database, миграции, " +
                        "работу с LiveData и Flow. Практические примеры " +
                        "помогут понять все аспекты локального хранения данных.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 6: Retrofit и работа с API
                new CourseEntity(
                        6,
                        "Retrofit 2 и работа с REST API",
                        "Pluralsight",
                        18,
                        "Intermediate",
                        "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80",
                        "Научитесь работать с сетевыми запросами в Android используя Retrofit. " +
                        "Курс охватывает GET/POST запросы, обработку JSON, " +
                        "работу с headers, аутентификацию, обработку ошибок " +
                        "и интеграцию с Coroutines.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 7: Material Design
                new CourseEntity(
                        7,
                        "Material Design в Android приложениях",
                        "Material.io",
                        12,
                        "Beginner",
                        "https://images.unsplash.com/photo-1561070791-2526d30994b5?auto=format&fit=crop&w=800&q=80",
                        "Создавайте красивые приложения следуя принципам Material Design. " +
                        "Изучите компоненты Material Design 3, color schemes, " +
                        "typography, animations и лучшие практики UX дизайна " +
                        "для мобильных приложений.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 8: Testing в Android
                new CourseEntity(
                        8,
                        "Unit Testing и UI Testing в Android",
                        "Test Automation University",
                        28,
                        "Advanced",
                        "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=800&q=80",
                        "Комплексный курс по тестированию Android приложений. " +
                        "Unit тесты с JUnit и Mockito, UI тесты с Espresso, " +
                        "Integration тесты, Test-Driven Development (TDD) " +
                        "и лучшие практики написания надёжного кода.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 9: Firebase для Android
                new CourseEntity(
                        9,
                        "Firebase - Backend для Android приложений",
                        "Firebase",
                        22,
                        "Intermediate",
                        "https://images.unsplash.com/photo-1551033406-611cf9a28f67?auto=format&fit=crop&w=800&q=80",
                        "Используйте Firebase как backend для вашего Android приложения. " +
                        "Курс включает Authentication, Firestore Database, " +
                        "Cloud Storage, Push Notifications, Analytics " +
                        "и другие сервисы Firebase.",
                        "",
                        0f,
                        false
                ),
                
                // Курс 10: Публикация в Google Play
                new CourseEntity(
                        10,
                        "Публикация приложения в Google Play",
                        "Google Play Academy",
                        10,
                        "Beginner",
                        "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?auto=format&fit=crop&w=800&q=80",
                        "Пошаговое руководство по публикации Android приложения. " +
                        "Подготовка release build, создание app bundle, " +
                        "настройка listing в консоли, работа с beta тестами, " +
                        "оптимизация под Play Store и продвижение приложения.",
                        "",
                        0f,
                        false
                )
        };
    }
}

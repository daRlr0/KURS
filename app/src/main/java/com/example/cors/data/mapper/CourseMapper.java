package com.example.cors.data.mapper;

import com.example.cors.data.local.entity.CourseEntity;
import com.example.cors.data.remote.dto.CourseDto;
import com.example.cors.domain.model.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс-маппер для конвертации данных между различными слоями приложения.
 */
public class CourseMapper {

    public static CourseEntity dtoToEntity(CourseDto dto) {
        CourseEntity entity = new CourseEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setProvider(dto.getProvider());
        entity.setDuration(dto.getDuration());
        entity.setLevel(dto.getLevel());
        entity.setImageUrl(dto.getImageUrl());
        entity.setDescription(dto.getDescription());
        
        // Поля, которые не приходят с сервера - инициализируем значениями по умолчанию
        entity.setComment(""); // Пустой комментарий
        entity.setUserRating(0f); // Оценка 0
        entity.setFavorite(false); // Не в избранном
        
        return entity;
    }
    

    public static List<CourseEntity> dtoListToEntityList(List<CourseDto> dtoList) {
        List<CourseEntity> entityList = new ArrayList<>();
        
        // Проходим по каждому DTO и конвертируем в Entity
        for (CourseDto dto : dtoList) {
            entityList.add(dtoToEntity(dto));
        }
        
        return entityList;
    }
    
    /**
     * Конвертирует Entity из БД в Domain модель для UI.
     * 
     * Это основной метод для подготовки данных к отображению.
     * Entity -> Domain Model изолирует UI от Room аннотаций и структуры БД.
     * 
     * @param entity Entity объект из Room БД
     * @return Domain модель для использования в UI
     */
    public static Course entityToDomain(CourseEntity entity) {
        Course course = new Course();
        course.setId(entity.getId());
        course.setTitle(entity.getTitle());
        course.setProvider(entity.getProvider());
        course.setDuration(entity.getDuration());
        course.setLevel(entity.getLevel());
        course.setImageUrl(entity.getImageUrl());
        course.setDescription(entity.getDescription());
        course.setComment(entity.getComment());
        course.setUserRating(entity.getUserRating());
        course.setFavorite(entity.isFavorite());
        
        return course;
    }
    
    /**
     * Конвертирует список Entity в список Domain моделей.
     * Используется для отображения списка курсов в RecyclerView.
     */
    public static List<Course> entityListToDomainList(List<CourseEntity> entityList) {
        List<Course> courseList = new ArrayList<>();
        
        // Проверка на null - защита от NPE если БД вернула null
        if (entityList == null) {
            return courseList; // Возвращаем пустой список
        }
        
        // Конвертируем каждый Entity в Domain модель
        for (CourseEntity entity : entityList) {
            courseList.add(entityToDomain(entity));
        }
        
        return courseList;
    }
    
    /**
     * Конвертирует Domain модель обратно в Entity.
     * Используется когда нужно сохранить изменения из UI обратно в БД.
     */
    public static CourseEntity domainToEntity(Course course) {
        CourseEntity entity = new CourseEntity();
        entity.setId(course.getId());
        entity.setTitle(course.getTitle());
        entity.setProvider(course.getProvider());
        entity.setDuration(course.getDuration());
        entity.setLevel(course.getLevel());
        entity.setImageUrl(course.getImageUrl());
        entity.setDescription(course.getDescription());
        entity.setComment(course.getComment());
        entity.setUserRating(course.getUserRating());
        entity.setFavorite(course.isFavorite());
        
        return entity;
    }
}

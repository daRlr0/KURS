package com.example.cors.data.remote.api;

import com.example.cors.data.remote.dto.CourseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit API интерфейс для работы с REST API курсов.
 * Retrofit автоматически генерирует реализацию этого интерфейса.
 * 
 * Каждый метод представляет HTTP запрос к серверу.
 */
public interface CourseApiService {
    
    /**
     * Получает список всех курсов с сервера.
     */
    @GET("courses")
    Call<List<CourseDto>> getCourses();
    
    /**
     * Получает детальную информацию о конкретном курсе.
     * 
     * @Path("id") - подставляет courseId в URL вместо {id}
     */
    @GET("courses/{id}")
    Call<CourseDto> getCourseById(@Path("id") int courseId);
    

}

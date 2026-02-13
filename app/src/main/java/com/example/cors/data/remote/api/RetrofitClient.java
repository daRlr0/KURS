package com.example.cors.data.remote.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.example.com/";
    
    /**
     * Singleton instance Retrofit клиента
     */
    private static Retrofit retrofit = null;
    
    /**
     * Приватный конструктор - запрещает создание экземпляров извне.
     * Это часть паттерна Singleton.
     */
    private RetrofitClient() {
    }
    
    /**
     * Получает экземпляр Retrofit клиента (Singleton pattern).
     * При первом вызове создаёт Retrofit, при последующих - возвращает существующий.
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Создаём Retrofit с настройками
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // GsonConverterFactory автоматически конвертирует JSON в Java объекты
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * Создаёт и возвращает экземпляр API сервиса.
     * Это удобный метод для быстрого получения API интерфейса.
     */
    public static CourseApiService getApiService() {
        return getRetrofitInstance().create(CourseApiService.class);
    }
}

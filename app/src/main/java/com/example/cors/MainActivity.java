package com.example.cors;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cors.ui.CoursesActivity;

/**
 * MainActivity - начальная Activity приложения.
 * Служит только для перенаправления на CoursesActivity.
 * Эта Activity оставлена для обратной совместимости и как точка входа.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * onCreate - вызывается при создании Activity.
     * Сразу перенаправляет пользователя на главный экран с курсами.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Создаём Intent для перехода на CoursesActivity
        Intent intent = new Intent(this, CoursesActivity.class);
        
        // Запускаем CoursesActivity
        startActivity(intent);
        
        // Закрываем MainActivity чтобы при нажатии "Назад" не вернуться сюда
        finish();
    }
}

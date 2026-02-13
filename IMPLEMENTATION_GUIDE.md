# 📖 Руководство по реализации - Каталог курсов

## Детальное описание архитектуры и реализации

### 🏛️ Архитектурные слои

#### 1. Data Layer (Слой данных)

##### Room Database (Локальное хранилище)

**CourseEntity.java** - Entity класс для Room
```
Поля:
- id: int (Primary Key, автогенерация)
- title: String (название курса)
- provider: String (платформа)
- duration: int (длительность в часах)
- level: String (уровень сложности)
- imageUrl: String (URL изображения)
- description: String (описание)
- comment: String (комментарий пользователя)
- userRating: float (оценка 0-5)
- isFavorite: boolean (в избранном)

Аннотации:
- @Entity(tableName = "courses")
- @PrimaryKey(autoGenerate = true)
```

**CourseDao.java** - Data Access Object
```
Основные методы:
1. insertCourse() - вставка курса
2. insertCourses() - массовая вставка
3. updateCourse() - обновление курса
4. getAllCourses() - все курсы (LiveData)
5. getFavoriteCourses() - избранные (LiveData)
6. searchCourses(query) - поиск по названию
7. getCoursesByLevel(level) - фильтр по уровню
8. getCourseById(id) - курс по ID
9. updateFavoriteStatus() - обновление избранного
10. updateCourseReview() - обновление отзыва

Особенности:
- Все query методы возвращают LiveData для реактивного UI
- Использование LIKE для поиска
- COLLATE NOCASE для поиска без учёта регистра
```

**AppDatabase.java** - Singleton класс БД
```
Паттерн: Singleton с double-checked locking
Методы:
- getInstance(Context) - получение instance
- courseDao() - абстрактный метод для DAO
- destroyInstance() - очистка instance (для тестов)

Конфигурация:
- version = 1
- fallbackToDestructiveMigration() - пересоздание при миграции
```

##### Retrofit (Сетевой слой)

**CourseDto.java** - Data Transfer Object
```
Поля соответствуют JSON с сервера
Использует @SerializedName для маппинга полей
Не содержит Room аннотаций (изоляция слоёв)
```

**CourseApiService.java** - API интерфейс
```
Методы:
- getCourses() - GET /courses
- getCourseById(id) - GET /courses/{id}

Возвращают Call<T> для асинхронных запросов
```

**RetrofitClient.java** - Singleton Retrofit instance
```
Конфигурация:
- BASE_URL (заглушка для демо)
- GsonConverterFactory для JSON
- Singleton pattern для переиспользования
```

##### Repository Pattern

**CourseRepository.java** - Единая точка доступа к данным
```
Стратегия работы:
1. Возвращаем LiveData из Room (быстро, offline)
2. Параллельно запрашиваем данные с API
3. Обновляем Room при успешном ответе
4. LiveData автоматически уведомляет UI

Методы:
- getAllCourses() - с автообновлением с API
- getFavoriteCourses() - только из БД
- searchCourses(query)
- getCoursesByLevel(level)
- getCourseById(id)
- updateFavoriteStatus()
- saveCourseReview()
- refreshCoursesFromApi() - private

LiveData для состояний:
- loadingLiveData - индикатор загрузки
- errorLiveData - сообщения об ошибках

ExecutorService для операций БД в фоне
```

##### Mapper Pattern

**CourseMapper.java** - Конвертация между моделями
```
Методы:
1. dtoToEntity(CourseDto) → CourseEntity
   - Для сохранения данных с API в БД
   
2. dtoListToEntityList(List<CourseDto>) → List<CourseEntity>
   - Массовая конвертация
   
3. entityToDomain(CourseEntity) → Course
   - Для отображения в UI
   
4. entityListToDomainList(List<CourseEntity>) → List<Course>
   - Конвертация списков для RecyclerView
   
5. domainToEntity(Course) → CourseEntity
   - Для сохранения изменений из UI в БД

Преимущества:
- Изоляция слоёв (UI не знает о Room)
- Возможность добавить бизнес-логику
- Легко тестировать
```

#### 2. Domain Layer (Бизнес-логика)

**Course.java** - Domain модель
```
Чистая Java модель без аннотаций
Используется в UI слое

Дополнительные методы:
- getFormattedDuration() - "40 часов"
- getLocalizedLevel() - "Начальный"

Изоляция от Room и Retrofit
```

#### 3. ViewModel Layer

**CoursesViewModel.java** - ViewModel главного экрана
```
Наследует: AndroidViewModel (для Application context)

LiveData:
- coursesLiveData: MediatorLiveData<List<Course>>
  (переключается между разными источниками)
- uiStateLiveData: MutableLiveData<UiState>
  (Loading/Empty/Success/Error)
- searchQueryLiveData: String (текущий запрос)
- selectedLevelLiveData: String (выбранный фильтр)

Методы:
- loadAllCourses() - загрузка всех
- searchCourses(query) - поиск
- filterByLevel(level) - фильтрация
- toggleFavorite(course) - изменение избранного
- switchSource(LiveData) - переключение источника данных

MediatorLiveData позволяет:
- Подписываться на несколько источников
- Переключаться между ними динамически
- Объединять данные из разных источников
```

**CourseDetailViewModel.java** - ViewModel деталей
```
Методы:
- loadCourse(id) - загрузка курса по ID
- toggleFavorite(isFavorite) - изменение избранного
- saveCourseReview(comment, rating) - сохранение отзыва

Валидация:
- Проверка null для комментария
- Ограничение рейтинга 0-5
```

**FavoritesViewModel.java** - ViewModel избранного
```
Методы:
- getFavoritesLiveData() - список избранных
- removeFromFavorites(course) - удаление
- determineUiState(courses) - определение состояния UI

Особенности:
- Полностью offline режим
- Не делает сетевых запросов
```

#### 4. UI Layer

##### Activities

**CoursesActivity.java** - Главный экран
```
ViewBinding: ActivityCoursesBinding

Функционал:
1. RecyclerView с курсами
2. SearchView в ActionBar (живой поиск)
3. Material Chips для фильтрации
4. FAB для перехода к избранному
5. Обработка состояний UI

Observer подписки:
- coursesLiveData → обновление adapter
- uiStateLiveData → показ/скрытие компонентов
- errorLiveData → Toast с ошибками

Состояния UI:
- LOADING: показываем ProgressBar
- EMPTY: показываем Empty State
- SUCCESS: показываем RecyclerView
- ERROR: показываем сообщение
```

**CourseDetailActivity.java** - Экран деталей
```
ViewBinding: ActivityCourseDetailBinding

Компоненты:
1. CollapsingToolbar с изображением
2. Информация о курсе
3. RatingBar для оценки
4. EditText для комментария
5. FAB для избранного

Glide для загрузки изображений:
- placeholder() - показываем пока загружается
- error() - показываем при ошибке
- centerCrop() - обрезка по центру
```

**FavoritesActivity.java** - Экран избранного
```
ViewBinding: ActivityFavoritesBinding

Особенности:
- Переиспользуем CourseAdapter
- Empty State с подсказкой
- Удаление свайпом или кликом
- Полностью offline
```

##### Adapters

**CourseAdapter.java** - RecyclerView Adapter
```
Паттерны:
- ViewHolder для кеширования View
- DiffUtil для эффективных обновлений

Методы:
- setCourses(List) - обновление с DiffUtil
- setOnCourseClickListener() - клик на элемент
- setOnFavoriteClickListener() - клик на избранное

ViewHolder:
- Кеширует ссылки на View
- Устанавливает listeners один раз
- bind(Course) для заполнения данными

DiffUtil.Callback:
- areItemsTheSame() - сравнение по ID
- areContentsTheSame() - сравнение содержимого
- Автоматические анимации при изменениях
```

### 🎨 Material Design компоненты

#### Layouts

**activity_courses.xml**
```
Структура:
CoordinatorLayout
  ├─ AppBarLayout (Toolbar)
  ├─ NestedScrollView
  │   ├─ HorizontalScrollView (Chips)
  │   ├─ ProgressBar
  │   ├─ RecyclerView
  │   └─ Empty State
  └─ FloatingActionButton

Material компоненты:
- Toolbar с SearchView
- ChipGroup (singleSelection)
- MaterialCardView в элементах списка
```

**item_course.xml**
```
MaterialCardView
  └─ ConstraintLayout
      ├─ ImageView (обложка 80x80dp)
      ├─ TextView (название)
      ├─ TextView (провайдер)
      ├─ TextView (длительность с иконкой)
      ├─ TextView (уровень с иконкой)
      └─ ImageButton (избранное)

Особенности:
- cardCornerRadius="12dp"
- cardElevation="4dp"
- ripple effect на клике
```

**activity_course_detail.xml**
```
CoordinatorLayout
  ├─ AppBarLayout
  │   └─ CollapsingToolbarLayout
  │       ├─ ImageView (header)
  │       └─ Toolbar
  ├─ NestedScrollView
  │   ├─ MaterialCardView (информация)
  │   └─ MaterialCardView (отзыв)
  └─ FloatingActionButton

Эффекты:
- Parallax scroll для изображения
- Collapsing toolbar при прокрутке
```

### 📱 Обработка состояний UI

#### UiState Enum
```java
enum UiState {
    LOADING,  // ProgressBar visible
    SUCCESS,  // RecyclerView visible
    EMPTY,    // Empty State visible
    ERROR     // Error message
}
```

#### Логика обработки состояний

```java
// ViewModel определяет состояние на основе данных
private void updateUiState(List<Course> courses) {
    if (courses == null) {
        uiStateLiveData.setValue(UiState.LOADING);
    } else if (courses.isEmpty()) {
        uiStateLiveData.setValue(UiState.EMPTY);
    } else {
        uiStateLiveData.setValue(UiState.SUCCESS);
    }
}

// Activity подписывается и обновляет UI
viewModel.getUiStateLiveData().observe(this, state -> {
    updateUiState(state);
});

private void updateUiState(UiState state) {
    // Скрываем всё
    progressBar.setVisibility(View.GONE);
    emptyState.setVisibility(View.GONE);
    recyclerView.setVisibility(View.GONE);
    
    // Показываем нужное
    switch (state) {
        case LOADING: progressBar.setVisibility(View.VISIBLE); break;
        case EMPTY: emptyState.setVisibility(View.VISIBLE); break;
        case SUCCESS: recyclerView.setVisibility(View.VISIBLE); break;
    }
}
```

### 🔄 Поток данных

#### 1. Загрузка всех курсов
```
User открывает app
    ↓
CoursesActivity.onCreate()
    ↓
ViewModel.loadAllCourses()
    ↓
Repository.getAllCourses()
    ├─ Возвращает LiveData<List<Course>> из Room
    └─ Параллельно: API запрос → сохранение в Room
    ↓
Room обновляет LiveData
    ↓
ViewModel получает данные через Transformations.map()
    ↓
Activity.observe() получает List<Course>
    ↓
Adapter.setCourses() обновляет RecyclerView
```

#### 2. Поиск курсов
```
User вводит текст в SearchView
    ↓
SearchView.OnQueryTextListener
    ↓
ViewModel.searchCourses(query)
    ↓
ViewModel.switchSource() переключает на searchCourses LiveData
    ↓
MediatorLiveData отписывается от старого источника
    ↓
MediatorLiveData подписывается на новый источник
    ↓
Room выполняет LIKE запрос
    ↓
Activity получает отфильтрованный список
```

#### 3. Добавление в избранное
```
User кликает на кнопку избранного
    ↓
Adapter.OnFavoriteClickListener
    ↓
ViewModel.toggleFavorite(course)
    ↓
Repository.updateFavoriteStatus(id, newStatus)
    ↓
ExecutorService.execute() в фоновом потоке
    ↓
DAO.updateFavoriteStatus() обновляет БД
    ↓
Room автоматически обновляет LiveData
    ↓
Adapter получает обновлённый список
    ↓
DiffUtil вычисляет изменения
    ↓
RecyclerView анимирует обновление иконки
```

### 🛡️ Обработка ошибок

#### Уровни обработки

1. **Repository level**
```java
call.enqueue(new Callback<List<CourseDto>>() {
    @Override
    public void onFailure(Call call, Throwable t) {
        errorLiveData.postValue("Ошибка сети: " + t.getMessage());
        loadingLiveData.postValue(false);
    }
    
    @Override
    public void onResponse(Response response) {
        if (!response.isSuccessful()) {
            errorLiveData.postValue("Ошибка сервера: " + response.code());
        }
    }
});
```

2. **ViewModel level**
```java
repository.getErrorLiveData().observe(this, error -> {
    if (error != null) {
        // Пробрасываем ошибку в UI
    }
});
```

3. **UI level**
```java
viewModel.getErrorLiveData().observe(this, error -> {
    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
});
```

### ⚡ Оптимизации

#### 1. Room
- Автогенерация ID (не нужно синхронизировать с сервером)
- LiveData (автоматическая отписка при уничтожении Activity)
- OnConflictStrategy.REPLACE (избегаем дубликатов)

#### 2. RecyclerView
- ViewHolder pattern (переиспользование View)
- DiffUtil (обновляем только изменённые элементы)
- setHasStableIds (оптимизация анимаций)

#### 3. Glide
- Автоматическое кеширование изображений
- Уменьшение размера в памяти
- Lifecycle aware (останавливает загрузку при pause)

#### 4. ViewModel
- Переживает rotation (не теряем данные)
- Не содержит ссылок на View (нет утечек)
- onCleared() для cleanup ресурсов

### 🧪 Тестируемость

Архитектура позволяет легко тестировать каждый слой:

1. **Unit tests для Repository**
   - Mock DAO и API service
   - Тестируем логику обработки данных

2. **Unit tests для ViewModel**
   - Mock Repository
   - Тестируем UI логику

3. **UI tests для Activity**
   - Espresso для взаимодействия с UI
   - Тестируем navigation и отображение

4. **Integration tests**
   - Тестируем взаимодействие слоёв
   - Room In-Memory database для тестов

### 📚 Ключевые концепции

1. **Separation of Concerns** - каждый класс отвечает за одну задачу
2. **Single Responsibility** - один класс = одна ответственность
3. **Dependency Inversion** - зависимости через интерфейсы
4. **Offline First** - приложение работает без интернета
5. **Reactive UI** - LiveData автоматически обновляет UI
6. **Material Design** - следуем гайдлайнам Google

### 🎓 Обучающие комментарии

Весь код содержит:
- Javadoc для классов и методов
- Inline комментарии для сложной логики
- Объяснения архитектурных решений
- Примеры использования
- Ссылки на паттерны и best practices

Это делает код отличным учебным материалом для изучения Android разработки!

package com.example.cors.data;

/**
 * Resource<T> - универсальная обертка для данных с состояниями Loading/Success/Error.
 * 
 * Это класс-обертка (wrapper) который используется в MVVM архитектуре для
 * представления трех возможных состояний любой операции с данными:
 * - LOADING: операция выполняется (показываем ProgressBar)
 * - SUCCESS: операция успешна (показываем данные)
 * - ERROR: произошла ошибка (показываем сообщение об ошибке)
 * 
 * Паттерн называется LCE (Loading-Content-Error) или Resource Pattern.
 * 
 * Преимущества использования Resource<T>:
 * 1. Единообразная обработка состояний во всем приложении
 * 2. Type-safe доступ к данным и ошибкам
 * 3. Невозможно получить данные в состоянии ошибки (компилятор защитит)
 * 4. Упрощает код в UI слое (один observer вместо нескольких)
 * 
 * @param <T> Тип данных, которые оборачиваются (например, List<Course>)
 */
public class Resource<T> {
    
    /**
     * Текущее состояние ресурса
     */
    private final Status status;
    
    /**
     * Данные (null если status != SUCCESS)
     */
    private final T data;
    
    /**
     * Сообщение об ошибке (null если status != ERROR)
     */
    private final String message;
    
    /**
     * Приватный конструктор - используйте статические методы success/error/loading
     * для создания экземпляров Resource.
     * 
     * @param status Состояние ресурса
     * @param data Данные (может быть null)
     * @param message Сообщение об ошибке (может быть null)
     */
    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
    
    /**
     * Создает Resource в состоянии SUCCESS с данными.
     * Используется когда операция успешно завершена и есть данные для отображения.
     * 
     * Пример использования в Repository:
     * <code>
     * return Resource.success(coursesList);
     * </code>
     * 
     * @param data Данные для обертывания
     * @param <T> Тип данных
     * @return Resource в состоянии SUCCESS
     */
    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }
    
    /**
     * Создает Resource в состоянии ERROR с сообщением об ошибке.
     * Используется когда операция завершилась с ошибкой.
     * 
     * Можно передать частичные данные (например, закешированные данные)
     * чтобы показать их пользователю даже при ошибке обновления.
     * 
     * Пример использования:
     * <code>
     * return Resource.error("Ошибка сети", cachedData);
     * </code>
     * 
     * @param message Сообщение об ошибке для пользователя
     * @param data Частичные данные (может быть null)
     * @param <T> Тип данных
     * @return Resource в состоянии ERROR
     */
    public static <T> Resource<T> error(String message, T data) {
        return new Resource<>(Status.ERROR, data, message);
    }
    
    /**
     * Создает Resource в состоянии LOADING.
     * Используется когда операция выполняется (загрузка данных с API, из БД и т.д.).
     * 
     * Можно передать частичные данные (например, старые данные)
     * чтобы показать их во время загрузки новых.
     * 
     * Пример использования:
     * <code>
     * // Показываем ProgressBar
     * _coursesLiveData.postValue(Resource.loading(null));
     * 
     * // Или показываем старые данные + ProgressBar
     * _coursesLiveData.postValue(Resource.loading(oldData));
     * </code>
     * 
     * @param data Частичные данные (обычно null или старые данные)
     * @param <T> Тип данных
     * @return Resource в состоянии LOADING
     */
    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }
    
    /**
     * Возвращает текущее состояние ресурса.
     * 
     * Используется в UI для определения что показывать:
     * <code>
     * switch (resource.getStatus()) {
     *     case LOADING: showProgressBar(); break;
     *     case SUCCESS: showData(resource.getData()); break;
     *     case ERROR: showError(resource.getMessage()); break;
     * }
     * </code>
     * 
     * @return Текущее состояние (LOADING, SUCCESS или ERROR)
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Возвращает данные из ресурса.
     * 
     * ВАЖНО: Проверяйте status перед использованием data!
     * data может быть null в состояниях LOADING и ERROR.
     * 
     * @return Данные или null
     */
    public T getData() {
        return data;
    }
    
    /**
     * Возвращает сообщение об ошибке.
     * 
     * Не null только в состоянии ERROR.
     * Используется для показа Toast или Snackbar с ошибкой.
     * 
     * @return Сообщение об ошибке или null
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Enum для состояний ресурса.
     * 
     * Три возможных состояния:
     * - SUCCESS: данные успешно загружены
     * - ERROR: произошла ошибка
     * - LOADING: идет загрузка
     */
    public enum Status {
        /**
         * Данные успешно загружены.
         * data != null, message == null
         */
        SUCCESS,
        
        /**
         * Произошла ошибка при загрузке.
         * message != null, data может быть null или содержать старые данные
         */
        ERROR,
        
        /**
         * Идет загрузка данных.
         * data может быть null или содержать старые данные, message == null
         */
        LOADING
    }
}

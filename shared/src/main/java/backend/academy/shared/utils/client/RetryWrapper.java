package backend.academy.shared.utils.client;

import backend.academy.shared.exceptions.ApiCallException;
import java.util.concurrent.Callable;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;

/** Класс предоставляет удобный интерфейс для повторения запросов http-клиентов. */
public class RetryWrapper {

    private static final int DEFAULT_RETRIES = 3;

    private RetryWrapper() {}

    /**
     * Повторяет запрос {@code DEFAULT_RETRIES} раз.
     *
     * <p>Повторение происходит только в случае истечения таймаумов запроса, или если сервис вернул ответ с 5xx кодом.
     *
     * @param callable Запрос, который нужно повторить
     * @return Результат запроса
     * @param <T> Тип результата запроса
     */
    public static <T> T retry(Callable<T> callable) {
        return retry(callable, DEFAULT_RETRIES);
    }

    /**
     * Повторяет запрос {@code retryCount} раз.
     *
     * <p>Повторение происходит только в случае истечения таймаумов запроса, или если сервис вернул ответ с 5xx кодом.
     *
     * @param callable Запрос, который нужно повторить
     * @param retryCount Количество повторений запроса
     * @return Результат запроса
     * @param <T> Тип результата запроса
     */
    @SuppressWarnings("EmptyCatch")
    public static <T> T retry(Callable<T> callable, int retryCount) {
        int count = 1;
        while (count < retryCount) {
            try {
                return callable.call();
            } catch (ApiCallException e) {
                if (!HttpStatus.valueOf(e.code()).is5xxServerError()) {
                    throw e;
                }
            } catch (ResourceAccessException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ++count;
        }
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

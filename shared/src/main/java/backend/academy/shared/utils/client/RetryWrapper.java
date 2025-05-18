package backend.academy.shared.utils.client;

import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.exceptions.NotRetryApiCallException;
import java.util.concurrent.Callable;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/** Класс предоставляет удобный интерфейс для повторения запросов http-клиентов. */
@Component
public class RetryWrapper {

    /**
     * Повторяет запрос {@code retryCount} раз.
     *
     * <p>Повторение происходит в случае истечения таймаумов запроса, или если сервис вернул ответ с 5xx кодом.
     *
     * @param callable Запрос, который нужно повторить
     * @return Результат запроса
     * @param <T> Тип результата запроса
     */
    @Retryable(
            maxAttemptsExpression = "${app.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${app.retry.backoff:500}"),
            noRetryFor = NotRetryApiCallException.class)
    public <T> T retry(Callable<T> callable) {
        try {
            return callable.call();
        } catch (ApiCallException e) {
            HttpStatus responseStatus = HttpStatus.valueOf(e.code());
            if (!responseStatus.is5xxServerError() && responseStatus.value() != HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new NotRetryApiCallException(e);
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

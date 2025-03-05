package backend.academy.scrapper.utils;

import backend.academy.shared.exceptions.BaseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Класс предназначен для того, чтобы было удобнее доставать содержимое Future. Класс предоставляет метод unwrap. Он
 * проверяет наличие во Future исключения и если оно есть - превращает в RuntimeException. Если же исключения нет, то
 * возвращается результат выполнения Future.
 */
public class FutureUnwrapper {

    public static <T> T unwrap(Future<T> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw switch (e.getCause()) {
                case BaseException baseException -> baseException;
                default -> throw new RuntimeException(e.getCause());
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package backend.academy.scrapper.utils;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.shared.exceptions.BaseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

public class FutureUnwrapperTest {

    @Test
    public void valueUnwrapTest() {
        int expectedResult = 1;
        Future<Integer> future = CompletableFuture.completedFuture(expectedResult);

        int actualResult = unwrap(future);

        assertThat(expectedResult).isEqualTo(actualResult);
    }

    @Test
    public void baseExceptionUnwrapTest() {
        BaseException baseException = new BaseException("test");
        Future<Integer> future = CompletableFuture.failedFuture(baseException);

        assertThatThrownBy(() -> unwrap(future)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> unwrap(future)).hasMessage("test");
    }

    @Test
    public void runtimeExceptionUnwrapTest() {
        Exception exception = new Exception("test");
        Future<Integer> future = CompletableFuture.failedFuture(exception);

        assertThatThrownBy(() -> unwrap(future)).isInstanceOf(RuntimeException.class);
    }
}

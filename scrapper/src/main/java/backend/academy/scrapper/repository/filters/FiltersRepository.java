package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FiltersRepository {

    CompletableFuture<List<Filter>> getAllByDataId(long dataId);

    CompletableFuture<Void> create(Filter filter);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> deleteAllByDataId(long dataId);
}

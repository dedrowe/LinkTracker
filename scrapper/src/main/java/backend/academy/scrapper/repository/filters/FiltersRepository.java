package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import java.util.List;

public interface FiltersRepository {

    <T extends Filter> List<T> getAllByDataId(long dataId);

    void create(Filter filter);

    void deleteById(long id);

    void deleteAllByDataId(long dataId);
}

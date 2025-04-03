package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FiltersService {

    private final FiltersRepository filtersRepository;

    public List<Filter> getAllByDataIdSync(long dataId) {
        return unwrap(filtersRepository.getAllByDataId(dataId));
    }

    public CompletableFuture<List<Filter>> getAllByDataId(long dataId) {
        return filtersRepository.getAllByDataId(dataId);
    }

    @Transactional
    public void createAllSync(List<String> filters, long linkDataId) {
        unwrap(createAll(filters, linkDataId));
    }

    @Transactional
    public CompletableFuture<Void> createAll(List<String> filters, long linkDataId) {
        List<Filter> curFilters = unwrap(filtersRepository.getAllByDataId(linkDataId));
        Set<String> filtersSet = new HashSet<>(filters);
        Set<Filter> curFiltersSet = new HashSet<>(curFilters);

        for (Filter filter : Set.copyOf(curFiltersSet)) {
            if (filtersSet.contains(filter.filter())) {
                curFiltersSet.remove(filter);
                filtersSet.remove(filter.filter());
            }
        }

        CompletableFuture<Void> deleteFiltersFuture = CompletableFuture.allOf(curFiltersSet.stream()
                .map(filter -> filtersRepository.deleteById(filter.id()))
                .toArray(CompletableFuture[]::new));

        CompletableFuture<Void> createFiltersFuture = CompletableFuture.allOf(filtersSet.stream()
                .map(filter -> filtersRepository.create(new Filter(linkDataId, filter)))
                .toArray(CompletableFuture[]::new));

        unwrap(CompletableFuture.allOf(createFiltersFuture, deleteFiltersFuture));
        return CompletableFuture.completedFuture(null);
    }

    public void deleteByIdSync(long id) {
        unwrap(filtersRepository.deleteById(id));
    }

    public CompletableFuture<Void> deleteById(long id) {
        return filtersRepository.deleteById(id);
    }

    public void deleteAllByDataIdSync(long dataId) {
        unwrap(filtersRepository.deleteAllByDataId(dataId));
    }

    public CompletableFuture<Void> deleteAllByDataId(long dataId) {
        return filtersRepository.deleteAllByDataId(dataId);
    }
}

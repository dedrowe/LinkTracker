package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.service.entityFactory.filter.FilterFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FiltersService {

    private final FiltersRepository filtersRepository;

    private final FilterFactory filterFactory;

    public List<Filter> getAllByDataId(long dataId) {
        return filtersRepository.getAllByDataId(dataId);
    }

    @Transactional
    public void createAll(List<String> filters, LinkData linkData) {
        List<Filter> curFilters = filtersRepository.getAllByDataId(linkData.id());
        Set<String> filtersSet = new HashSet<>(filters);
        Set<Filter> curFiltersSet = new HashSet<>(curFilters);

        for (Filter filter : Set.copyOf(curFiltersSet)) {
            if (filtersSet.contains(filter.filter())) {
                curFiltersSet.remove(filter);
                filtersSet.remove(filter.filter());
            }
        }

        curFiltersSet.forEach(filter -> filtersRepository.deleteById(filter.id()));
        filtersSet.forEach(filter -> filtersRepository.create(filterFactory.getFilter(linkData, filter)));
    }

    public void deleteById(long id) {
        filtersRepository.deleteById(id);
    }

    public void deleteAllByDataId(long dataId) {
        filtersRepository.deleteAllByDataId(dataId);
    }
}

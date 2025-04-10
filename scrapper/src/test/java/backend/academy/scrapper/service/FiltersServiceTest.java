package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.jdbc.JdbcFilter;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import backend.academy.scrapper.entity.jpa.JpaFilter;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.service.entityFactory.filter.FilterFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FiltersServiceTest {

    @Mock
    private FiltersRepository filtersRepository;

    @Mock
    private FilterFactory filterFactory;

    @InjectMocks
    private FiltersService filtersService;

    @Test
    public void createAllTest() {
        String filter1 = "1:1";
        String filter2 = "2:2";
        String filter3 = "3:3";

        List<String> newFilters = List.of(filter2, filter3);
        JdbcLinkData data = new JdbcLinkData(1L, 1L, 1L);
        when(filtersRepository.getAllByDataId(anyLong()))
                .thenReturn(List.of(new JdbcFilter(1L, 1L, filter1), new JdbcFilter(2L, 1L, filter2)));
        when(filterFactory.getFilter(any(), anyString())).thenReturn(new JpaFilter());

        filtersService.createAll(newFilters, data);

        verify(filtersRepository).getAllByDataId(anyLong());
        verify(filtersRepository, times(1)).create(any());
        verify(filtersRepository, times(1)).deleteById(anyLong());
    }
}

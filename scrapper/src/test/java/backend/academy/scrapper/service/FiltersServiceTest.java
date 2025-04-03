package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FiltersServiceTest {

    @Mock
    private FiltersRepository filtersRepository;

    @InjectMocks
    private FiltersService filtersService;

    @Test
    public void createAllTest() {
        String filter1 = "1:1";
        String filter2 = "2:2";
        String filter3 = "3:3";

        List<String> newFilters = List.of(filter2, filter3);
        long dataId = 1L;
        when(filtersRepository.getAllByDataId(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(
                        List.of(new Filter(1L, 1L, filter1), new Filter(2L, 1L, filter2))));
        when(filtersRepository.deleteById(anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(filtersRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));

        filtersService.createAllSync(newFilters, dataId);

        verify(filtersRepository).getAllByDataId(anyLong());
        verify(filtersRepository, times(1)).create(any());
        verify(filtersRepository, times(1)).deleteById(anyLong());
    }
}

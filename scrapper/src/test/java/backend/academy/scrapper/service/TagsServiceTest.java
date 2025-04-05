package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import backend.academy.scrapper.repository.tags.TagsRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagsServiceTest {

    @Mock
    private TagsRepository tagsRepository;

    @InjectMocks
    private TagsService tagsService;

    @Test
    public void createAllTest() {
        String tag1 = "tag1";
        String tag2 = "tag2";
        String tag3 = "tag3";

        List<String> newTags = List.of(tag2, tag3);
        JdbcLinkData data = new JdbcLinkData(1L, 1L, 1L);
        when(tagsRepository.getAllByDataId(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(List.of(new Tag(1L, tag1), new Tag(2L, tag2))));
        when(tagsRepository.getAllByTagsSet(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(new Tag(2L, tag2))));
        when(tagsRepository.createRelation(anyLong(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsRepository.deleteRelation(anyLong(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsRepository.createTag(any())).then(a -> {
            Tag argument = a.getArgument(0);
            argument.id(1L);
            return CompletableFuture.completedFuture(null);
        });

        tagsService.createAllSync(newTags, data);

        verify(tagsRepository).getAllByDataId(anyLong());
        verify(tagsRepository).getAllByTagsSet(any());
        verify(tagsRepository, times(1)).deleteRelation(anyLong(), anyLong());
        verify(tagsRepository, times(2)).createRelation(anyLong(), anyLong());
        verify(tagsRepository, times(1)).createTag(any());
    }
}

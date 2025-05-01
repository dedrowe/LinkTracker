package backend.academy.scrapper.service;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.service.botClient.TgBotClientWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UpdatesSendService {

    private final int batchSize;

    private final OutboxRepository repository;

    private final TgBotClientWrapper clientWrapper;

    private final LinkMapper linkMapper;

    @Autowired
    public UpdatesSendService(
        ScrapperConfig config, OutboxRepository repository,
        TgBotClientWrapper clientWrapper,
        LinkMapper linkMapper) {
        batchSize = config.updatesSender().batchSize();
        this.repository = repository;
        this.clientWrapper = clientWrapper;
        this.linkMapper = linkMapper;
    }

    @Transactional
    public boolean sendUpdates() {
        List<Outbox> outboxList = repository.getAllWithDeletion(batchSize);
        if (outboxList.isEmpty()) {
            return false;
        }

        Map<OutboxGrouping, List<Long>> outboxMap = outboxList.stream()
            .collect(Collectors.groupingBy(
                outbox -> new OutboxGrouping(outbox.linkId(), outbox.link(), outbox.description()),
                Collectors.mapping(Outbox::chatId, Collectors.toList())));

        for (Map.Entry<OutboxGrouping, List<Long>> entry : outboxMap.entrySet()) {
            clientWrapper.sendUpdates(linkMapper.createLinkUpdate(
                entry.getKey().linkId(),
                entry.getKey().link(),
                "Получено обновление по ссылке " + entry.getKey().link() + "\n"
                    + entry.getKey().description(),
                entry.getValue()));
        }
        return true;
    }

    private record OutboxGrouping(long linkId, String link, String description) {}
}

package backend.academy.scrapper.service;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.service.botClient.TgBotClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class UpdatesSendScheduler {

    private final int batchSize;

    private final OutboxRepository repository;

    private final TgBotClient tgBotClient;

    private final LinkMapper linkMapper;

    @Autowired
    public UpdatesSendScheduler(
            ScrapperConfig config, OutboxRepository repository, TgBotClient tgBotClient, LinkMapper linkMapper) {
        batchSize = config.updatesSender().batchSize();
        this.repository = repository;
        this.tgBotClient = tgBotClient;
        this.linkMapper = linkMapper;
    }

    @Transactional
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void sendUpdates() {
        while (true) {
            List<Outbox> outboxList = repository.getAllWithDeletion(batchSize);
            if (outboxList.isEmpty()) {
                break;
            }

            Map<OutboxGrouping, List<Long>> outboxMap = outboxList.stream()
                    .collect(Collectors.groupingBy(
                            outbox -> new OutboxGrouping(outbox.linkId(), outbox.link(), outbox.description()),
                            Collectors.mapping(Outbox::chatId, Collectors.toList())));

            for (Map.Entry<OutboxGrouping, List<Long>> entry : outboxMap.entrySet()) {
                tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                        entry.getKey().linkId(),
                        entry.getKey().link(),
                        "Получено обновление по ссылке " + entry.getKey().link() + "\n"
                                + entry.getKey().description(),
                        entry.getValue()));
            }
        }
    }

    private record OutboxGrouping(long linkId, String link, String description) {}
}

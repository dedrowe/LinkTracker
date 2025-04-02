package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.LinkRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UpdatesCheckScheduler {

    public static final int DEFAULT_BATCH_SIZE = 200;

    public static final int DEFAULT_THREAD_POOL_SIZE = 4;

    protected final int batchSize;

    private final LinkRepository linkRepository;

    private final Duration linksCheckInterval;

    private final ExecutorService executorService;

    private final LinksCheckerService checker;

    @Autowired
    public UpdatesCheckScheduler(LinkRepository linkRepository, ScrapperConfig config, LinksCheckerService checker) {
        this.linkRepository = linkRepository;
        batchSize = config.updatesChecker().batchSize();
        linksCheckInterval = Duration.ofSeconds(config.updatesChecker().checkIntervalSeconds());
        executorService = Executors.newFixedThreadPool(config.updatesChecker().threadsCount());
        this.checker = checker;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void checkUpdates() {
        while (true) {
            try {
                List<Link> links = unwrap(linkRepository.getAllNotChecked(
                        batchSize, LocalDateTime.now(ZoneOffset.UTC), linksCheckInterval.getSeconds()));
                if (links.isEmpty()) {
                    break;
                }

                List<Future<Void>> results = executorService.invokeAll(links.stream()
                        .map(link -> (Callable<Void>) () -> {
                            checker.checkUpdatesForLink(link);
                            return null;
                        })
                        .toList());
                for (int i = 0; i < results.size(); i++) {
                    try {
                        results.get(i).get();
                    } catch (ExecutionException | InterruptedException e) {
                        MDC.put("link", links.get(i).link());
                        log.error("Произошла ошибка при получении обновлений", e.getCause());
                        MDC.remove("link");
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}

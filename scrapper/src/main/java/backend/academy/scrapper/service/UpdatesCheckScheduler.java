package backend.academy.scrapper.service;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.time.Duration;
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
        executorService = ExecutorServiceMetrics.monitor(
                Metrics.globalRegistry,
                Executors.newFixedThreadPool(config.updatesChecker().threadsCount()),
                "linksCheckerExecutor");
        this.checker = checker;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @SuppressWarnings("EmptyCatch")
    public void checkUpdates() {
        while (true) {
            try {
                List<Link> links = linkRepository.getNotChecked(
                        batchSize, UtcDateTimeProvider.now(), linksCheckInterval.toSeconds());
                if (links.isEmpty()) {
                    break;
                }

                List<Future<Void>> results = executorService.invokeAll(links.stream()
                        .map(link -> (Callable<Void>) () -> {
                            try {
                                List<Update> updates = checker.getLinkUpdate(link);
                                if (!updates.isEmpty()) {
                                    checker.setUpdatesForLink(link, updates);
                                }
                            } finally {
                                link.lastUpdate(UtcDateTimeProvider.now());
                                link.checking(false);
                                linkRepository.update(link);
                            }
                            return null;
                        })
                        .toList());
                for (int i = 0; i < results.size(); i++) {
                    try {
                        results.get(i).get();
                    } catch (ExecutionException e) {
                        MDC.put("link", links.get(i).link());
                        log.error("Произошла ошибка при получении обновлений", e.getCause());
                        MDC.remove("link");
                    } catch (InterruptedException ignored) {
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}

package backend.academy.scrapper.scheduling;

import backend.academy.scrapper.service.UpdatesSendService;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdatesSendScheduler {

    private final UpdatesSendService service;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void sendUpdates() {
        boolean sent = true;
        while (sent) {
            sent = service.sendUpdates();
        }
    }
}

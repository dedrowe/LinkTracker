package backend.academy.bot.stateStorage;

import backend.academy.bot.dto.LinkState;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class TrackStateStorage {

    private final ConcurrentMap<Long, LinkState> storage = new ConcurrentHashMap<>();

    public LinkState getState(long id) {
        return storage.get(id);
    }

    public boolean containsState(long id) {
        return storage.containsKey(id);
    }

    public void put(long id, LinkState state) {
        storage.put(id, state);
    }

    public void remove(long id) {
        storage.remove(id);
    }
}

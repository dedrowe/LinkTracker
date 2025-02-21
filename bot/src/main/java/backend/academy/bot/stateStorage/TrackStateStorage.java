package backend.academy.bot.stateStorage;

import backend.academy.bot.dto.LinkState;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TrackStateStorage {

    private Map<Long, LinkState> storage = new HashMap<>();

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

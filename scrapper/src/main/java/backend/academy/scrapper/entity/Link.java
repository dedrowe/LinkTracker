package backend.academy.scrapper.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    private Long id;

    private String link;

    private LocalDateTime lastUpdate;

    private boolean deleted = false;

    public Link(String link) {
        this.link = link;
        this.lastUpdate = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Link(Long id, String link, LocalDateTime lastUpdate) {
        this.id = id;
        this.link = link;
        this.lastUpdate = lastUpdate;
    }
}

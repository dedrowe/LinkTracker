package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TgChat {

    private Long id;

    private long chatId;

    private boolean deleted = false;

    public TgChat(long chatId) {
        this.chatId = chatId;
    }

    public TgChat(Long id, long chatId) {
        this.id = id;
        this.chatId = chatId;
    }
}

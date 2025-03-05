package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TgChat {

    private Long id;

    private long chatId;

    public TgChat(long chatId) {
        this.chatId = chatId;
    }
}

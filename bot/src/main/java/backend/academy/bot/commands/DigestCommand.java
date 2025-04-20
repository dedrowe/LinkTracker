package backend.academy.bot.commands;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.shared.dto.TgChatUpdateDto;
import com.pengrad.telegrambot.model.Update;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/digest")
public class DigestCommand extends TgBotCommand {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String INVALID_SYNTAX_MESSAGE =
            "Неверный формат команды, ожидается: /digest off или /digest ЧЧ:ММ";

    public DigestCommand(ScrapperClientWrapper client) {
        super(
                client,
                """
                Установка режима дайджеста (все обновления будут приходить раз в сутки).
                Использование:
                /digest ЧЧ:ММ - включение дайджеста, время указывается по UTC(GMT)
                /digest off - выключение дайджеста
                """);
    }

    @Override
    public Optional<String> execute(Update update) {
        String[] command = update.message().text().split(" ");
        if (command.length != 2) {
            throw new InvalidCommandSyntaxException(
                    INVALID_SYNTAX_MESSAGE, update.message().text());
        }
        LocalTime digest;
        if (Objects.equals(command[1], "off")) {
            digest = null;
        } else {
            try {
                digest = LocalTime.parse(command[1], TIME_FORMAT);
            } catch (DateTimeParseException e) {
                throw new InvalidCommandSyntaxException(e.getMessage(), update.message().text(), INVALID_SYNTAX_MESSAGE);
            }
        }
        client.updateChat(update.message().chat().id(), new TgChatUpdateDto(digest));
        return Optional.of("Дайджест изменен");
    }
}

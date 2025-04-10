package backend.academy.scrapper.entity;

public interface LinkData {

    Long id();

    void id(Long id);

    Long linkId();

    Long chatId();

    boolean deleted();

    void deleted(boolean deleted);
}

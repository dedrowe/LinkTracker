package backend.academy.scrapper.entity;

public interface Filter {

    Long id();

    void id(Long id);

    String filter();

    void filter(String filter);

    Long dataId();
}

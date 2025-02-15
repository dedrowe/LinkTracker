package backend.academy.scrapper.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddLinkRequest(@NotNull String link, List<String> tags, List<String> filters) {}

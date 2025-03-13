package backend.academy.shared.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddLinkRequest(String link, @NotNull List<String> tags, @NotNull List<String> filters) {}

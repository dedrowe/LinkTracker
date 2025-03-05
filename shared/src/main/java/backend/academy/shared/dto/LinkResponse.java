package backend.academy.shared.dto;

import java.util.List;

public record LinkResponse(long id, String url, List<String> tags, List<String> filters) {}

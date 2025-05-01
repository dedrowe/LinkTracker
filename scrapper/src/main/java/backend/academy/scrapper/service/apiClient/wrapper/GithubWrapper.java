package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.dto.github.Comment;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.scrapper.dto.github.PullRequest;
import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("github.com")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("StringSplitter")
public class GithubWrapper implements ApiClientWrapper {

    private final GithubClient client;

    private static final int BODY_PREVIEW_LENGTH = 200;

    @Override
    public List<Update> getLastUpdate(URI uri, LocalDateTime lastUpdate) {
        try {
            String[] path = uri.getPath().split("/");
            if (path.length == 3) {
                return getRepositoryUpdate(uri, lastUpdate);
            } else if (path.length == 5 && path[3].equals("issues")) {
                return getIssueUpdate(uri, lastUpdate);
            } else {
                throw new ApiCallException("Ресурс не поддерживается", 400, uri.toString());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Ошибка при формировании ссылки api", e);
        }
    }

    @Override
    public void checkResource(URI uri) {
        String[] path = uri.getPath().split("/");
        if (path.length == 3) {
            client.getRepository(uri);
        } else if (path.length == 5 && path[3].equals("issues")) {
            client.getIssue(uri);
        } else {
            throw new ApiCallException("Ресурс не поддерживается", 400, uri.toString());
        }
    }

    public List<Update> getRepositoryUpdate(URI uri, LocalDateTime lastUpdate) throws URISyntaxException {
        URI pullsUri = new URI(uri + "/pulls");
        List<Update> result = new ArrayList<>();

        List<PullRequest> pulls = client.getPullRequests(pullsUri);
        PullRequest lastPull =
                pulls.stream().max(Comparator.comparing(PullRequest::createdAt)).orElse(null);
        StringBuilder sb = new StringBuilder();
        if (lastPull != null
                && ZonedDateTime.parse(lastPull.createdAt()).toLocalDateTime().isAfter(lastUpdate)) {
            sb.append("\nПоследний PR:\n");
            sb.append(lastPull.getInfo(BODY_PREVIEW_LENGTH));
            result.add(new Update(sb.toString(), lastPull.getPossibleFilters()));
            sb.delete(0, sb.length());
        }

        URI issuesUri = new URI(uri + "/issues");
        List<Issue> issues = client.getIssues(issuesUri);
        Issue lastIssue =
                issues.stream().max(Comparator.comparing(Issue::createdAt)).orElse(null);
        if (lastIssue != null
                && ZonedDateTime.parse(lastIssue.createdAt()).toLocalDateTime().isAfter(lastUpdate)) {
            sb.append("\nПоследний Issue:\n");
            sb.append(lastIssue.getInfo(BODY_PREVIEW_LENGTH));
            result.add(new Update(sb.toString(), lastIssue.getPossibleFilters()));
        }

        return result;
    }

    public List<Update> getIssueUpdate(URI uri, LocalDateTime lastUpdate) throws URISyntaxException {
        Issue issue = client.getIssue(uri);
        LocalDateTime updatedAt = ZonedDateTime.parse(issue.updatedAt()).toLocalDateTime();
        if (!updatedAt.isAfter(lastUpdate)) {
            return new ArrayList<>();
        }
        URI commentsUri = new URI(uri + "/comments");

        List<Comment> comments = client.getComments(commentsUri);
        List<Update> result = new ArrayList<>();

        Comment lastComment =
                comments.stream().max(Comparator.comparing(Comment::updatedAt)).orElse(null);
        StringBuilder sb = new StringBuilder();
        if (lastComment != null) {
            sb.append("\nПоследний комментарий:\n");
            sb.append(lastComment.getInfo(BODY_PREVIEW_LENGTH));
            result.add(new Update(sb.toString(), lastComment.getPossibleFilters()));
        }
        return result;
    }
}

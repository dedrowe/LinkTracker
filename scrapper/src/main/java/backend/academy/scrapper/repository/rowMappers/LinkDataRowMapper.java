package backend.academy.scrapper.repository.rowMappers;

import backend.academy.scrapper.entity.LinkData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.springframework.jdbc.core.RowMapper;

public final class LinkDataRowMapper implements RowMapper<LinkData> {

    @Override
    public LinkData mapRow(ResultSet rs, int rowNum) throws SQLException {
        LinkData linkData = new LinkData();

        linkData.id(rs.getLong("id"));
        linkData.linkId(rs.getLong("link_id"));
        linkData.chatId(rs.getLong("chat_id"));
        linkData.tags(Arrays.asList((String[]) rs.getArray("tags").getArray()));
        linkData.filters(Arrays.asList((String[]) rs.getArray("filters").getArray()));
        linkData.deleted(rs.getBoolean("deleted"));

        return linkData;
    }
}

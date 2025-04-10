package backend.academy.scrapper.service.entityFactory.filter;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.jdbc.JdbcFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class JdbcFilterFactory implements FilterFactory {

    @Override
    public Filter getFilter(LinkData data, String filter) {
        return new JdbcFilter(data.id(), filter);
    }

    @Override
    public Filter getFilter(Long id, LinkData data, String filter) {
        return new JdbcFilter(data.id(), filter);
    }
}

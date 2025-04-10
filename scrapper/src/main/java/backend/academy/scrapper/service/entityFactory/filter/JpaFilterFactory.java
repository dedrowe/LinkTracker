package backend.academy.scrapper.service.entityFactory.filter;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.jpa.JpaFilter;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public class JpaFilterFactory implements FilterFactory {

    @Override
    public Filter getFilter(LinkData data, String filter) {
        return new JpaFilter((JpaLinkData) data, filter);
    }

    @Override
    public Filter getFilter(Long id, LinkData data, String filter) {
        return new JpaFilter(id, (JpaLinkData) data, filter);
    }
}

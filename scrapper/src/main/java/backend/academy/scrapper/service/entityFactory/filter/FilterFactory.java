package backend.academy.scrapper.service.entityFactory.filter;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.LinkData;

public interface FilterFactory {

    Filter getFilter(LinkData data, String filter);

    Filter getFilter(Long id, LinkData data, String filter);
}

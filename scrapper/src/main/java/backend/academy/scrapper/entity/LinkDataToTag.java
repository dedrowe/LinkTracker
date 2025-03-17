package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LinkDataToTag {

    private Long id;

    private long dataId;

    private long tagId;

    private boolean deleted = false;

    public LinkDataToTag(long dataId, long tagId) {
        this.dataId = dataId;
        this.tagId = tagId;
    }

    public LinkDataToTag(long dataId, long tagId, boolean deleted) {
        this.dataId = dataId;
        this.tagId = tagId;
        this.deleted = deleted;
    }
}

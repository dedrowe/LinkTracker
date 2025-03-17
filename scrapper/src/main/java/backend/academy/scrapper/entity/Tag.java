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
public class Tag {

    private Long id;

    private String tag;

    public Tag(String tag) {
        this.tag = tag;
    }
}

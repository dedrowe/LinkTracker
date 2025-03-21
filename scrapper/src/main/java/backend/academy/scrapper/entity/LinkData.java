package backend.academy.scrapper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
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
@Entity
@Table(name = "links_data")
public class LinkData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_data_id_gen")
    @SequenceGenerator(name = "links_data_id_gen", allocationSize = 1, sequenceName = "links_data_id_seq")
    private Long id;

    @Column(name = "link_id", nullable = false, insertable = false, updatable = false)
    private Long linkId;

    @Column(name = "chat_id", nullable = false, insertable = false, updatable = false)
    private Long chatId;

    @Column(name = "deleted")
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "link_id", nullable = false, referencedColumnName = "id")
    private Link link;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false, referencedColumnName = "id")
    private TgChat tgChat;

    @OneToMany(
            mappedBy = "linkData",
            cascade = {CascadeType.ALL},
            orphanRemoval = true)
    private List<Filter> filters;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "links_data_to_tags",
            joinColumns = @JoinColumn(name = "data_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    public LinkData(long linkId, long chatId) {
        this.linkId = linkId;
        this.chatId = chatId;
    }

    public LinkData(Long id, long linkId, long chatId) {
        this.id = id;
        this.linkId = linkId;
        this.chatId = chatId;
    }

    public LinkData(Link link, TgChat tgChat) {
        this.link = link;
        this.tgChat = tgChat;
    }

    public LinkData(Link link, TgChat tgChat, boolean deleted) {
        this.link = link;
        this.tgChat = tgChat;
        this.deleted = deleted;
    }
}

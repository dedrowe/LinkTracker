package backend.academy.scrapper.entity.jpa;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
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
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "links_data")
@SQLRestriction("deleted = false")
public class JpaLinkData implements LinkData {

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
    private List<JpaFilter> filters;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "links_data_to_tags",
            joinColumns = @JoinColumn(name = "data_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public void id(Long id) {
        this.id = id;
    }

    @Override
    public Long linkId() {
        if (link == null) {
            return null;
        }
        return link.id();
    }

    @Override
    public Long chatId() {
        if (tgChat == null) {
            return null;
        }
        return tgChat.id();
    }

    @Override
    public boolean deleted() {
        return deleted;
    }

    @Override
    public void deleted(boolean deleted) {
        this.deleted = deleted;
    }

    public JpaLinkData(Link link, TgChat tgChat) {
        this.link = link;
        this.tgChat = tgChat;
    }

    public JpaLinkData(Long id, Link link, TgChat tgChat) {
        this.id = id;
        this.link = link;
        this.tgChat = tgChat;
    }

    public JpaLinkData(Link link, TgChat tgChat, boolean deleted) {
        this.link = link;
        this.tgChat = tgChat;
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JpaLinkData that = (JpaLinkData) o;
        return deleted == that.deleted && Objects.equals(link, that.link) && Objects.equals(tgChat, that.tgChat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link, tgChat, deleted);
    }
}

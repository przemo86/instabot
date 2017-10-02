package pl.szewczyk.stats;

import org.jinstagram.entity.users.feed.MediaFeedData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by przem on 27.09.2017.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "media", indexes = {@Index(name = "mediaid", columnList = "mediaId")})
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_seq")
    @SequenceGenerator(name = "media_seq", sequenceName = "media_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Statistic statistic;


    private String mediaId;

    private String thumbnailUri;

    private String link;

    @ElementCollection
    @CollectionTable(name = "tags")
    private Set<String> tags = new HashSet<>();


    public Media(MediaFeedData mediaFeedData) {
        this.link = mediaFeedData.getLink();
        this.mediaId = mediaFeedData.getId();
        this.thumbnailUri = mediaFeedData.getImages().getThumbnail().getImageUrl();
        this.tags = new HashSet<>(mediaFeedData.getTags());
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}

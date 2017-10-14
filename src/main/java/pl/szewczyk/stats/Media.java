package pl.szewczyk.stats;

import org.jinstagram.entity.common.Location;
import org.jinstagram.entity.users.feed.MediaFeedData;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by przem on 27.09.2017.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "media", schema = "instabot", indexes = {@Index(name = "mediaid", columnList = "mediaId")})
@XmlRootElement
public class Media implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_seq")
    @SequenceGenerator(name = "media_seq", sequenceName = "media_seq", allocationSize = 1, schema = "instabot")
    private Long id;

    @ManyToOne()
    private Statistic statistic;


    private String mediaId;

    private String thumbnailUri;

    private String link;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tags", schema = "instabot")
    private Set<String> tags = new HashSet<>();

    @Transient
    private String user;

    @Transient
    private Set<String> userFollowed;

    @Transient
    private Set<String> userFollows;

    @Transient
    private Location location;

    public Media() {
    }

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Set<String> getUserFollowed() {
        return userFollowed;
    }

    public void setUserFollowed(Set<String> userFollowed) {
        this.userFollowed = userFollowed;
    }

    public Set<String> getUserFollows() {
        return userFollows;
    }

    public void setUserFollows(Set<String> userFollows) {
        this.userFollows = userFollows;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

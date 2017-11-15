package pl.szewczyk.stats;

import com.google.gson.annotations.Expose;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.stream.Collectors;

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

    @Expose
    @Column(name = "stat_id")
    private Statistic statistic;

    private String mediaId;

    @Expose
    private String thumbnailUri;

    @Expose
    private String link;

    @Expose
    @Column(length = 3000)
    private String tags;

    @Transient
    @Expose
    private String userName;

    @Transient
    @Expose
    private String userProfileImage;

    @Transient
    @Expose
    private Integer userFollowed;

    @Transient
    @Expose
    private Integer userLikes;

    @Transient
    @Expose
    private Integer commentsCount;

    public Media() {
    }

    public Media(me.postaddict.instagram.scraper.domain.Media mediaFeedData) {
        this.link = mediaFeedData.link;
        this.mediaId = mediaFeedData.id;
        this.thumbnailUri = mediaFeedData.imageUrls.thumbnail;
        this.tags = mediaFeedData.getTags().stream().collect(Collectors.joining(","));
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public Integer getUserFollowed() {
        return userFollowed;
    }

    public void setUserFollowed(Integer userFollowed) {
        this.userFollowed = userFollowed;
    }

    public Integer getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(Integer userLikes) {
        this.userLikes = userLikes;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        return mediaId != null ? mediaId.equals(media.mediaId) : media.mediaId == null;
    }

    @Override
    public int hashCode() {
        return mediaId != null ? mediaId.hashCode() : 0;
    }
}

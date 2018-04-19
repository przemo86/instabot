package pl.szewczyk.stats;

import com.google.gson.annotations.Expose;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
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
    @ManyToOne
    @JoinColumn(name = "stat_id", referencedColumnName = "id")
    private Statistic statistic;

    private String mediaId;

    @Expose
    private String thumbnailUri;

    @Expose
    private String link;

    @Column(length = 2200)
    private String caption;

    @Expose
    @Column(length = 3000)
    private String tags;

    private Date createdDate;

    private Boolean liked;

    private Boolean commented;

    @Column(name = "action_time")
    private Date actionTime;

    private String userName;

    private String userProfileImage;

    private Integer userFollowed;

    private Integer userLikes;

    private Integer commentsCount;

    private String locationName;

    private String myComment;

    public Media() {
    }

    public Media(me.postaddict.instagram.scraper.domain.Media mediaFeedData) {
        this(mediaFeedData, null);
    }

    public Media(me.postaddict.instagram.scraper.domain.Media mediaFeedData, String myComment) {
        System.out.println(mediaFeedData);
        this.link = mediaFeedData.link;
        this.mediaId = mediaFeedData.id;
        this.thumbnailUri = mediaFeedData.imageUrl;
        this.tags = mediaFeedData.getTags().stream().collect(Collectors.joining(","));
        this.createdDate = new Date(mediaFeedData.createdTime);
        this.actionTime = mediaFeedData.action_time;
        this.liked = mediaFeedData.liked;
        this.commented = mediaFeedData.commented;
        this.locationName = mediaFeedData.locationName;
        this.caption = mediaFeedData.caption;
        this.myComment = myComment;
        this.userLikes = mediaFeedData.likesCount;
//        this.userName = mediaFeedData.owner.username;
//        this.locationName = mediaFeedData.locationName;
//        this.userProfileImage = mediaFeedData.owner.profilePicUrl;
        this.commentsCount = mediaFeedData.commentsCount;
//        this.userFollowed = mediaFeedData.owner.followedByCount;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Boolean getCommented() {
        return commented;
    }

    public void setCommented(Boolean commented) {
        this.commented = commented;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMyComment() {
        return myComment;
    }

    public void setMyComment(String myComment) {
        this.myComment = myComment;
    }

//    @Override
//    public String toString() {
//        return "Media{" +
//                "id=" + id +
//                ", statistic=" + statistic +
//                ", mediaId='" + mediaId + '\'' +
//                ", thumbnailUri='" + thumbnailUri + '\'' +
//                ", link='" + link + '\'' +
//                ", caption='" + caption + '\'' +
//                ", tags='" + tags + '\'' +
//                ", createdDate=" + createdDate +
//                ", liked=" + liked +
//                ", commented=" + commented +
//                ", actionTime=" + actionTime +
//                ", userName='" + userName + '\'' +
//                ", userProfileImage='" + userProfileImage + '\'' +
//                ", userFollowed=" + userFollowed +
//                ", userLikes=" + userLikes +
//                ", commentsCount=" + commentsCount +
//                ", locationName='" + locationName + '\'' +
//                ", myComment='" + myComment + '\'' +
//                '}';
//    }
}

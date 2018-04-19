package pl.szewczyk.projects;

import pl.szewczyk.account.Account;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "project", schema = "instabot")
public class Project implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq")
    @SequenceGenerator(name = "project_seq", sequenceName = "project_seq", allocationSize = 1, schema = "instabot")
    private Long id;

    @Column(unique = true)
    private String name;

    private Instant created;

    private String customer;

    private String instagramAccount;

    private boolean status = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private Account owner;

    private String includeHashtags;

    private String excludeHashtags;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private FrequencyEnum likeFrequency;

//    @Enumerated(EnumType.STRING)
//    private FrequencyEnum commentFrequency;

    @Enumerated(EnumType.STRING)
    private HashtagSearchEnum hashtagSearch;

    @Column(name = "_like")
    private boolean like;

    @Column(name = "_comment")
    private boolean comment;

    private String blackFileName;

    @Column(columnDefinition = "blacklisted text")
    private String blacklisted;

    private boolean onlineStats = true;

    private String locationName;

    private String locationId;

    private Integer mediaAge;

    protected Project() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getIncludeHashtags() {
        if (includeHashtags != null)
            return includeHashtags.replaceAll(" ", "");
        return null;
    }

    public void setIncludeHashtags(String includeHashtags) {
        this.includeHashtags = includeHashtags;
    }

    public String getExcludeHashtags() {
        if (excludeHashtags != null)
            return excludeHashtags.replaceAll(" ", "");
        return null;
    }

    public void setExcludeHashtags(String excludeHashtags) {
        this.excludeHashtags = excludeHashtags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public FrequencyEnum getLikeFrequency() {
        return likeFrequency;
    }

    public void setLikeFrequency(FrequencyEnum likeFrequency) {
        this.likeFrequency = likeFrequency;
    }

//    public FrequencyEnum getCommentFrequency() {
//        return commentFrequency;
//    }

//    public void setCommentFrequency(FrequencyEnum commentFrequency) {
//        this.commentFrequency = commentFrequency;
//    }

    public HashtagSearchEnum getHashtagSearch() {
        return hashtagSearch;
    }

    public void setHashtagSearch(HashtagSearchEnum hashtagSearch) {
        this.hashtagSearch = hashtagSearch;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public boolean isComment() {
        return comment;
    }

    public void setComment(boolean comment) {
        this.comment = comment;
    }

    public String getInstagramAccount() {
        return instagramAccount;
    }

    public void setInstagramAccount(String instagramAccount) {
        this.instagramAccount = instagramAccount;
    }

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }

    public String getBlackFileName() {
        return blackFileName;
    }

    public void setBlackFileName(String blackFileName) {
        this.blackFileName = blackFileName;
    }

    public String getBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(String blacklisted) {
        this.blacklisted = blacklisted;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Integer getMediaAge() {
        return mediaAge;
    }

    public void setMediaAge(Integer mediaAge) {
        this.mediaAge = mediaAge;
    }

    public boolean isOnlineStats() {
        return onlineStats;
    }

    public void setOnlineStats(boolean onlineStats) {
        this.onlineStats = onlineStats;
    }

    public String getRandomComment() {
        if (comments.size() == 0)
            return "";

        int sum = comments.stream().mapToInt(c -> c.getPriority()).sum();
        Double rand = ((Math.random() * sum) + 1);

        int rSum = 0;
        for (Comment c : comments) {
        System.out.println("R " + c.getComment());
            rSum += c.getPriority();
            if (rSum <= rand) {
                System.out.println("RET " + c.getComment());
                return c.getComment();
            }
        }
        rand = (Math.random() * comments.size());
        System.out.println("RETTT " + rand);
        return comments.get(rand.intValue()).getComment();
    }
}

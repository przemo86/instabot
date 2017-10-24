package pl.szewczyk.projects;

import pl.szewczyk.account.Account;

import javax.persistence.*;
import java.time.Instant;

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

    private String commentString;

    @Enumerated(EnumType.STRING)
    private FrequencyEnum likeFrequency;

    @Enumerated(EnumType.STRING)
    private FrequencyEnum commentFrequency;

    @Enumerated(EnumType.STRING)
    private HashtagSearchEnum hashtagSearch;

    @Column(name = "_like")
    private Boolean like = false;

    @Column(name = "_comment")
    private Boolean comment = false;

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

    public String getInstagramAccount() {
        return instagramAccount;
    }

    public void setInstagramAccount(String instagramAccount) {
        this.instagramAccount = instagramAccount;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }

    public String getIncludeHashtags() {
        return includeHashtags;
    }

    public void setIncludeHashtags(String includeHashtags) {
        this.includeHashtags = includeHashtags;
    }

    public String getExcludeHashtags() {
        return excludeHashtags;
    }

    public void setExcludeHashtags(String excludeHashtags) {
        this.excludeHashtags = excludeHashtags;
    }

    public String getCommentString() {
        return commentString;
    }

    public void setCommentString(String commentString) {
        this.commentString = commentString;
    }

    public FrequencyEnum getLikeFrequency() {
        return likeFrequency;
    }

    public void setLikeFrequency(FrequencyEnum likeFrequency) {
        this.likeFrequency = likeFrequency;
    }

    public FrequencyEnum getCommentFrequency() {
        return commentFrequency;
    }

    public void setCommentFrequency(FrequencyEnum commentFrequency) {
        this.commentFrequency = commentFrequency;
    }

    public HashtagSearchEnum getHashtagSearch() {
        return hashtagSearch;
    }

    public void setHashtagSearch(HashtagSearchEnum hashtagSearch) {
        this.hashtagSearch = hashtagSearch;
    }

    public Boolean isLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

    public Boolean isComment() {
        return comment;
    }

    public void setComment(Boolean comment) {
        this.comment = comment;
    }
}


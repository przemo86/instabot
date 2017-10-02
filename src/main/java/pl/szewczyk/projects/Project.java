package pl.szewczyk.projects;

import pl.szewczyk.account.Account;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
@Entity
@Table(name = "project")
public class Project implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq")
    @SequenceGenerator(name = "project_seq", sequenceName = "project_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true)
    private String name;

    private Instant created;

    private String customer;

    private String instagramAccount;

    private boolean status = false;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private Account owner;

    @ElementCollection
    @CollectionTable(name = "includetags")
       private Set<String> includeHashtags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "excludetags")
    private Set<String> excludeHashtags = new HashSet<>();

    private String commentString;

    @Enumerated(EnumType.STRING)
    private FrequencyEnum likeFrequency;

    @Enumerated(EnumType.STRING)
    private FrequencyEnum commentFrequency;

    @Enumerated(EnumType.STRING)
    private HashtagSearchEnum hashtagSearch;

    @Column(name = "_like")
    private boolean like;

    private boolean comment;

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

    public Set<String> getIncludeHashtags() {
        return includeHashtags;
    }

    public void setIncludeHashtags(Set<String> includeHashtags) {
        this.includeHashtags = includeHashtags;
    }

    public Set<String> getExcludeHashtags() {
        return excludeHashtags;
    }

    public void setExcludeHashtags(Set<String> excludeHashtags) {
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
}

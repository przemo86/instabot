package pl.szewczyk.projects;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectForm implements java.io.Serializable {

    private String name;

    private String customer;

    private String instagramAccount;

    private boolean status = false;

    private String includeHashtags;

    private String excludeHashtags;

    private List<CommentForm> comments = new ArrayList<>();

    private FrequencyEnum likeFrequency;

    private FrequencyEnum commentFrequency;

    private HashtagSearchEnum hashtagSearch;

    private boolean like;

    private boolean comment;
    private String blackFileName;

    private String locationName;
    private String locationId;
    private Integer mediaAge;
    private boolean onlineStats;

    private MultipartFile file;

    public ProjectForm(Project project) {
        this.name = project.getName();
        this.customer = project.getCustomer();
        this.instagramAccount = project.getInstagramAccount();
        this.status = project.isStatus();
        this.includeHashtags = project.getIncludeHashtags();
        this.excludeHashtags = project.getExcludeHashtags();
        this.comments = project.getComments().stream().map(c -> CommentForm.fromEntity(c)).collect(Collectors.toList());
            project.getComments().stream().forEach(c -> c.setProject(project));
        this.likeFrequency = project.getLikeFrequency();
//        this.commentFrequency = project.getCommentFrequency();
        this.hashtagSearch = project.getHashtagSearch();
        this.like = project.isLike();
        this.comment = project.isComment();
        this.blackFileName = project.getBlackFileName();
        this.locationName = project.getLocationName();
        this.locationId = project.getLocationId();
        this.mediaAge = project.getMediaAge();
        this.onlineStats = project.isOnlineStats();
    }

    public ProjectForm() {
    }

    public Project toEntity(Project project) {
        System.out.println("FILE??? " + this.file);
        project.setName(this.name);
        project.setCustomer(this.customer);
        project.setInstagramAccount(this.instagramAccount);
        project.setStatus(this.status);
        project.setIncludeHashtags(this.includeHashtags);
        project.setExcludeHashtags(this.excludeHashtags);
//        project.setComments(this.comments.stream().map(c -> c.toEntity()).collect(Collectors.toList()));
        project.setLikeFrequency(this.likeFrequency);
//        project.setCommentFrequency(this.commentFrequency);
        project.setHashtagSearch(this.hashtagSearch);
        project.setLike(this.like);
        project.setComment(this.comment);
        project.setLocationName(this.locationName);
        project.setLocationId(this.locationId);
        project.setMediaAge(this.mediaAge);
        project.setOnlineStats(this.onlineStats);
        try {
            project.setBlacklisted(new String(this.file.getBytes()));
            project.setBlackFileName(this.file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (project.getId() == null)
            project.setCreated(Instant.now());
        return project;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<CommentForm> getComments() {
        return comments;
    }

    public void setComments(List<CommentForm> comments) {
        this.comments = comments;
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

    public String getBlackFileName() {
        return blackFileName;
    }

    public void setBlackFileName(String blackFileName) {
        this.blackFileName = blackFileName;
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
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

    @Override
    public String toString() {
        return "ProjectForm{" +
                "name='" + name + '\'' +
                ", customer='" + customer + '\'' +
                ", instagramAccount='" + instagramAccount + '\'' +
                ", status=" + status +
                ", includeHashtags='" + includeHashtags + '\'' +
                ", excludeHashtags='" + excludeHashtags + '\'' +
                ", comments='" + comments + '\'' +
                ", likeFrequency=" + likeFrequency +
                ", commentFrequency=" + commentFrequency +
                ", hashtagSearch=" + hashtagSearch +
                ", like=" + like +
                ", comment=" + comment +
                '}';
    }
}

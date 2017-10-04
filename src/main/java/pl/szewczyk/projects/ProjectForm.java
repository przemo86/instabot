package pl.szewczyk.projects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@LikeCorrect
@CommentCorrect
public class ProjectForm implements java.io.Serializable {

    @Size(min = 1, max = 40, message = "pole nie może być puste")
    private String name;

    @Size(min = 1, max = 40, message = "pole nie może być puste")
    private String customer;

    @Size(min = 1, max = 25, message = "Nie wybrano konta instagram")
    private String instagramAccount;

    private boolean status = false;

    @Size(min = 1, message = "Podaj hashtagi występujące")
    private String includeHashtags;

    private String excludeHashtags;

    private String commentString;

    @NotNull(message = "Wybierz częstotliwość lajków")
    private FrequencyEnum likeFrequency;

    @NotNull(message = "Wybierz częstotliwość komentarzy")
    private FrequencyEnum commentFrequency;

    @NotNull(message = "Wybierz rodzaj poszukiwania hashtagów")
    private HashtagSearchEnum hashtagSearch;

    private boolean like;

    private boolean comment;


    public ProjectForm(Project project) {
        this.name = project.getName();
        this.customer = project.getCustomer();
        this.instagramAccount = project.getInstagramAccount();
        this.status = project.isStatus();
        this.includeHashtags = String.join(",",project.getIncludeHashtags().stream().map(s -> s.trim()).collect(Collectors.toList()));
        this.excludeHashtags = String.join(",",project.getExcludeHashtags().stream().map(s -> s.trim()).collect(Collectors.toList()));
        this.commentString = project.getCommentString();
        this.likeFrequency = project.getLikeFrequency();
        this.commentFrequency = project.getCommentFrequency();
        this.hashtagSearch = project.getHashtagSearch();
        this.like = project.isLike();
        this.comment = project.isComment();

    }

    public ProjectForm() {
    }

    public Project toEntity(Project project) {
        Logger.getGlobal().severe("IN");
        Logger.getGlobal().severe(includeHashtags);
        Logger.getGlobal().severe(excludeHashtags);

        project.setName(this.name);
        project.setCustomer(this.customer);
        project.setInstagramAccount(this.instagramAccount);
        project.setStatus(this.status);
        if (Objects.nonNull(includeHashtags))
            project.setIncludeHashtags(new HashSet(Arrays.asList(includeHashtags.split(","))));
        else
            project.setIncludeHashtags(new HashSet());
        if (Objects.nonNull(excludeHashtags))
            project.setExcludeHashtags(new HashSet(Arrays.asList(excludeHashtags.split(","))));
        else
            project.setExcludeHashtags(new HashSet());

        project.setCommentString(this.commentString);
        project.setLikeFrequency(this.likeFrequency);
        project.setCommentFrequency(this.commentFrequency);
        project.setHashtagSearch(this.hashtagSearch);
        project.setLike(this.like);
        project.setComment(this.comment);
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

    @Override
    public String toString() {
        return "ProjectForm{" +
                "name='" + name + '\'' +
                ", customer='" + customer + '\'' +
                ", instagramAccount='" + instagramAccount + '\'' +
                ", status=" + status +
                ", includeHashtags='" + includeHashtags + '\'' +
                ", excludeHashtags='" + excludeHashtags + '\'' +
                ", commentString='" + commentString + '\'' +
                ", likeFrequency=" + likeFrequency +
                ", commentFrequency=" + commentFrequency +
                ", hashtagSearch=" + hashtagSearch +
                ", like=" + like +
                ", comment=" + comment +
                '}';
    }
}

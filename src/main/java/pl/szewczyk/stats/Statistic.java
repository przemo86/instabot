package pl.szewczyk.stats;

import pl.szewczyk.projects.HashtagSearchEnum;
import pl.szewczyk.projects.Project;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by przem on 27.09.2017.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "statistics", schema = "instabot", indexes = {@Index(name = "statistic_project_idx", columnList = "kind,projectid")})
public class Statistic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statistics_seq")
    @SequenceGenerator(name = "statistics_seq", sequenceName = "statistics_seq", allocationSize = 1, schema = "instabot")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projectid")
    private Project project;

    private Instant time;

    private String includeHashtags;

    private String excludeHashtags;

    private String commentString;

    @Enumerated(EnumType.STRING)
    private HashtagSearchEnum hashtagSearch;

    private Character kind;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="id", referencedColumnName = "stat_id")
    private Set<Media> media = new HashSet<>();

    public Statistic() {
    }

    public Statistic(Project project, Set<Media> mediaFeedData) {

        this.time = Instant.now();

        this.project = project;
        this.commentString = project.getCommentString();
        this.includeHashtags = project.getIncludeHashtags();
        this.excludeHashtags = project.getExcludeHashtags();
        this.hashtagSearch = project.getHashtagSearch();
        this.media = new HashSet<>(mediaFeedData);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
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

    public HashtagSearchEnum getHashtagSearch() {
        return hashtagSearch;
    }

    public void setHashtagSearch(HashtagSearchEnum hashtagSearch) {
        this.hashtagSearch = hashtagSearch;
    }

    public Character getKind() {
        return kind;
    }

    public void setKind(Character kind) {
        this.kind = kind;
    }

    public Set<Media> getMedia() {
        return media;
    }

    public void setMedia(Set<Media> media) {
        this.media = media;
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "id=" + id +
                ", project=" + project +
                ", time=" + time +
                ", includeHashtags='" + includeHashtags + '\'' +
                ", excludeHashtags='" + excludeHashtags + '\'' +
                ", commentString='" + commentString + '\'' +
                ", hashtagSearch=" + hashtagSearch +
                ", kind=" + kind +
                ", media=" + media +
                '}';
    }
}

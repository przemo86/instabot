package pl.szewczyk.stats;

import org.jinstagram.entity.users.feed.MediaFeedData;
import pl.szewczyk.projects.HashtagSearchEnum;
import pl.szewczyk.projects.Project;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by przem on 27.09.2017.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "statistics", schema = "instabot", indexes = {@Index(name = "statistic_project_idx", columnList = "kind,projectid")})
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statistics_seq")
    @SequenceGenerator(name = "statistics_seq", sequenceName = "statistics_seq", allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "projectid")
    private Project project;

    private Instant time;

    @ElementCollection
    @CollectionTable(name = "stat_includetags")
    private Set<String> includeHashtags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "stat_excludetags")
    private Set<String> excludeHashtags = new HashSet<>();

    private String commentString;

    @Enumerated(EnumType.STRING)
    private HashtagSearchEnum hashtagSearch;

    private Character kind;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = Media.class, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<Media> media = new HashSet<>();

    public Statistic(Project project, Set<MediaFeedData> mediaFeedData) {

        this.time = Instant.now();

        this.project = project;
        this.commentString = project.getCommentString();
        this.includeHashtags = new HashSet<>(project.getIncludeHashtags());
        this.excludeHashtags = new HashSet<>(project.getExcludeHashtags());
        this.hashtagSearch = project.getHashtagSearch();
        for (MediaFeedData mediaFeed : mediaFeedData) {
            media.add(new Media(mediaFeed));
        }

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
}

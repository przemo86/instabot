package pl.szewczyk.projects;

import javax.persistence.*;

/**
 * Created by przem on 21.12.2017.
 */
@Entity
@Table(name = "comments", schema = "instabot")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    @SequenceGenerator(name = "comment_seq", sequenceName = "comment_seq", allocationSize = 1, schema = "instabot")
    private Long id;

    @ManyToOne
    private Project project;

    private String comment;

    private Integer priority;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}

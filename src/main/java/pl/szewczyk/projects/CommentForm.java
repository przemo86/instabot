package pl.szewczyk.projects;

/**
 * Created by przem on 21.12.2017.
 */
public class CommentForm {


    private Long id;

    private String comment;

    private Integer priority;

    public Comment toEntity() {
        Comment c = new Comment();
        c.setComment(comment);
        c.setPriority(priority);
        c.setId(id);
        return c;
    }

    public static CommentForm fromEntity(Comment c) {
        CommentForm cf = new CommentForm();
        cf.setComment(c.getComment());
        cf.setPriority(c.getPriority());
        cf.setId(c.getId());
        return cf;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

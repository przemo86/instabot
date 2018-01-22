package pl.szewczyk.home;

import java.util.Date;

/**
 * Created by przem on 29.09.2017.
 */
public class DashboardPOJO {

    private Long projectId;
    private String projectName;
    private String customerName;

    private Date runningTime;
    private Date nextFire;
    private Long hits;

    private Date likeRunningTime;
    private Date likeNextFire;
    private Long likeHits;

    private Date commentRunningTime;
    private Date commentNextFire;
    private Long commentHits;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Date getLikeRunningTime() {
        return likeRunningTime;
    }

    public void setLikeRunningTime(Date likeRunningTime) {
        this.likeRunningTime = likeRunningTime;
    }

    public Date getLikeNextFire() {
        return likeNextFire;
    }

    public void setLikeNextFire(Date likeNextFire) {
        this.likeNextFire = likeNextFire;
    }


    public Date getCommentRunningTime() {
        return commentRunningTime;
    }

    public void setCommentRunningTime(Date commentRunningTime) {
        this.commentRunningTime = commentRunningTime;
    }

    public Date getCommentNextFire() {
        return commentNextFire;
    }

    public void setCommentNextFire(Date commentNextFire) {
        this.commentNextFire = commentNextFire;
    }

    public Long getLikeHits() {
        return likeHits;
    }

    public void setLikeHits(Long likeHits) {
        this.likeHits = likeHits;
    }

    public Long getCommentHits() {
        return commentHits;
    }

    public void setCommentHits(Long commentHits) {
        this.commentHits = commentHits;
    }

    public Date getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(Date runningTime) {
        this.runningTime = runningTime;
    }

    public Date getNextFire() {
        return nextFire;
    }

    public void setNextFire(Date nextFire) {
        this.nextFire = nextFire;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }
}

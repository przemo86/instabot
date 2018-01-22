package me.postaddict.instagram.scraper.domain;

import java.util.Date;
import java.util.Map;

public class Comment {
    public String text;
    public Date createdAt;
    public String id;

    public Account user;

    public static Comment fromApi(Map commentMap) {
        Comment instance = new Comment();
        if (commentMap.get("text") == null) {
            commentMap = (Map) commentMap.get("node");
        }
        instance.text = (String) commentMap.get("text");

        try {
            instance.createdAt = new Date((long) (0d + (Double) commentMap.get("created_at")* 1000));
        } catch (NullPointerException e) {
            Object time = commentMap.get("created_time");
            if (time instanceof String) {
                instance.createdAt = new Date(new Long((String) time)* 1000);
            } else if (time instanceof Double) {
                instance.createdAt = new Date(((Double) time).longValue()* 1000);
            }
        }

        instance.id = (String) commentMap.get("id");
        try {
            instance.user = Account.fromComments((Map) commentMap.get("from"));
        } catch (NullPointerException ne) {
            instance.user = Account.fromComments((Map) commentMap.get("owner"));
        }


        return instance;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", createdAt=" + createdAt +
                ", id='" + id + '\'' +
                ", user=" + user +
                '}';
    }
}

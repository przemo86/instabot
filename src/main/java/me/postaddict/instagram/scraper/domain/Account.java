package me.postaddict.instagram.scraper.domain;

import java.util.Map;
import java.util.logging.Logger;

public class Account {

    private static Logger log = Logger.getLogger(Account.class.getName());

    public String username;
    public int followsCount;
    public int followedByCount;
    public String profilePicUrl;
    public long id;
    public String biography;
    public String fullName;
    public int mediaCount;
    public boolean isPrivate;
    public String externalUrl;
    public boolean isVerified;

    public static Account fromAccountPage(Map userJson) {
        Account instance = new Account();
        instance.username = (String) userJson.get("username");
        instance.followsCount = ((Double) (((Map) (userJson.get("edge_follow"))).get("count"))).intValue();
        instance.followedByCount = ((Double) (((Map) (userJson.get("edge_followed_by"))).get("count"))).intValue();
        instance.profilePicUrl = (String) userJson.get("profile_pic_url");
        instance.id = Long.parseLong((String) userJson.get("id"));
        instance.biography = (String) userJson.get("biography");
        instance.fullName = (String) userJson.get("full_name");
        instance.mediaCount = ((Double) (((Map) (userJson.get("edge_saved_media"))).get("count"))).intValue();
        instance.isPrivate = (Boolean) userJson.get("is_private");
        instance.externalUrl = (String) userJson.get("external_url");
        instance.isVerified = (Boolean) userJson.get("is_verified");
        return instance;
    }

    public static Account fromLogin(Map map) {
        Account instance = new Account();
        Map userJson = (Map) map.get("user");
        instance.id = Long.parseLong(userJson.get("pk").toString());
        instance.username = (String) userJson.get("username");
        instance.fullName = (String) userJson.get("full_name");
        instance.followedByCount = Double.valueOf(Double.parseDouble(userJson.get("follower_count").toString())).intValue();
        instance.profilePicUrl = (String) userJson.get("profile_pic_url");
        instance.isPrivate = (Boolean) userJson.get("is_private");
        instance.isVerified = (Boolean) userJson.get("is_verified");
        return instance;
    }

    public static Account fromMediaPage(Map map) {
        log.info(map.toString());
        Account instance = new Account();
        log.info("1 " + map);
        instance.id = Long.parseLong((String) map.get("id"));
        log.info("2");
        instance.username = (String) map.get("username");
        log.info("3");
        if (map.containsKey("profile_pic_url")) {
            instance.profilePicUrl = (String) map.get("profile_pic_url");
        } else if (map.containsKey("profile_picture")) {
            instance.profilePicUrl = (String) map.get("profile_picture");
        }
        log.info("4");

        instance.fullName = (String) map.get("full_name");
        log.info("5");
        if (map.containsKey("is_private")) {
            instance.isPrivate = (Boolean) map.get("is_private");
        }

        log.info("returnung " + instance.toString());
        return instance;
    }

    public static Account fromComments(Map map) {
        Account instance = new Account();
        instance.id = Long.parseLong((String) map.get("id"));
        instance.username = (String) map.get("username");
        instance.profilePicUrl = (String) map.get("profile_picture");
        instance.fullName = (String) map.get("full_name");
        if(instance.fullName == null){
            instance.fullName = instance.username;
        }
        return instance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", followsCount=" + followsCount +
                ", followedByCount=" + followedByCount +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", id=" + id +
                ", biography='" + biography + '\'' +
                ", fullName='" + fullName + '\'' +
                ", mediaCount=" + mediaCount +
                ", isPrivate=" + isPrivate +
                ", externalUrl='" + externalUrl + '\'' +
                ", isVerified=" + isVerified +
                '}';
    }
}

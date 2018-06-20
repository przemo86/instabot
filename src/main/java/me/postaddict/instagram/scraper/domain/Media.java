package me.postaddict.instagram.scraper.domain;

import me.postaddict.instagram.scraper.Endpoint;
import me.postaddict.instagram.scraper.Instagram;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Media {

    public static final String INSTAGRAM_URL = "https://www.instagram.com/";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_CAROUSEL = "carousel";
    public static final long INSTAGRAM_BORN_YEAR = 1262304000000L;
    public String id;
    public long createdTime;
    public String type;
    public String link;
    public String imageUrl;
//    public ImageUrls imageUrls = new ImageUrls();
//    public List<CarouselMedia> carouselMedia;
    public String caption;
    public VideoUrls videoUrls = new VideoUrls();
    public String shortcode;
    public int commentsCount;
    public List<Comment> previewCommentsList;
    public int likesCount;
    public int videoViews;
    public String ownerId;
    public Account owner;
    public String locationName;
    public String locationId;

    public Date action_time;
    public Boolean liked = false;
    public Boolean commented = false;
    public String myComment;


    public static class CarouselMedia {
        public String type;
        public ImageUrls imageUrls = new ImageUrls();
        public VideoUrls videoUrls = new VideoUrls();
        public int videoViews;
    }

    public static class ImageUrls {
        public String low;
        public String thumbnail;
        public String standard;
        public String high;

        @Override
        public String toString() {
            return "{" +
                    "low='" + low + '\'' +
                    ", thumbnail='" + thumbnail + '\'' +
                    ", standard='" + standard + '\'' +
                    ", high='" + high + '\'' +
                    '}';
        }
    }

    public static class VideoUrls {
        public String low;
        public String standard;
        public String lowBandwidth;

        @Override
        public String toString() {
            return "{" +
                    "low='" + low + '\'' +
                    ", standard='" + standard + '\'' +
                    ", lowBandwidth='" + lowBandwidth + '\'' +
                    '}';
        }
    }

    public static Media fromApi(Map mediaMap) {
        Media instance = new Media();
        instance.id = (String) mediaMap.get("id");
        instance.createdTime = Long.parseLong((String) mediaMap.get("taken_at_timestamp"));
        fixDate(instance);
        instance.type = (String) mediaMap.get("type");
        instance.link = (String) mediaMap.get("link");
        instance.shortcode = (String) mediaMap.get("code");
        if (mediaMap.get("caption") != null) {
            instance.caption = (String) ((Map) mediaMap.get("caption")).get("text");
        }

        Map images = (Map) mediaMap.get("images");
//        fillImageUrls(instance, (String) ((Map) images.get("standard_resolution")).get("url"));
        instance.commentsCount = (new Double(((Map) mediaMap.get("comments")).get("count").toString())).intValue();
        instance.likesCount = (new Double(((Map) mediaMap.get("likes")).get("count").toString())).intValue();

        instance.imageUrl = (String) mediaMap.get("display_url");
        if (instance.type.equals(TYPE_VIDEO) && mediaMap.containsKey("videos")) {
            Map videos = (Map) mediaMap.get("videos");
            instance.videoUrls.low = (String) ((Map) videos.get("low_resolution")).get("url");
            instance.videoUrls.standard = (String) ((Map) videos.get("standard_resolution")).get("url");
            instance.videoUrls.lowBandwidth = (String) ((Map) videos.get("low_bandwidth")).get("url");
        }

        instance.previewCommentsList = new ArrayList<Comment>();
        if (instance.commentsCount > 0) {
            for (Object o : (List) ((Map) mediaMap.get("comments")).get("data")) {
                instance.previewCommentsList.add(Comment.fromApi((Map) o));
            }
        }
        instance.owner = Account.fromMediaPage((Map) mediaMap.get("user"));
        if (mediaMap.containsKey("location") && mediaMap.get("location") != null) {
            Map location = (Map) mediaMap.get("location");
            if (location.containsKey("name")) {
                instance.locationName = (String) location.get("name");
            }
            if (location.containsKey("id")) {
                instance.locationId = (String) location.get("id");
            }
        }
        return instance;
    }

    private static void fixDate(Media instance) {
        if (instance.createdTime > 0 && instance.createdTime < INSTAGRAM_BORN_YEAR) {
            instance.createdTime *= 1000;
        }
    }

    public static Media fromMediaPage(Map pageMap) {
        Media instance = new Media();
        instance.id = (String) pageMap.get("id");
        instance.type = TYPE_IMAGE;
        if ((Boolean) pageMap.get("is_video")) {
            instance.type = TYPE_VIDEO;
            instance.videoUrls.standard = (String) pageMap.get("video_url");
            instance.videoViews = ((Double) pageMap.get("video_view_count")).intValue();
        }
        instance.imageUrl = (String) pageMap.get("display_url");
        instance.createdTime = ((Double) pageMap.get("taken_at_timestamp")).longValue();
        fixDate(instance);
        instance.shortcode = (String) pageMap.get("shortcode");
        instance.link = INSTAGRAM_URL + "p/" + instance.shortcode;
        instance.commentsCount = ((Double) ((Map) pageMap.get("edge_media_to_comment")).get("count")).intValue();
        instance.likesCount = ((Double) ((Map) pageMap.get("edge_media_preview_like")).get("count")).intValue();
        if (pageMap.containsKey("location") && pageMap.get("location") != null) {
            Map location = (Map) pageMap.get("location");
            if (location.containsKey("name")) {
                instance.locationName = (String) location.get("name");
            }
            if (location.containsKey("id")) {
                instance.locationId = (String) location.get("id");
            }
        }
//        fillImageUrls(instance, (String) pageMap.get("display_url"));
        String caption = (String) ((Map) ((Map) ((List) ((Map) pageMap.get("edge_media_to_caption")).get("edges")).get(0)).get("node")).get("text");
        if (caption != null) {
            instance.caption = caption;
        }
        instance.owner = Account.fromMediaPage((Map) pageMap.get("owner"));
        return instance;
    }

    public static Media fromLocationPage(Map mediaMap) {
        Media instance = new Media();
        mediaMap = (Map) mediaMap.get("node");
        instance.shortcode = (String) mediaMap.get("shortcode");
        instance.link = Endpoint.getMediaPageLinkByCode(instance.shortcode);
        instance.commentsCount = ((Double) ((Map) mediaMap.get("edge_media_to_comment")).get("count")).intValue();
        instance.likesCount = ((Double) ((Map) mediaMap.get("edge_liked_by")).get("count")).intValue();
        instance.ownerId = (String) ((Map) mediaMap.get("owner")).get("id");
        try {
            instance.caption = (String) mediaMap.get("caption");
        } catch (Exception e) {
            instance.caption = "";
        }

        instance.createdTime = ((Double) mediaMap.get("taken_at_timestamp")).longValue();
        fixDate(instance);
//        fillImageUrls(instance, (String) ((Map) ((List) mediaMap.get("thumbnail_resources")).get(0)).get("src"));
        instance.type = TYPE_IMAGE;
        if ((Boolean) mediaMap.get("is_video")) {
            instance.type = TYPE_VIDEO;
        }

        instance.id = (String) mediaMap.get("id");
        return instance;
    }

    public static Media fromTagPage(Map mediaMap) {
        Media instance = new Media();
        instance.shortcode = (String) mediaMap.get("shortcode");
        instance.link = Endpoint.getMediaPageLinkByCode(instance.shortcode);
        instance.commentsCount = ((Double) ((Map) mediaMap.get("edge_media_to_comment")).get("count")).intValue();
        instance.likesCount = ((Double) ((Map) mediaMap.get("edge_liked_by")).get("count")).intValue();
        instance.ownerId = (String) ((Map) mediaMap.get("owner")).get("id");
        try {
            instance.caption = (String) ((Map) ((Map) ((List) ((Map) mediaMap.get("edge_media_to_caption")).get("edges")).get(0)).get("node")).get("text");
        } catch (Exception e) {
            instance.caption = "";
        }

        instance.createdTime = ((Double) mediaMap.get("taken_at_timestamp")).longValue();
        fixDate(instance);
//        fillImageUrls(instance, (String) ((Map) ((List) mediaMap.get("thumbnail_resources")).get(0)).get("src"));
        instance.type = TYPE_IMAGE;
        if ((Boolean) mediaMap.get("is_video")) {
            instance.type = TYPE_VIDEO;
            instance.videoViews = ((Double) mediaMap.get("video_view_count")).intValue();
        }
        instance.imageUrl = (String) mediaMap.get("display_url");
        instance.id = (String) mediaMap.get("id");
        return instance;
    }

//    private static void fillImageUrls(final Media instance, String imageUrl) {
//        URL url;
//        try {
//            url = new URL(imageUrl);
//            String[] parts = url.getPath().split("/");
//            String imageName = parts[parts.length - 1];
//            instance.imageUrls.low = Endpoint.INSTAGRAM_CDN_URL + "t/s150x150/" + imageName;
//            instance.imageUrls.thumbnail = Endpoint.INSTAGRAM_CDN_URL + "t/s320x320/" + imageName;
//            instance.imageUrls.standard = Endpoint.INSTAGRAM_CDN_URL + "t/s640x640/" + imageName;
//            instance.imageUrls.high = Endpoint.INSTAGRAM_CDN_URL + "t/" + imageName;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void fillCarouselImageUrls(final CarouselMedia instance, String imageUrl) {
//        URL url;
//        try {
//            url = new URL(imageUrl);
//            String[] parts = url.getPath().split("/");
//            String imageName = parts[parts.length - 1];
//            instance.imageUrls.low = Endpoint.INSTAGRAM_CDN_URL + "t/s150x150/" + imageName;
//            instance.imageUrls.thumbnail = Endpoint.INSTAGRAM_CDN_URL + "t/s320x320/" + imageName;
//            instance.imageUrls.standard = Endpoint.INSTAGRAM_CDN_URL + "t/s640x640/" + imageName;
//            instance.imageUrls.high = Endpoint.INSTAGRAM_CDN_URL + "t/" + imageName;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//    }

    public static String getIdFromCode(String code) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        long id = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            id = id * 64 + alphabet.indexOf(c);
        }
        return id + "";
    }

    public static String getCodeFromId(String id) {
        String[] parts = id.split("_");
        id = parts[0];
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        StringBuilder code = new StringBuilder();
        long longId = Long.parseLong(id);
        while (longId > 0) {
            long index = longId % 64;
            longId = (longId - index) / 64;
            code.insert(0, alphabet.charAt((int) index));
        }
        return code.toString();
    }

    public static String getLinkFromId(String id) {
        String code = Media.getCodeFromId(id);
        return Endpoint.getMediaPageLinkByCode(code);
    }

    @Override
    public String toString() {
        return "Media{" +
                "id='" + id + '\'' +
                ", createdTime=" + createdTime +
                ", type='" + type + '\'' +
                ", link='" + link + '\'' +
                ", imageUrl=" + imageUrl +
                ", caption='" + caption + '\'' +
                ", videoUrls=" + videoUrls +
                ", shortcode='" + shortcode + '\'' +
                ", commentsCount=" + commentsCount +
                ", previewCommentsList=" + previewCommentsList +
                ", likesCount=" + likesCount +
                ", videoViews=" + videoViews +
                ", ownerId='" + ownerId + '\'' +
                ", owner=" + owner +
                ", locationName='" + locationName + '\'' +
                '}';
    }

    public List<String> getTags() {
        List<String> strs = new ArrayList<String>();
        try {
            Pattern MY_PATTERN = Pattern.compile("#([^\\r\\n\\t\\f\\v# ]+)");
            Matcher mat = MY_PATTERN.matcher(caption);
            while (mat.find()) {
                strs.add(mat.group(1));
            }
        } catch (NullPointerException e) {

        }
        return strs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        return link != null ? link.equals(media.link) : media.link == null;
    }

    @Override
    public int hashCode() {
        return link != null ? link.hashCode() : 0;
    }
}

package me.postaddict.instagram.scraper;

import com.google.gson.Gson;
import me.postaddict.instagram.scraper.domain.*;
import me.postaddict.instagram.scraper.exception.InstagramAuthException;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Instagram implements AuthenticatedInsta {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    private final int MAX_LOGIN = 3;
    private final int MAX_LIKE = 2;
    private final int MAX_COMMENT = 2;

    public OkHttpClient httpClient;
    public Gson gson;
    private String username;

    public Instagram(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    private Request withCsrfToken(Request request) {
        List<Cookie> cookies = httpClient.cookieJar()
                .loadForRequest(request.url());

        for (Iterator<Cookie> iterator = cookies.iterator(); iterator.hasNext(); ) {
            Cookie cookie = iterator.next();
            if (!cookie.name().equals("csrftoken")) {
                iterator.remove();
            }
        }

        if (!cookies.isEmpty()) {
            Cookie cookie = cookies.get(0);
            request = request.newBuilder()
                    .addHeader("X-CSRFToken", cookie.value())
                    .addHeader("Cookie", "csrftoken=" + cookie.value())
                    .build();

            return request;
        }
        return request;
    }

    public void basePage() throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.BASE_URL)
                .build();

        Response response = this.httpClient.newCall(request).execute();
        response.body().close();
    }

    public void logout() throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.LOGOUT_URL)
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        response.body().close();
    }

    public void login1(String username, String password, int num) throws IOException, IllegalAccessException {
        if (username == null || password == null) {
            throw new InstagramAuthException("Specify username and password");
        }
        basePage();
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(Endpoint.LOGIN_URL)
                .header("Referer", "https://www.instagram.com/accounts/login/")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String json = response.body().string();
        response.body().close();
        log.info("LOGIN1 response code " + response.code() + " response starts with " + json.charAt(0));
        if (response.code() == 200 && json.charAt(0) == '{') {
            log.info(json);
        }

        if (response.code() != 200 && num <= MAX_LOGIN) {
            log.severe(json.length() > 1000 ? json.substring(0, 1000) : json);
            log.info("TRY AGAIN");
            try {
                Thread.currentThread().sleep((long) (Math.random() * 10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            login1(username, password, num + 1);
        }
        this.username = username;
    }

    public Account login(String username, String password, int num) throws Exception {
        login1(username, password, 0);
        log.info("LOGIN1 PASSED NOW ACCOUNT PLEASE");
        Request request = new Request.Builder()
                .url(Endpoint.USER_SELF_URL)
                .header("Referer", Endpoint.LOGIN_URL)
                .get()
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String json = response.body().string();

        log.info("LOGIN response code " + response.code() + " response starts with " + json.charAt(0));
        if (response.code() == 200 && json.charAt(0) == '{') {
            log.info("SUPER \n" + json);
            log.info((String) gson.fromJson(json, Map.class).get("status"));
        }

        if (response.code() != 200 && num <= MAX_LOGIN) {
            response.body().close();
            Thread.currentThread().sleep((long) (Math.random() * 10000));
            return login(username, password, ++num);
        }


        response.body().close();
        if (json.charAt(0) != '{') {
            if (num < MAX_LOGIN) {
                return login(username, password, ++num);
            }
            throw new Exception("Nie zalogowalem");
        } else {
            try {
                String user = (String) ((Map) gson.fromJson(json, Map.class).get("form_data")).get("username");

                Account acc = getAccountByUsername(user);

                this.username = username;
                return acc;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Nie zalogowalem 2");
            }
        }
    }

    public Account getAccountById(long id) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.getAccountJsonInfoLinkByAccountId(id))
                .header("Referer", Endpoint.BASE_URL + "/")
                .build();
        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String jsonString = response.body().string();
        response.body().close();

        Map userJson = gson.fromJson(jsonString, Map.class);
        String shortCode = (String) ((Map) ((Map) ((List) ((Map) ((Map) ((Map) userJson.get("data")).get("user")).get("edge_web_discover_media")).get("edges")).get(0)).get("node")).get("shortcode");
        Media m = getMediaByCode(shortCode);
        return m.owner;
    }

    public Account getAccountByUsername(String username) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.getAccountJsonInfoLinkByUsername(username))
                .build();

        Response response = this.httpClient.newCall(request).execute();
        String jsonString = response.body().string();
        response.body().close();
        Map userJson = gson.fromJson(jsonString, Map.class);
        return Account.fromAccountPage((Map) userJson.get("user"));
    }

    public List<Media> getMedias(String username, int count) throws IOException {
        int index = 0;
        ArrayList<Media> medias = new ArrayList<Media>();
        String maxId = "";
        boolean isMoreAvailable = true;

        while (index < count && isMoreAvailable) {
            Request request = new Request.Builder()
                    .url(Endpoint.getAccountMediasJsonLink(username, maxId))
                    .build();

            Response response = this.httpClient.newCall(request).execute();
            String jsonString = response.body().string();
            response.body().close();

            Map map = gson.fromJson(jsonString, Map.class);
            Map userMap = (Map) map.get("user");
            List items = (List) (((Map) userMap.get("media")).get("nodes"));

            for (Object item : items) {
                if (index == count) {
                    return medias;
                }
                index++;
                Map mediaMap = (Map) item;
                Media media = Media.fromApi(mediaMap);
                medias.add(media);
                maxId = media.id;
            }
            isMoreAvailable = (Boolean) ((Map) (((Map) userMap.get("media")).get("page_info"))).get("has_next_page");
        }
        return medias;
    }

    public Media getMediaByUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url + "/?__a=1")
                .build();

        Response response = this.httpClient.newCall(request).execute();
        String jsonString = response.body().string();
        response.body().close();
        Map pageMap = gson.fromJson(jsonString, Map.class);
        return Media.fromMediaPage((Map) ((Map) pageMap.get("graphql")).get("shortcode_media"));
    }

    public Media getMediaByCode(String code) throws IOException {
        return getMediaByUrl(Endpoint.getMediaPageLinkByCode(code));
    }

    public Tag getTagByName(String tagName) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.getTagJsonByTagName(tagName))
                .build();

        Response response = this.httpClient.newCall(request).execute();
        String jsonString = response.body().string();
        response.body().close();

        Map tagJson = gson.fromJson(jsonString, Map.class);
        return Tag.fromSearchPage((Map) tagJson.get("tag"));

    }

    public List<Media> getLocationMediasById(String locationId, int count) throws IOException {
        int index = 0;
        ArrayList<Media> medias = new ArrayList<Media>();
        String offset = "";
        boolean hasNext = true;

        while (index < count && hasNext) {
            Request request = new Request.Builder()
                    .url(Endpoint.getMediasJsonByLocationIdLink(locationId, offset))
                    .header("Referer", Endpoint.BASE_URL + "/")
                    .build();

            Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
            String jsonString = response.body().string();
            response.body().close();

            Map locationMap = gson.fromJson(jsonString, Map.class);
            List nodes = (List) ((Map) ((Map) locationMap.get("location")).get("media")).get("nodes");
            for (Object node : nodes) {
                if (index == count) {
                    return medias;
                }
                index++;
                Map mediaMap = (Map) node;
                Media media = Media.fromLocationPage(mediaMap);
                medias.add(media);
            }
            hasNext = (Boolean) ((Map) ((Map) ((Map) locationMap.get("location")).get("media")).get("page_info")).get("has_next_page");
            offset = (String) ((Map) ((Map) ((Map) locationMap.get("location")).get("media")).get("page_info")).get("end_cursor");
        }
        return medias;
    }

    public List<Media> getMediasByTag(String tag, int count, String maxMediaId) throws IOException {
        int index = 0;
        ArrayList<Media> medias = new ArrayList<Media>();
        if (!maxMediaId.equals(""))
            maxMediaId = MediaUtil.getCodeFromId(maxMediaId);
        boolean hasNext = true;
        boolean finish = false;
        String maxId = "";
        while (index < count && hasNext && !finish) {
            Request request = new Request.Builder()
                    .url(Endpoint.getMediasJsonByTagLink(tag, maxId))
                    .header("Referer", Endpoint.BASE_URL + "/")
                    .build();
            Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
            String jsonString = response.body().string();
            response.body().close();
            System.out.println("Endpoint.getMediasJsonByTagLink(tag, maxId) " + Endpoint.getMediasJsonByTagLink(tag, maxId));
            Map tagMap = gson.fromJson(jsonString, Map.class);

            List nodes = (List) ((Map) ((Map) ((Map) tagMap.get("graphql")).get("hashtag")).get("edge_hashtag_to_media")).get("edges");
            for (Object node : nodes) {
                if (index == count) {
                    return medias;
                }
                index++;
                Map mediaMap = (Map) node;
                Media media = Media.fromTagPage((Map) mediaMap.get("node"));
                if (media.shortcode.equals(maxMediaId)) {
                    finish = true;
                    break;
                }
                medias.add(media);
            }
            hasNext = (Boolean) ((Map) ((Map) ((Map) ((Map) tagMap.get("graphql")).get("hashtag")).get("edge_hashtag_to_media")).get("page_info")).get("has_next_page");
            maxId = (String) ((Map) ((Map) ((Map) ((Map) tagMap.get("graphql")).get("hashtag")).get("edge_hashtag_to_media")).get("page_info")).get("end_cursor");
        }
        return medias;
    }

    public List<Media> getTopMediasByTag(String tag) throws IOException {
        ArrayList<Media> medias = new ArrayList<Media>();
        String maxId = "";

        Request request = new Request.Builder()
                .url(Endpoint.getMediasJsonByTagLink(tag, maxId))
                .header("Referer", Endpoint.BASE_URL + "/")
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String jsonString = response.body().string();
        response.body().close();

        Map tagMap = gson.fromJson(jsonString, Map.class);
        List nodes = (List) ((Map) ((Map) tagMap.get("tag")).get("top_posts")).get("nodes");
        for (Object node : nodes) {
            Map mediaMap = (Map) node;
            Media media = Media.fromTagPage(mediaMap);
            medias.add(media);
        }
        return medias;
    }

    public List<Comment> getCommentsByMediaCode(String code, int count) throws IOException {
        List<Comment> comments = new ArrayList<Comment>();
        Request request = new Request.Builder()
                .url(Endpoint.getCommentsBeforeCommentIdByCode(code, 20))
                .header("Referer", Endpoint.BASE_URL + "/")
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String jsonString = response.body().string();
        response.body().close();
        Map commentsMap = gson.fromJson(jsonString, Map.class);
        List nodes = (List) ((Map) ((Map) ((Map) commentsMap.get("data")).get("shortcode_media")).get("edge_media_to_comment")).get("edges");
        for (Object node : nodes) {

            Map commentMap = (Map) node;
            Comment comment = Comment.fromApi(commentMap);
            comments.add(comment);
        }

        return comments;
    }

    public List<Account> getUserLikesByMediaCode(String code) throws Exception {
        List<Account> comments = new ArrayList<>();
        boolean hasNext = true;

        Request request = new Request.Builder()
                .url(Endpoint.getMediaLikesByShortcode(code))
                .header("Referer", Endpoint.BASE_URL + "/")
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String jsonString = response.body().string();
        response.body().close();
        Map commentsMap = gson.fromJson(jsonString, Map.class);
        if (!commentsMap.get("status").equals("ok"))
            throw new Exception("Wait a minute!");

        List nodes = (List) ((Map) ((Map) ((Map) commentsMap.get("data")).get("shortcode_media")).get("edge_liked_by")).get("edges");
        for (Object node : nodes) {

            Map commentMap = (Map) ((Map) node).get("node");
            Account comment = Account.fromMediaPage(commentMap);
            comments.add(comment);
        }


        return comments;
    }

    public void likeMediaByCode(String code) throws IOException {
        String url = Endpoint.getMediaLikeLink(Media.getIdFromCode(code));
        log.info("LIKE MEDIA = " + code);
        Request request = new Request.Builder()
                .url(url)
                .header("Referer", Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(new FormBody.Builder().build())
                .build();


        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String json = response.body().string();
        log.info("STATUS = " + response.code());
        response.body().close();

//        if ((response.code() != 200 || (json.charAt(0) != '{')) && num < MAX_LIKE) {
//            try {
//                Thread.currentThread().sleep(10000 + (long) (Math.random() * 10000));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            likeMediaByCode(code, ++num);
//        }

    }

    public List<Account> getFollows(long userId, int count) throws IOException {
        boolean hasNext = true;
        List<Account> follows = new ArrayList<Account>();
        String followsLink = Endpoint.getFollowsLinkVariables(userId, 200, "");
        while (follows.size() < count && hasNext) {
            Request request = new Request.Builder()
                    .url(followsLink)
                    .header("Referer", Endpoint.BASE_URL + "/")
                    .build();

            Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
            String jsonString = response.body().string();
            response.body().close();

            Map commentsMap = gson.fromJson(jsonString, Map.class);
            Map edgeFollow = (Map) ((Map) ((Map) commentsMap.get("data")).get("user")).get("edge_follow");
            List edges = (List) edgeFollow.get("edges");
            for (Object edgeObj : edges) {
                Account account = account((Map) edgeObj);
                follows.add(account);
                if (count == follows.size()) {
                    return follows;
                }
            }
            boolean hasNexPage = (Boolean) ((Map) edgeFollow.get("page_info")).get("has_next_page");
            if (hasNexPage) {
                followsLink = Endpoint.getFollowsLinkVariables(userId, 200, (String) ((Map) edgeFollow.get("page_info")).get("end_cursor"));
                hasNext = true;
            } else {
                hasNext = false;
            }
        }
        return follows;
    }

    private Account account(Map edgeObj) {
        Account account = new Account();
        Map edgeNode = (Map) edgeObj.get("node");
        account.id = Long.valueOf((String) edgeNode.get("id"));
        account.username = (String) edgeNode.get("username");
        account.profilePicUrl = (String) edgeNode.get("profile_pic_url");
        account.isVerified = (Boolean) edgeNode.get("is_verified");
        account.fullName = (String) edgeNode.get("full_name");
        return account;
    }

    public List<Account> getFollowers(long userId, int count) throws IOException {
        boolean hasNext = true;
        List<Account> followers = new ArrayList<Account>();
        String followsLink = Endpoint.getFollowersLinkVariables(userId, 200, "");
        while (followers.size() < count && hasNext) {
            Request request = new Request.Builder()
                    .url(followsLink)
                    .header("Referer", Endpoint.BASE_URL + "/")
                    .build();

            Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
            String jsonString = response.body().string();
            response.body().close();

            Map commentsMap = gson.fromJson(jsonString, Map.class);
            Map edgeFollow = (Map) ((Map) ((Map) commentsMap.get("data")).get("user")).get("edge_followed_by");
            List edges = (List) edgeFollow.get("edges");
            for (Object edgeObj : edges) {
                Account account = account((Map) edgeObj);
                followers.add(account);
                if (count == followers.size()) {
                    return followers;
                }
            }
            boolean hasNexPage = (Boolean) ((Map) edgeFollow.get("page_info")).get("has_next_page");
            if (hasNexPage) {
                followsLink = Endpoint.getFollowersLinkVariables(userId, 200, (String) ((Map) edgeFollow.get("page_info")).get("end_cursor"));
                hasNext = true;
            } else {
                hasNext = false;
            }
        }
        return followers;
    }

    public List<Location> getLocations(String search, int count) throws IOException {
        int index = 0;
        ArrayList<Location> locations = new ArrayList<Location>();
        String maxId = "";
        boolean isMoreAvailable = true;


        Request request = new Request.Builder()
                .url(Endpoint.getLocationSearchJsonLink(search))
                .build();

        Response response = this.httpClient.newCall(request).execute();
        String jsonString = response.body().string();
        response.body().close();

        Map map = gson.fromJson(jsonString, Map.class);
        List items = (List) map.get("places");
        System.out.println(map.get("places"));


        for (Object item : items) {
            if (index == count) {
                return locations;
            }
            index++;
            Map mediaMap = (Map) ((Map) ((Map) item).get("place")).get("location");
            Location location = Location.fromApi(mediaMap);
            locations.add(location);
        }


        return locations;
    }

    public void unlikeMediaByCode(String code) throws IOException {
        String url = Endpoint.getMediaUnlikeLink(Media.getIdFromCode(code));
        Request request = new Request.Builder()
                .url(url)
                .header("Referer", Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(new FormBody.Builder().build())
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        response.body().close();
    }

    public Comment addMediaComment(String code, String commentText) throws Exception {
        String url = Endpoint.addMediaCommentLink(Media.getIdFromCode(code));
        FormBody formBody = new FormBody.Builder()
                .add("comment_text", commentText)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .header("Referer", Endpoint.getMediaPageLinkByCode(code) + "/")
                // .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")
                .post(formBody)
                .build();
        basePage();
        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        String jsonString = response.body().string();
        if (response.code() == 403) {
            throw new Exception(jsonString);
        }

        response.body().close();

        Map commentMap = gson.fromJson(jsonString, Map.class);
        return Comment.fromApi(commentMap);
    }

    public void deleteMediaComment(String code, String commentId) throws IOException {
        String url = Endpoint.deleteMediaCommentLink(Media.getIdFromCode(code), commentId);
        Request request = new Request.Builder()
                .url(url)
                .header("Referer", Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(new FormBody.Builder().build())
                .build();

        Response response = this.httpClient.newCall(withCsrfToken(request)).execute();
        response.body().close();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

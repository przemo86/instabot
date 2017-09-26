package pl.szewczyk.instagram;

import org.jinstagram.Instagram;
import org.jinstagram.InstagramBase;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by przem on 21.09.2017.
 */
@Component
public class InstagramUtils {
    private static final String TAG_SEARCH = "https://api.instagram.com/v1/tags/{tag-name}/media/recent?access_token={access-token}";
    private final Instagram instagram = new Instagram(InstaConstants.ClientID);

    public List<MediaFeedData> searchTag(String tag, String authToken) {
        String uri = TAG_SEARCH.replace("{tag-name}", tag).replace("{access-token}", authToken);

        String response = null;
        try {
            response = doGet(new URL(uri));
            MediaFeed mediaFeed = InstagramBase.createObjectFromResponse(MediaFeed.class, response);
            if (mediaFeed.getMeta().getCode() == 200)
                if (mediaFeed.getData().size() > 0)
                    return mediaFeed.getData();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InstagramException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String doGet(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());
            return response.toString();
        } catch (IOException e) {
            System.out.println("EERROORR " + e.getMessage());
        } finally {
            conn.disconnect();
            return null;
        }
    }
}

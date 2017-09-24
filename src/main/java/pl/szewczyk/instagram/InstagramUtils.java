package pl.szewczyk.instagram;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by przem on 21.09.2017.
 */
@Component
public class InstagramUtils {
    private static final String TAG_SEARCH = "https://api.instagram.com/v1/tags/{tag-name}/media/recent?access_token={access-token}";

    public String searchTag(String tag, String authToken) {
        Logger.getGlobal().severe("START");
        String uri = TAG_SEARCH.replace("{tag-name}", tag).replace("{access-token}", authToken);
        Logger.getGlobal().severe("URI " + uri);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(uri).openConnection();
            conn.setRequestMethod("GET");

//            conn.setDoOutput(true);
            System.out.println("CONN + " + conn);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            System.out.println("HERE " + in);
            String inputLine;
            StringBuffer response = new StringBuffer();
            System.out.println("CONN " + conn);

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

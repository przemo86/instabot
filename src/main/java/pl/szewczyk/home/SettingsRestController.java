package pl.szewczyk.home;

import me.postaddict.instagram.scraper.Endpoint;
import me.postaddict.instagram.scraper.domain.Account;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.szewczyk.instagram.InstaConstants;

/**
 * Created by przem on 04.01.2018.
 */
@RestController
public class SettingsRestController {
    @RequestMapping(value = "/instalogin", method = RequestMethod.GET)
    public String instalogin(@RequestParam("login") String login) throws Exception {
        System.out.println("LOGIN " + login);
        InstaConstants instaConstants = new InstaConstants();
        try {
            Account acc = instaConstants.getInstagramLoggedIn(login).getAccountByUsername(login);
            return acc.fullName;
        } catch (Exception e) {
            try {
                OkHttpClient httpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Endpoint.BASE_URL)
                        .build();

                Response response = null;

                System.out.println("4");
                response = httpClient.newCall(request).execute();
                System.out.println("5");
                String res = response.body().string();

                response.body().close();
                return res;
            } catch (Exception e1) {
                return "NORE";
            }

        }
    }
}

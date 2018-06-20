package pl.szewczyk.projects;

import me.postaddict.instagram.scraper.domain.Location;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.szewczyk.instagram.InstaConstants;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by przem on 19.12.2017.
 */
@RestController
public class SearchRest {

    private InstaConstants instaConstants = new InstaConstants();

    @RequestMapping(value = "/location", method = RequestMethod.GET, produces = "application/json")
    public List<Location> greeting(@RequestParam(value = "search") String search) {
        System.out.println("hrere");
        try {
            return instaConstants.getInstagramAnonymous().getLocations(search, 20);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/blockedusers", method = RequestMethod.GET, produces = "application/json")
    public List<BlacklistedUser> getBlackListed(@RequestParam(value = "search") String search) {
        System.out.println("blavla");
        try {

            return instaConstants.getInstagramAnonymous().searchAccounts(search, 20)
                    .stream().map(acc -> {
                        BlacklistedUser bl = new BlacklistedUser();
                        bl.setUsername(acc.username);
                        bl.setProfilePicUrl(acc.profilePicUrl);
                        return bl;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

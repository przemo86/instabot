package pl.szewczyk.projects;

import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import pl.szewczyk.instagram.InstaConstants;

/**
 * Created by przem on 20.09.2017.
 */

public class ProjectLikeJob extends ProjectJob {


    public void like(MediaFeedData mediaFeedData, String authToken) throws InstagramException {
        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
        Instagram instagram = new Instagram(accessToken);

        instagram.setUserLike(mediaFeedData.getId());
    }

    @Override
    void doJob(MediaFeedData mediaFeedData, String comment, String authToken) throws InstagramException {
//        like(mediaFeedData, authToken);
    }
}

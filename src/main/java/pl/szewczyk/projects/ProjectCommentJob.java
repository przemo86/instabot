package pl.szewczyk.projects;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.domain.Media;

import java.util.logging.Logger;

/**
 * Created by przem on 20.09.2017.
 */

public class ProjectCommentJob extends ProjectJob {
    protected Logger log = Logger.getLogger(this.getClass().getName());

    public void comment(Media mediaFeedData, String comment, Instagram instagram) throws Exception {
        instagram.addMediaComment(mediaFeedData.shortcode, comment);
    }

//    @Override
//    void doJob(Media mediaFeedData, String comment, Instagram instagram) throws Exception {
//        comment(mediaFeedData, comment, instagram);
//    }
}

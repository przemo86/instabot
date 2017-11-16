package pl.szewczyk.projects;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.domain.Media;

import java.io.IOException;

/**
 * Created by przem on 20.09.2017.
 */

public class ProjectCommentJob extends ProjectJob {
    public void comment(Media mediaFeedData, String comment, Instagram instagram) throws IOException {
        try {
            instagram.addMediaComment(mediaFeedData.shortcode, comment);
        } catch (Exception r) {
            System.out.println("NIE UDALO SIE SKOMENTOWAC...");
        }
    }

    @Override
    void doJob(Media mediaFeedData, String comment, Instagram instagram) throws IOException {
        comment(mediaFeedData, comment, instagram);
    }
}

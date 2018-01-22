package pl.szewczyk.projects;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.domain.Media;

import java.io.IOException;

/**
 * Created by przem on 20.09.2017.
 */

public class ProjectLikeJob extends ProjectJob {


    public void like(Media media, Instagram instagram) throws IOException {
        instagram.likeMediaByCode(media.shortcode);
    }

//    @Override
//    void doJob(Media media, String comment, Instagram instagram) throws IOException {
//        like(media, instagram);
//    }
}

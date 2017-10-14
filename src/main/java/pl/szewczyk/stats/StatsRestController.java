package pl.szewczyk.stats;

import org.jinstagram.Instagram;
import org.jinstagram.entity.media.MediaInfoFeed;
import org.jinstagram.exceptions.InstagramException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.projects.ProjectRepository;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.util.List;

/**
 * Created by przem on 11.09.2017.
 */
@RestController
public class StatsRestController {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    @RequestMapping(path = "stats/project", method = RequestMethod.GET)
    public ResponseEntity<List<Media>> getStatsMedia(@RequestParam(name = "id") Long id, Principal principal) {

//        Instagram instagram = new Instagram()
        Instagram instagram = new Instagram(InstaConstants.ClientID, InstaConstants.ClientSecret);


        System.out.println();
        List<Media> medias = em.createNativeQuery("select * from instabot.media where random() < 0.25", Media.class).getResultList();
        for (Media media : medias) {
            try {
                MediaInfoFeed data = instagram.getMediaInfo(media.getMediaId());
                media.setUser("@"+data.getData().getUser().getUserName());
                media.setLocation(data.getData().getLocation());

                data.getData().getUser();
                        instagram.getUserInfo(data.getData().getUser().getId()).getData();
            } catch (InstagramException e) {

            }
        }
//        return em.createQuery("from Media m join m.statistic s where s.id = :statId order by m.id", Media.class).getResultList();
        return ResponseEntity.ok(medias);
    }
}

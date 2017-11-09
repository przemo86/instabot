package pl.szewczyk.stats;

import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.comments.MediaCommentResponse;
import org.jinstagram.entity.likes.LikesFeed;
import org.jinstagram.exceptions.InstagramException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.projects.Project;
import pl.szewczyk.projects.ProjectRepository;

import javax.persistence.EntityManager;

/**
 * Created by przem on 11.09.2017.
 */
@RestController
public class StatsRestController {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("stats/like")
    public ResponseEntity<String> like(@RequestParam(name = "id") String mediaId) {
        System.out.println(mediaId);
        InstaUser user = (InstaUser) em.createNativeQuery("select i.* from instabot.account_instauser ai join instabot.instauser i on ai.instausers_id = i.id JOIN instabot.project p on i.instausername = p.instagramaccount join instabot.statistics on p.id = statistics.projectid join instabot.statistics_media sm on statistics.id = sm.statistic_id join instabot.media m on sm.media_id = m.id where m.mediaid = :id limit 1", InstaUser.class).setParameter("id", mediaId).getSingleResult();

        Instagram instagram = new Instagram(new Token(user.getAccessToken(), InstaConstants.ClientSecret));
        try {
            if (instagram.getUserLikes(mediaId).getUserList().stream().filter(u -> u.getUserName().equals(user.getInstaUserName())).count() == 0) {
                LikesFeed lf = instagram.setUserLike(mediaId);
                return ResponseEntity.ok("Liked");
            } else {
                return ResponseEntity.status(500).body("Already liked");
            }

        } catch (InstagramException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("stats/comment")
    public ResponseEntity<String> comment(@RequestParam(name = "id") String mediaId) {
        InstaUser user = (InstaUser) em.createNativeQuery("select i.* from instabot.account_instauser ai join instabot.instauser i on ai.instausers_id = i.id JOIN instabot.project p on i.instausername = p.instagramaccount join instabot.statistics on p.id = statistics.projectid join instabot.statistics_media sm on statistics.id = sm.statistic_id join instabot.media m on sm.media_id = m.id where m.mediaid = :id limit 1", InstaUser.class).setParameter("id", mediaId).getSingleResult();
        Project project = (Project) em.createNativeQuery("select p.* from instabot.project p" +
                "  join instabot.statistics s on p.id = s.projectid " +
                "  join instabot.statistics_media sm on s.id = sm.statistic_id " +
                "  join instabot.media m on sm.media_id = m.id where m.mediaid = :id limit 1", Project.class).setParameter("id", mediaId).getSingleResult();

        Instagram instagram = new Instagram(new Token(user.getAccessToken(), InstaConstants.ClientSecret));
        try {
            if (instagram.getMediaComments(mediaId).getCommentDataList().stream().filter(c -> Long.valueOf(c.getCommentFrom().getId()).equals(user.getId())).count() > 0)
                return ResponseEntity.status(500).body("Commented");
            MediaCommentResponse mcr = instagram.setMediaComments(mediaId, project.getCommentString());
            System.out.println("MM " + mcr.getCommentData().getCreatedTime());
            return ResponseEntity.ok("Commented");
        } catch (InstagramException e) {
            System.out.println("ERROR");
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

//    @RequestMapping(path = "stats/project", method = RequestMethod.GET, produces = "application/json")
//    public ResponseEntity<String> getStatsMedia(@RequestParam(name = "id") Long id, Principal principal) {
//
//        String authToken = em.createNativeQuery("SELECT accesstoken FROM instabot.instauser ORDER BY RANDOM() LIMIT 1").getSingleResult().toString();
//        System.out.println(authToken);
//        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
//        System.out.println(accessToken);
//        InstagramConfig config = new InstagramConfig();
//        config.setRetryOnServerError(true);
//        config.setConnectionKeepAlive(true);
//        Instagram instagram = new Instagram(accessToken, config);
//
//
//        System.out.println("od " + id);
////        List<Media> medias = em.createNativeQuery("select m.* from instabot.media m join instabot.statistics_media sm on m.id = sm.media_id join instabot.statistics s on sm.statistic_id = s.id limit 5", Media.class)
//        List<Media> medias = em.createNativeQuery("select m.* from instabot.media m join instabot.statistics_media sm on m.id = sm.media_id where sm.statistic_id = :id", Media.class)
//                .setParameter("id", id)
//                .getResultList();
//        System.out.println("cnt " + medias.size());
//        Set<Thread> threads = new HashSet<>(medias.size());
//        for (Media media : medias) {
//            Thread thread = new Thread(() -> {
//                try {
//                    System.out.println("1");
//                    MediaInfoFeed data = instagram.getMediaInfo(media.getMediaId());
//                    System.out.println("2");
//                    UserInfo userInfo = instagram.getUserInfo(data.getData().getUser().getId());
//                    System.out.println("3");
//                    MediaCommentsFeed commentsFeed = instagram.getMediaComments(media.getMediaId());
//                    System.out.println("4");
//                    LikesFeed likesFeed = instagram.getUserLikes(media.getMediaId());
//                    System.out.println("5");
//
//                    media.setUserName(data.getData().getUser().getUserName());
//                    media.setUserProfileImage(data.getData().getUser().getProfilePictureUrl());
//                    System.out.println("6");
//                    media.setLocation(data.getData().getLocation());
//                    System.out.println("7");
//                    if (commentsFeed.getCommentDataList() != null) {
//                        media.setComments(commentsFeed.getCommentDataList().stream().map(c -> c.getText()).collect(Collectors.toList()));
//                    } else {
//                        media.setComments(new ArrayList<>());
//                    }
//                    System.out.println("8");
//                    if (likesFeed.getUserList() != null) {
//                        media.setUserLikes(likesFeed.getUserList().stream().map(u -> u.getUserName()).collect(Collectors.toList()));
//                    } else {
//                        media.setUserLikes(new ArrayList<>());
//                    }
//                    System.out.println("9");
//
//                    media.setUserFollowed(userInfo.getData().getCounts().getFollowedBy());
//                    System.out.println("10");
//
//                } catch (InstagramException e) {
//                    e.printStackTrace();
//                    return;
//                }
//            });
//            System.out.println("STARTUJ TE WARTKI");
//            thread.start();
//            threads.add(thread);
//        }
//
//        for (Thread thread : threads) {
//            try {
//                System.out.println("DOLACZYSZ?");
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
////        return em.createQuery("from Media m join m.statistic s where s.id = :statId order by m.id", Media.class).getResultList();
//        System.out.println("XXX");
//        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//        if (medias.stream().filter(m -> m.getUserName() != null).count() > 0) {
//            System.out.println("BEDZIE");
//            return ResponseEntity.ok(gson.toJson(medias));
//        }
//        else {
//            System.out.println("NIE BEDZIE");
//            return ResponseEntity.badRequest().body("");
//        }
//    }

//    @RequestMapping(path = "stats/tags", method = RequestMethod.GET, produces = "application/json")
//    public ResponseEntity<String> getStatsMedia(@RequestParam(name = "id") Long projectId) {
//        List<Object[]> ret = em.createNativeQuery("select tags, count(DISTINCT m.mediaid) mcnt, " +
//                "  (select count(DISTINCT media.mediaid) from instabot.media join instabot.statistics_media on media.id = statistics_media.media_id JOIN instabot.statistics ON statistics_media.statistic_id = statistics.id where projectid = :projectid) acnt" +
//                "  from " +
//                "    instabot.tags t join " +
//                "    instabot.media m on t.media_id = m.id join " +
//                "    instabot.statistics_media sm on m.id = sm.media_id join " +
//                "    instabot.statistics s on sm.statistic_id = s.id " +
//                "where s.projectid = :projectid " +
//                "group by tags " +
//                "order by 2 desc").setParameter("projectid", projectId).getResultList();
//
//        StringBuilder sb = new StringBuilder("[");
//        for (Object[] o : ret) {
//            sb.append("{");
//            sb.append("\"tag\":\"" + o[0] + "\",");
//            sb.append("\"hits\":\"" + o[1] + "\",");
//            sb.append("\"total\":\"" + o[2] + "\"");
//            sb.append("},");
//        }
//        if (sb.length() > 1)
//            sb.deleteCharAt(sb.length() - 1);
//        sb.append("]");
//        Gson gson = new Gson();
////        gson.toJson(new )
//        return ResponseEntity.ok(sb.toString());
//    }
}

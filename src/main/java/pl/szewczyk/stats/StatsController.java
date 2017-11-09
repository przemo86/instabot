package pl.szewczyk.stats;

import org.jinstagram.Instagram;
import org.jinstagram.InstagramConfig;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.comments.MediaCommentsFeed;
import org.jinstagram.entity.likes.LikesFeed;
import org.jinstagram.entity.media.MediaInfoFeed;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.exceptions.InstagramException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.projects.Project;
import pl.szewczyk.projects.ProjectRepository;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by przem on 11.09.2017.
 */
@Controller
public class StatsController {

    @ModelAttribute("module")
    String module() {
        return "stats";
    }

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;

    @GetMapping("stats")
    public String listProjects(Model model, @RequestParam(name = "id") Long projectId, HttpServletRequest request) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String name = auth.getName(); //get logged in username
        Project project = projectRepository.znajdz(projectId);
        model.addAttribute("project", project);
        request.getSession(false).setAttribute("project", project);
        List<Object[]> ret = em.createNativeQuery("select * from (select s.time, count(m.mediaid), s.id, s.kind from instabot.statistics s " +
                "   left outer join instabot.statistics_media sm on s.id = sm.statistic_id " +
                "   left outer join instabot.media m on sm.media_id = m.id where s.projectid = :project" +
                "   group by s.time, s.id order by s.time) t ").setParameter("project", projectId).getResultList();
//        List<Object[]> ret = em.createNativeQuery("select * from (select s.time, (case when s.kind = 'L' then trunc(abs(cos(radians(EXTRACT('minute' from s.time)*6))+1)*3) else " +
//                "   trunc(abs(cos(radians(EXTRACT('minute' from s.time)*7))+2)*2) end), s.id, s.kind from instabot.statistics s " +
//                "   left outer join instabot.statistics_media sm on s.id = sm.statistic_id " +
//                "   left outer join instabot.media m on sm.media_id = m.id where s.projectid = :project" +
//                "   group by s.time,s.id order by s.time) t limit 100").setParameter("project", projectId).getResultList();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("chartLikeData", ret.stream().filter(a -> (a[3]).equals('L')).map(a -> a[1]).collect(Collectors.toList()));
        model.addAttribute("chartLikeLabels", ret.stream().filter(a -> (a[3]).equals('L')).map(a -> sdf.format(a[0]) + "###" + a[2]).collect(Collectors.toList()));

        model.addAttribute("chartCommentData", ret.stream().filter(a -> (a[3]).equals('C')).map(a -> a[1]).collect(Collectors.toList()));
        model.addAttribute("chartCommentLabels", ret.stream().filter(a -> (a[3]).equals('C')).map(a -> sdf.format(a[0]) + "###" + a[2]).collect(Collectors.toList()));

        return "stats/stats";
    }

    @GetMapping("activity")
    public String getActivity(Model model, @RequestParam("id") Long id, HttpServletRequest request) {
        String authToken = em.createNativeQuery("SELECT accesstoken FROM instabot.instauser ORDER BY RANDOM() LIMIT 1").getSingleResult().toString();
        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
        InstagramConfig config = new InstagramConfig();
        config.setRetryOnServerError(true);
        config.setConnectionKeepAlive(true);
        Instagram instagram = new Instagram(accessToken, config);

        request.getSession(false).setAttribute("instagram", instagram);

//        List<Media> medias = em.createNativeQuery("select m.* from instabot.media m join instabot.statistics_media sm on m.id = sm.media_id join instabot.statistics s on sm.statistic_id = s.id limit 5", Media.class)
        List<Media> medias = em.createNativeQuery("select m.* from instabot.media m join instabot.statistics_media sm on m.id = sm.media_id where sm.statistic_id = :id", Media.class)
                .setParameter("id", id)
                .getResultList();
        Set<Thread> threads = new HashSet<>(medias.size());
        for (Media media : medias) {
            Thread thread = new Thread(() -> {
                try {
                    MediaInfoFeed data = instagram.getMediaInfo(media.getMediaId());
                    UserInfo userInfo = instagram.getUserInfo(data.getData().getUser().getId());
                    MediaCommentsFeed commentsFeed = instagram.getMediaComments(media.getMediaId());
                    LikesFeed likesFeed = instagram.getUserLikes(media.getMediaId());

                    media.setUserName(data.getData().getUser().getUserName());
                    media.setUserProfileImage(data.getData().getUser().getProfilePictureUrl());
                    media.setLocation(data.getData().getLocation());
                    if (commentsFeed.getCommentDataList() != null) {
                        media.setComments(commentsFeed.getCommentDataList().stream().map(c -> c.getText()).collect(Collectors.toList()));
                    } else {
                        media.setComments(new ArrayList<>());
                    }
                    if (likesFeed.getUserList() != null) {
                        media.setUserLikes(likesFeed.getUserList().stream().map(u -> u.getUserName()).collect(Collectors.toList()));
                    } else {
                        media.setUserLikes(new ArrayList<>());
                    }

                    media.setUserFollowed(userInfo.getData().getCounts().getFollowedBy());

                } catch (InstagramException e) {
                    e.printStackTrace();
                    return;
                }
            });
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("project", request.getSession(false).getAttribute("project"));
        model.addAttribute("mediaList", medias);

        return "fragments/components :: activityStats";
    }

    @GetMapping("activity/tags")
    public String getTags(Model model, HttpServletRequest request) {
        Project project = (Project) request.getSession(false).getAttribute("project");
        List<Object[]> ret = em.createNativeQuery("select tags, count(DISTINCT m.mediaid) mcnt, " +
                "  (select count(DISTINCT media.mediaid) from instabot.media join instabot.statistics_media on media.id = statistics_media.media_id JOIN instabot.statistics ON statistics_media.statistic_id = statistics.id where projectid = :projectid) acnt" +
                "  from " +
                "    instabot.tags t join " +
                "    instabot.media m on t.media_id = m.id join " +
                "    instabot.statistics_media sm on m.id = sm.media_id join " +
                "    instabot.statistics s on sm.statistic_id = s.id " +
                "where s.projectid = :projectid " +
                "group by tags " +
                "order by 2 desc").setParameter("projectid", project.getId()).getResultList();

        List<Stat> stats = new ArrayList<>();
        for (Object[] o : ret) {
            Stat s = new Stat();
            s.setTag((String) o[0]);
            s.setHits(((BigInteger) o[1]).doubleValue());
            s.setTotal(((BigInteger) o[2]).doubleValue());
            stats.add(s);
        }

        model.addAttribute("mediatags", stats);

        return "fragments/components :: hashtags";
    }

    @GetMapping("/stats/media")
    public String getModalContent(Model model, @RequestParam("type") String type, @RequestParam("id") String id, HttpServletRequest request) {
        System.out.println("asadasdasd");
        Instagram instagram = (Instagram) request.getSession(false).getAttribute("instagram");

        try {
            MediaInfoFeed data = null;
            data = instagram.getMediaInfo(id);
            System.out.println(data);
            List<String> items = null;
            if (type.equals("like")) {

                items = instagram.getUserLikes(id).getUserList().stream().map(u -> u.getUserName()).collect(Collectors.toList());

            } else if (type.equals("comment")) {
                items = instagram.getMediaComments(id).getCommentDataList().stream().map(c -> c.getCommentFrom().getUsername() + " - " + c.getText()).collect(Collectors.toList());
            }
            model.addAttribute("items", items);
        } catch (InstagramException e) {
            e.printStackTrace();
        }
        return "fragments/components :: modal-content";
    }
}

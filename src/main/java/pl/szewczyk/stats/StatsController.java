package pl.szewczyk.stats;

import me.postaddict.instagram.scraper.exception.InstagramException;
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
import java.io.IOException;
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

    private InstaConstants instaConstants = new InstaConstants();

    @GetMapping("stats")
    public String listProjects(Model model, @RequestParam(name = "id") Long projectId, HttpServletRequest request) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String name = auth.getName(); //get logged in username
        Project project = projectRepository.znajdz(projectId);
        model.addAttribute("project", project);
        request.getSession(false).setAttribute("project", project);
        List<Object[]> ret = em.createNativeQuery("select * from (select s.time, count(m.mediaid), s.id, s.kind from instabot.statistics s " +
                "   left outer join instabot.media m on s.id = m.stat_id where s.projectid = :project" +
                "   group by s.time, s.id order by s.time) t ").setParameter("project", projectId).getResultList();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("chartLikeData", ret.stream().filter(a -> (a[3]).equals('L')).map(a -> a[1]).collect(Collectors.toList()));
        model.addAttribute("chartLikeLabels", ret.stream().filter(a -> (a[3]).equals('L')).map(a -> sdf.format(a[0]) + "###" + a[2]).collect(Collectors.toList()));

        model.addAttribute("chartCommentData", ret.stream().filter(a -> (a[3]).equals('C')).map(a -> a[1]).collect(Collectors.toList()));
        model.addAttribute("chartCommentLabels", ret.stream().filter(a -> (a[3]).equals('C')).map(a -> sdf.format(a[0]) + "###" + a[2]).collect(Collectors.toList()));

        return "stats/stats";
    }

    @GetMapping("activity")
    public String getActivity(Model model, @RequestParam("id") Long id, HttpServletRequest request) {
        List<Media> medias = em.createNativeQuery("select m.* from instabot.media m where m.stat_id = :id", Media.class)
                .setParameter("id", id)
                .getResultList();
        Set<Thread> threads = new HashSet<>(medias.size());
        for (Media media : medias) {
            Thread thread = new Thread(() -> {
                try {
                    me.postaddict.instagram.scraper.domain.Media data = instaConstants.getInstagramAnonymous().getMediaByCode(
                            me.postaddict.instagram.scraper.domain.Media.getCodeFromId(media.getMediaId()));
                    System.out.println(" data.likesCount " + data.likesCount);
                    System.out.println(" data.commentsCount " + data.commentsCount);
                    media.setUserLikes(data.likesCount);

                    media.setUserName(data.owner.username);

                    media.setUserProfileImage(data.owner.profilePicUrl);
                    media.setCommentsCount(data.commentsCount);
                    media.setUserFollowed(instaConstants.getInstagramAnonymous().getAccountByUsername(data.owner.username).followsCount);

                } catch (InstagramException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
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
        List<Object[]> ret = em.createNativeQuery("select distinct x.tags, count(DISTINCT x.mediaid) mcnt, " +
                "  (select count(DISTINCT m.mediaid) from instabot.media m JOIN instabot.statistics s ON m.stat_id = s.id where projectid = :projectid) acnt " +
                "from (select unnest(string_to_array(m.tags, ',')) tags, m.mediaid, m.id from instabot.media m) x " +
                "group by x.tags " +
                "order by mcnt desc").setParameter("projectid", project.getId()).getResultList();

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

        try {
            List<String> items = null;
            if (type.equals("like")) {

//                items = instaConstants.getInstagramAnonymous().getMediaByCode(me.postaddict.instagram.scraper.domain.Media.getCodeFromId(id)).

            } else if (type.equals("comment")) {

                items = instaConstants.getInstagramAnonymous().getCommentsByMediaCode(me.postaddict.instagram.scraper.domain.Media.getCodeFromId(id), 999).stream().map(c -> c.user.username + " - " + c.text).collect(Collectors.toList());
            }
            model.addAttribute("items", items);
        } catch (InstagramException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "fragments/components :: modal-content";
    }
}

package pl.szewczyk.stats;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.domain.Account;
import me.postaddict.instagram.scraper.domain.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.projects.Project;
import pl.szewczyk.projects.ProjectRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by przem on 11.09.2017.
 */
@Controller
public class StatsController {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @ModelAttribute("module")
    String module() {
        return "stats";
    }

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;

    private InstaConstants instaConstants = new InstaConstants();
    private Instagram loggedInInstagram;

    @GetMapping("stats")
    public String listProjects(Model model, @RequestParam(name = "id") Long projectId, @RequestParam(name = "hours", defaultValue = "24", required = false) String hours, HttpServletRequest request) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String name = auth.getName(); //get logged in username
        Project project = projectRepository.znajdz(projectId);
        System.out.println("HOURS " + hours + project.getId());
        model.addAttribute("project", project);
        request.getSession(false).setAttribute("project", project);
        List<Object[]> ret = em.createNativeQuery("select * from (select date_trunc('hour', s.time), count(m.mediaid), s.kind from instabot.statistics s" +
                "                   left outer join instabot.media m on s.id = m.stat_id where s.projectid = :project" +
                "                   and s.time >= current_timestamp - interval '" + hours + " hours' " +
                "                   group by date_trunc('hour', s.time), s.kind order by 1) t ").setParameter("project", projectId)
                //.setParameter("hours", hours)
                .getResultList();
        log.info("COMMENTS ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");


        model.addAttribute("chartData", ret.stream().map(a -> a[1]).collect(Collectors.toList()));
        model.addAttribute("chartLabels", ret.stream().map(a -> sdf.format(a[0])).collect(Collectors.toList()));


        return "stats/stats";
    }

    @GetMapping("activity")
    public String getActivity(Model model, @RequestParam("id") String id, @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset, HttpServletRequest request) {
        log.info("pobierz dane " + id + " z offsetem " + offset);

        Project project = (Project) request.getSession(false).getAttribute("project");
        String sqlQuery = "select distinct link, mediaid, thumbnailuri, " +
                "tags, createddate, max(action_time), bool_or(liked) liked, bool_or(commented) commented, " +
                " userlikes, commentscount, userfollowed, username, locationname, userprofileimage " +
                "from instabot.media m join instabot.statistics s on m.stat_id = s.id " +
                "where to_char(s.time, 'yyyy-mm-dd hh24') = :id " +
                "group BY link, mediaid, thumbnailuri, tags, createddate, " +
                "userlikes, commentscount, userfollowed, username, locationname, userprofileimage " +
                "order by createddate desc " +
                (project.isOnlineStats() ? "limit 20 " +
                        "offset :offset" : "");
        Query query = em.createNativeQuery(sqlQuery);
        query.setParameter("id", id);
        if (project.isOnlineStats())
            query.setParameter("offset", offset);

        List<Object[]> objects = query.getResultList();
        log.info("project.isOnlineStats() " + project.isOnlineStats());
        List<Media> medias = new ArrayList<>();
        Set<Thread> threads = new HashSet<>(medias.size());
        for (Object[] o : objects) {
            Thread thread = new Thread(() -> {
                try {
                    Media media = new Media();
                    media.setLink((String) o[0]);
                    media.setMediaId((String) o[1]);
                    media.setThumbnailUri((String) o[2]);
                    media.setTags((String) o[3]);
                    media.setCreatedDate((Date) o[4]);
                    media.setActionTime((Date) o[5]);
                    if (project.isOnlineStats()) {

                        me.postaddict.instagram.scraper.domain.Media data = instaConstants.getInstagramAnonymous().getMediaByCode(
                                me.postaddict.instagram.scraper.domain.Media.getCodeFromId(media.getMediaId()));

                        try {
                            List<Account> list = instaConstants.getInstagramAnonymous().getUserLikesByMediaCode(data.shortcode);
                            media.setLiked(list.stream().filter(account -> account.username.equals(project.getInstagramAccount())).count() > 0);
                            media.setUserLikes(data.likesCount);
                        } catch (Exception e) {
                        }
                        try {
                            List<Comment> comments = instaConstants.getInstagramAnonymous().getCommentsByMediaCode(data.shortcode, 999);
                            media.setCommented(comments.stream().filter(comment -> comment.user.username.equals(project.getInstagramAccount())).count() > 0);
                            media.setCommentsCount(data.commentsCount);
                        } catch (Exception e) {
                        }
                        Account account = instaConstants.getInstagramAnonymous().getAccountByUsername(data.owner.username);
                        media.setUserFollowed(account.followsCount);
                        media.setUserName(account.username);

                        media.setLocationName(data.locationName);

                        media.setUserProfileImage(account.profilePicUrl);
                    } else {
                        media.setLiked((Boolean) o[6]);
                        media.setCommented((Boolean) o[7]);
                        media.setUserLikes((Integer) o[8]);
                        media.setCommentsCount((Integer) o[9]);
                        media.setUserFollowed((Integer) o[10]);
                    }



                    medias.add(media);
                } catch (IOException e) {
                    log.severe(e.getMessage());
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
        medias.sort(Comparator.comparing(Media::getCreatedDate));
        model.addAttribute("project", project);
        model.addAttribute("mediaList", medias);
        log.info("EEEME " + medias.size());
        return "fragments/components :: activityStats";
    }

    @GetMapping("activity/tags")
    public String getTags(Model model, HttpServletRequest request) {
        Project project = (Project) request.getSession(false).getAttribute("project");
        List<Object[]> ret = em.createNativeQuery("select distinct UPPER (x.tags), count(DISTINCT x.mediaid) mcnt, " +
                "  (select count(DISTINCT m.mediaid) from instabot.media m JOIN instabot.statistics s ON m.stat_id = s.id where projectid = :projectid) acnt " +
                "from (select unnest(string_to_array(m.tags, ',')) tags, m.mediaid, m.id from instabot.media m) x " +
                "group by UPPER(x.tags) " +
                "HAVING count(DISTINCT x.mediaid) > ((select count(DISTINCT m.mediaid) from instabot.media m JOIN instabot.statistics s ON m.stat_id = s.id where projectid = :projectid)/100) " +
                "order by mcnt desc").setParameter("projectid", project.getId()).getResultList();

        List<Stat> stats = new ArrayList<>();
        for (Object[] o : ret) {
            Stat s = new Stat();
            s.setTag(((String) o[0]).toLowerCase());
            s.setHits(((BigInteger) o[1]).doubleValue());
            s.setTotal(((BigInteger) o[2]).doubleValue());
            stats.add(s);
        }

        model.addAttribute("mediatags", stats);

        return "fragments/components :: hashtags";
    }

    @GetMapping("/stats/media")
    public String getModalContent(Model model, @RequestParam("type") String type, @RequestParam("id") String id, HttpServletRequest request) {
        log.info("asadasdasd " + type + "  " + id);

        try {
            log.info("BBB " + id);
            String code = me.postaddict.instagram.scraper.domain.Media.getCodeFromId(id);
            log.info("BBB " + code);
            if (type.equals("like")) {
                List<Account> list = instaConstants.getInstagramAnonymous().getUserLikesByMediaCode(code);
                model.addAttribute("items", list);

                return "fragments/components :: modal-content-like";
            } else {
                System.out.println(code);
                List<Comment> comments = instaConstants.getInstagramAnonymous().getCommentsByMediaCode(code, 999);
                for (Comment c : comments)
                    log.info(c.text);
                model.addAttribute("items", comments);
                return "fragments/components :: modal-content-comment";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Nie udało się pobrać... Poczekaj chwilę i spróbuj ponownie!");
            if (type.equals("like")) {
                return "fragments/components :: modal-content-like";
            } else {
                return "fragments/components :: modal-content-comment";
            }
        }

    }

    @PostMapping("/likeMedia")
    public String likeMediaByCode(@RequestParam("project") Long projectId, @RequestParam("code") String code) {
        Project project = projectRepository.znajdz(projectId);
        Instagram instagram = instaConstants.getInstagramLoggedIn(project.getInstagramAccount());
        try {
            instagram.likeMediaByCode(code);
            return "Liked!";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Not liked :/";
    }

    @PostMapping("/commentMedia")
    public String commentMediaByCode(@RequestParam("project") Long projectId, @RequestParam("code") String code) {
        Project project = projectRepository.znajdz(projectId);
        Instagram instagram = instaConstants.getInstagramLoggedIn(project.getInstagramAccount());
        try {
            instagram.addMediaComment(code, project.getRandomComment());
            return "Commented!";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Not commented :/";
    }
}

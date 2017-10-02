package pl.szewczyk.projects;

import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.stats.Statistic;
import pl.szewczyk.stats.StatisticsRepository;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by przem on 20.09.2017.
 */

public abstract class ProjectJob implements Job {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private StatisticsRepository statisticsRepository;

    private Project project;

    protected void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Long id = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");
        Character kind = jobExecutionContext.getJobDetail().getJobDataMap().getChar("kind");
        System.out.println("Project ID " + id);

        if (project == null) {
            init();
        }
        project = projectRepository.znajdz(id);

        InstaUser instaUser = em.createQuery("from InstaUser where instaUserName = :id", InstaUser.class).setParameter("id", project.getInstagramAccount()).getSingleResult();

        Map<String, List<MediaFeedData>> tags = new HashMap<>();

        for (String tag : project.getIncludeHashtags()) {
            try {
                List<MediaFeedData> list = searchTag(tag, instaUser.getAccessToken());
                if (list != null)
                    tags.put(tag, list);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(tag + " EXCEPTION " + e.getMessage());

            }
        }
        try {
            if (HashtagSearchEnum.ANY.equals(project.getHashtagSearch())) {
                System.out.println("ANY ");
                List<MediaFeedData> searches = tags.entrySet().stream()
                        .flatMap(s -> s.getValue().stream())
                        .distinct()
                        .collect(Collectors.toList());
                Iterator<MediaFeedData> it = searches.iterator();
                while (it.hasNext()) {
                    MediaFeedData mediaFeedData = it.next();
                    System.out.println("TAGS = " + mediaFeedData.getTags());
                    System.out.println("EXCLUDE " + project.getExcludeHashtags());
                    if (Collections.disjoint(mediaFeedData.getTags(), project.getExcludeHashtags())) {
                        try {
                            System.out.println("CHECKING    === " + projectRepository.countMediaId(mediaFeedData.getId(), kind, project));
                            if (projectRepository.countMediaId(mediaFeedData.getId(), kind, project) == 0L) {
                                System.out.println("ODPALAM ROBOTE");
                                doJob(mediaFeedData, project.getCommentString(), instaUser.getAccessToken());
                            } else {
                                System.out.println("JUZ TO ROBILEM...");
                                it.remove();
                            }
                        } catch (InstagramException e) {
                            e.printStackTrace();
                        }
                    } else {
                        it.remove();
                    }
                }
                System.out.println("SAVE STATS " + jobExecutionContext.getJobDetail().getJobDataMap().getChar("kind") + " WITH SEARCHES " + searches.size());
                try {
                    Statistic statistic = new Statistic(project, new HashSet<>(searches));
                    statistic.setKind(kind);
                    statisticsRepository.save(statistic);
                } catch (Exception e) {
                    System.out.println("EXCEPTION " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println("SAVED");
            }


            if (HashtagSearchEnum.ALL.equals(project.getHashtagSearch())) {
                System.out.println("ALL ");
                List<MediaFeedData> searches = tags.entrySet().stream()
                        .flatMap(s -> s.getValue().stream())
                        .distinct()
                        .collect(Collectors.toList());
                Iterator<MediaFeedData> it = searches.iterator();
                while (it.hasNext()) {
                    MediaFeedData mediaFeedData = it.next();
                    System.out.println("TAGS = " + mediaFeedData.getTags());
                    System.out.println("EXCLUDE " + project.getExcludeHashtags());
                    if (Collections.disjoint(mediaFeedData.getTags(), project.getExcludeHashtags()) &&
                            mediaFeedData.getTags().containsAll(project.getIncludeHashtags())) {
                        try {
                            System.out.println("CHECKING    === " + projectRepository.countMediaId(mediaFeedData.getId(), kind, project));
                            if (projectRepository.countMediaId(mediaFeedData.getId(), kind, project) == 0L) {
                                System.out.println("ODPALAM ROBOTE");
                                doJob(mediaFeedData, project.getCommentString(), instaUser.getAccessToken());
                            } else {
                                System.out.println("JUZ TO ROBILEM...");
                                it.remove();
                            }
                        } catch (InstagramException e) {
                            e.printStackTrace();
                        }
                    } else {
                        it.remove();
                    }
                }
                System.out.println("SAVE STATS " + jobExecutionContext.getJobDetail().getJobDataMap().getChar("kind") + " WITH SEARCHES " + searches.size());
                try {
                    Statistic statistic = new Statistic(project, new HashSet<>(searches));
                    statistic.setKind(kind);
                    statisticsRepository.save(statistic);
                } catch (Exception e) {
                    System.out.println("EXCEPTION " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println("SAVED");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public List<MediaFeedData> searchTag(String tag, String authToken)  {
        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
        Instagram instagram = new Instagram(accessToken);

        System.out.println("TAG SEARCH " + tag);
        MediaFeed mediaFeed = null;
        try {
            mediaFeed = instagram.getRecentMediaFeedTags(tag);
        } catch (InstagramException e) {
            System.err.println("ERRROR SEARCHING TAG " + tag + "   " + project.getName());
            System.err.println(e.getMessage());
        }

        if (mediaFeed != null)
            return mediaFeed.getData();

        return null;
    }


    abstract void doJob(MediaFeedData mediaFeedData, String comment, String authToken) throws InstagramException;

}

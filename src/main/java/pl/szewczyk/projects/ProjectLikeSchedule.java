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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by przem on 20.09.2017.
 */

public class ProjectLikeSchedule implements Job {
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
        System.out.println("AAAAAAAAAAAAAAAA");

        Long id = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");
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
            if (project.getHashtagSearch().equals(HashtagSearchEnum.ANY)) {
                System.out.println("ANY ");
                List<MediaFeedData> searches = tags.entrySet().stream()
                        .flatMap(s -> s.getValue().stream())
                        .distinct()
                        .collect(Collectors.toList());

                for (MediaFeedData mediaFeedData : searches) {

                    if (project.isLike()) {
                        try {
                            like(mediaFeedData, instaUser.getAccessToken());
                        } catch (InstagramException ie) {


                        }
                    }

                    if (project.isComment()) {
                        try {
                            comment(mediaFeedData, project.getCommentString(), instaUser.getAccessToken());
                        } catch (InstagramException ie) {

                        }
                    }
                }

                Statistic statistic = new Statistic(project, new HashSet<>(searches));
                statisticsRepository.save(statistic);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public List<MediaFeedData> searchTag(String tag, String authToken) throws InstagramException {
        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
        Instagram instagram = new Instagram(accessToken);

        MediaFeed mediaFeed = instagram.getRecentMediaFeedTags(tag);

        if (mediaFeed != null)
            return mediaFeed.getData();

        return null;
    }

    public void like(MediaFeedData mediaFeedData, String authToken) throws InstagramException {
        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
        Instagram instagram = new Instagram(accessToken);

        instagram.setUserLike(mediaFeedData.getId());
    }

    public void comment(MediaFeedData mediaFeedData, String comment, String authToken) throws InstagramException {
        Token accessToken = new Token(authToken, InstaConstants.ClientSecret);
        Instagram instagram = new Instagram(accessToken);
        instagram.setMediaComments(mediaFeedData.getId(), comment);
    }
}

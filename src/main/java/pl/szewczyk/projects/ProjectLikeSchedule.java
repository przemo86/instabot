package pl.szewczyk.projects;

import org.jinstagram.entity.users.feed.MediaFeedData;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.instagram.InstagramUtils;

import javax.persistence.EntityManager;
import java.util.HashMap;
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
    private InstagramUtils instagramUtils;

    private Project project;


    protected void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        System.out.println("AAAAAAAAAAAAAAAA");

        Long id = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");

        if (project == null) {
            init();
            project = projectRepository.znajdz(id);
        }

        InstaUser instaUser = em.createQuery("from InstaUser where instaUserName = :id", InstaUser.class).setParameter("id", project.getInstagramAccount()).getSingleResult();

        Map<String, List<MediaFeedData>> tags = new HashMap<>();

        for (String tag : project.getIncludeHashtags()) {
            try {

                tags.put(tag, instagramUtils.searchTag(tag, instaUser.getAccessToken()));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(tag + " EXCEPTION " + e.getMessage());

            }
        }


        if (project.getHashtagSearch().equals(HashtagSearchEnum.ANY)) {
            List<MediaFeedData> searches = tags.entrySet().stream()
                    .flatMap(s -> {
                        return s.getValue().stream();
                    })
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("SIEZ " + searches.size());
        }
    }

}

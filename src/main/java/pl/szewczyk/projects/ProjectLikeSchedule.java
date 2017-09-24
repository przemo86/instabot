package pl.szewczyk.projects;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.instagram.InstagramUtils;

import javax.persistence.EntityManager;
import java.util.logging.Logger;

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
        System.out.println("AAAAAAAAAAAAAAAA");

        Long id = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");

        if (project == null) {
            init();
            project = projectRepository.znajdz(id);
        }
        System.out.println("" + project.getName());
        System.out.println("" + project.getHashtagSearch());

        if (HashtagSearchEnum.ALL.equals(project.getHashtagSearch())) {
            Logger.getGlobal().severe("SEARCH ALL " + project.getIncludeHashtags().toString());
        } else if (HashtagSearchEnum.ANY.equals(project.getHashtagSearch())) {
            Logger.getGlobal().severe("SEARCH ANY " + project.getIncludeHashtags().toString());
        } else {
            Logger.getGlobal().severe("NONE " + project.getIncludeHashtags().toString());
        }

        Logger.getGlobal().severe("EXCLUDE " + project.getExcludeHashtags().toString());
        Logger.getGlobal().severe("EM " + project.getInstagramAccount());


        InstaUser instaUser = em.createQuery("from InstaUser where instaUserName = :id", InstaUser.class).setParameter("id", project.getInstagramAccount()).getSingleResult();
        Logger.getGlobal().severe("EM " + instaUser);
        Logger.getGlobal().severe("" + instaUser.equals(null));

        for (String tag : project.getIncludeHashtags()) {
            try {
                Logger.getGlobal().severe("TAG " + tag);
                Logger.getGlobal().severe(instagramUtils.searchTag(tag, instaUser.getAccessToken()));
            } catch (Exception e) {
                Logger.getGlobal().severe("ERROR QUERY " + e.getMessage());

            }
        }
    }

}

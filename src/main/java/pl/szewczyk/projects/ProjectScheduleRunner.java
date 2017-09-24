package pl.szewczyk.projects;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by przem on 20.09.2017.
 */
@Service
public class ProjectScheduleRunner {

    @Autowired
    private ProjectService projectService;

    private Scheduler scheduler;

    public void reset() throws SchedulerException {
        System.out.println("RESET");
        scheduler.shutdown();

        init();
        System.out.println("complete");
    }

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();

        for (Project project : projectService.listActiveProjects()) {
            try {
                addJob(project);
            } catch (InvocationTargetException | IllegalAccessException e) {
                Logger.getGlobal().severe("Error " + e.getMessage());
            }
        }
    }

    public void addJob(Project project) throws SchedulerException, InvocationTargetException, IllegalAccessException {

        Logger.getGlobal().severe("INIT RUNNER :/");

JobDataMap dataMap = new JobDataMap();
dataMap.put("projectId", project.getId());
        if (project.isComment()) {
            Logger.getGlobal().severe(String.valueOf(dataMap.getLong("projectId")));
            JobDetail job = newJob(ProjectCommentSchedule.class)
                    .usingJobData(dataMap)
                    .withIdentity(project.getName() + "_job", "comments")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(project.getName() + "_trigger", "comments")
                    .startNow()
                    .withSchedule(

                            simpleSchedule()
                                    .withIntervalInSeconds(3600 / project.getCommentFrequency().getFrequency())
                                    .repeatForever())
                    .build();


            scheduler.scheduleJob(job, trigger);
        }
        if (project.isLike()) {
            JobDetail job = newJob(ProjectLikeSchedule.class)
                    .usingJobData(dataMap)
                    .withIdentity(project.getName() + "_job", "likes")
                    .build();


            Trigger trigger = newTrigger()
                    .withIdentity(project.getName() + "_trigger", "likes")
                    .startNow()
                    .withSchedule(

                            simpleSchedule()
                                    .withIntervalInSeconds(3600 / project.getLikeFrequency().getFrequency())
                                    .repeatForever())
                    .build();


            scheduler.scheduleJob(job, trigger);
        }
        Logger.getGlobal().severe("Started ? :/");
    }


}

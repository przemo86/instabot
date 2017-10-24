package pl.szewczyk.projects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
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

    private Set<TriggerKey> currentJobs = new HashSet<>();


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

    @PreDestroy
    public void shutdown() {
        try {
            System.out.println("SHUTDOWN");
            scheduler.shutdown(false);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void interruptJob(Project project) throws SchedulerException {
        if (currentJobs.contains(TriggerKey.triggerKey(project.getName()))) {
            scheduler.unscheduleJob(TriggerKey.triggerKey(project.getName()));
            scheduler.deleteJob(JobKey.jobKey(project.getName()));
            currentJobs.remove(TriggerKey.triggerKey(project.getName()));
        }
        if (currentJobs.contains(TriggerKey.triggerKey(project.getName()))) {
            scheduler.unscheduleJob(TriggerKey.triggerKey(project.getName()));
            scheduler.deleteJob(JobKey.jobKey(project.getName()));
            currentJobs.remove(TriggerKey.triggerKey(project.getName()));
        }


    }

    private void addJob(Project project) throws SchedulerException, InvocationTargetException, IllegalAccessException {

        Logger.getGlobal().severe("Add jobs for '" + project.getName() + "'");

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("projectId", project.getId());
        if (project.isComment() && project.getCommentFrequency() != null && !currentJobs.contains(TriggerKey.triggerKey(project.getName()))) {
            dataMap.put("comment", project.getCommentFrequency().getFrequency());
        }
        if (project.isLike() && project.getLikeFrequency() != null && !currentJobs.contains(TriggerKey.triggerKey(project.getName()))) {
            dataMap.put("like", project.getLikeFrequency().getFrequency());
        }
        JobDetail job = newJob(ProjectJob.class)
                .usingJobData(dataMap)
                .withIdentity(project.getName())
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(project.getName())
                .startNow()
                .withSchedule(
                        simpleSchedule()
                                .withIntervalInSeconds(60)
                                .repeatForever())
                .build();
        scheduler.scheduleJob(job, trigger);
        currentJobs.add(trigger.getKey());


//    JobDetail job = newJob(ProjectLikeJob.class)
//            .usingJobData(dataMap)
//            .withIdentity(project.getName())
//            .build();
//
//
//    Trigger trigger = newTrigger()
//            .withIdentity(project.getName())
//            .startNow()
//            .withSchedule(
//
//                    simpleSchedule()
//                            .withIntervalInSeconds(3600 / project.getLikeFrequency().getFrequency())
//                            .repeatForever())
//            .build();
//
//
//            scheduler.scheduleJob(job,trigger);
//            currentJobs.add(trigger.getKey());
//}
        Logger.getGlobal().severe("Started for project '" + project.getName() + "'");
    }


    public void startProject(Project project) {
        System.out.println("START PROJECT");
        try {
            addJob(project);
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Pair<JobDetail, Trigger> getJobLike(Project project) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(project.getName()));
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(project.getName()));

        return new ImmutablePair<>(jobDetail, trigger);
    }

    public Pair<JobDetail, Trigger> getJobComment(Project project) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(project.getName()));
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(project.getName()));

        return new ImmutablePair<>(jobDetail, trigger);
    }
}

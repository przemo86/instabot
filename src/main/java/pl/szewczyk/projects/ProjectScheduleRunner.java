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

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private ProjectService projectService;

    private Scheduler scheduler;

    private Set<TriggerKey> currentJobs = new HashSet<>();

//    private static final String COMMENT_APPEND = "_comment";
//    private static final String LIKE_APPEND = "_like";

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();

        for (Project project : projectService.listActiveProjects()) {
            try {
                addJob(project);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.severe("Error " + e.getMessage());
            }
        }

    }

    @PreDestroy
    public void shutdown() {
        try {
            log.info("SHUTDOWN");
            scheduler.shutdown(false);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void interruptJob(Project project) throws SchedulerException {

        scheduler.getCurrentlyExecutingJobs().stream().filter(j -> {
            try {
                return j.getJobDetail().equals(getJob(project).getLeft());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            return false;
        }).forEach(j -> {
            try {
                ((InterruptableJob) j.getJobInstance()).interrupt();
            } catch (UnableToInterruptJobException e) {
                e.printStackTrace();
            }
        });
        if (currentJobs.contains(TriggerKey.triggerKey("_" + project.getId()))) {
            scheduler.unscheduleJob(TriggerKey.triggerKey("_" + project.getId()));
            scheduler.deleteJob(JobKey.jobKey("_" + project.getId()));
            currentJobs.remove(TriggerKey.triggerKey("_" + project.getId()));
        }
//        if (currentJobs.contains(TriggerKey.triggerKey(project.getId() + COMMENT_APPEND))) {
//            scheduler.unscheduleJob(TriggerKey.triggerKey(project.getId() + COMMENT_APPEND));
//            scheduler.deleteJob(JobKey.jobKey(project.getId() + COMMENT_APPEND));
//            currentJobs.remove(TriggerKey.triggerKey(project.getId() + COMMENT_APPEND));
//        }


    }

    private void addJob(Project project) throws SchedulerException, InvocationTargetException, IllegalAccessException {

        log.info("Add jobs for '" + project.getName() + "'");

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("projectId", project.getId());
//        if (project.isComment() && project.getCommentFrequency() != null && !currentJobs.contains(TriggerKey.triggerKey(project.getId() + COMMENT_APPEND))) {
//            JobDetail job = newJob(ProjectCommentJob.class)
//                    .usingJobData(dataMap)
//                    .withIdentity(project.getId() + COMMENT_APPEND)
//                    .build();
//
//            Trigger trigger = newTrigger()
//                    .withIdentity(project.getId() + COMMENT_APPEND)
//                    .startNow()
//                    .withSchedule(
//                            simpleSchedule()
//                                    .withIntervalInSeconds(3600 / project.getCommentFrequency().getFrequency())
//                                    .repeatForever())
//                    .build();
//            scheduler.scheduleJob(job, trigger);
//            currentJobs.add(trigger.getKey());
//        }
        System.out.println(project.getLikeFrequency());
        System.out.println(currentJobs.contains(TriggerKey.triggerKey("_" + project.getId())));
        if (project.getLikeFrequency() != null && !currentJobs.contains(TriggerKey.triggerKey("_" + project.getId()))) {
            JobDetail job = newJob(ProjectJob.class)
                    .usingJobData(dataMap)
                    .withIdentity("_" + project.getId())
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("_" + project.getId())
                    .startNow()
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(3600 / project.getLikeFrequency().getFrequency())
                                    .repeatForever())
                    .build();


            scheduler.scheduleJob(job, trigger);
            currentJobs.add(trigger.getKey());
            log.info("Started for project '" + project.getName() + "'");
        }
    }

    public void startProject(Project project) {
        log.info("START PROJECT");
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

    public Pair<JobDetail, Trigger> getJob(Project project) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey("_" + project.getId()));
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey("_" + project.getId()));

        return new ImmutablePair<>(jobDetail, trigger);
    }

//    public Pair<JobDetail, Trigger> getJobComment(Project project) throws SchedulerException {
//        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(project.getId() + COMMENT_APPEND));
//        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(project.getId() + COMMENT_APPEND));
//
//        return new ImmutablePair<>(jobDetail, trigger);
//    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}

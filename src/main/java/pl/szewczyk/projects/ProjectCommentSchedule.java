package pl.szewczyk.projects;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by przem on 20.09.2017.
 */
@Component
public class ProjectCommentSchedule implements Job{


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Project project = (Project) jobExecutionContext.get("project");
        Logger.getGlobal().severe("COMMENT");
        Logger.getGlobal().severe(project.getName());
        Logger.getGlobal().severe("COMMENTAZIONE");

    }
}

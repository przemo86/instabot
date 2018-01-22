package pl.szewczyk.home;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.szewczyk.account.Account;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.projects.Project;
import pl.szewczyk.projects.ProjectRepository;
import pl.szewczyk.projects.ProjectScheduleRunner;
import pl.szewczyk.projects.ProjectService;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Controller
class DashboardController {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @ModelAttribute("module")
    String module() {
        return "home";
    }

    @Autowired
    private ProjectScheduleRunner projectScheduleRunner;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;


    @GetMapping("/")
    String index(Principal principal, Model model) {
        List<DashboardPOJO> dashboardPOJOS = new ArrayList<>();
        Account account = em.createQuery("select a from Account a where a.email = :email", Account.class)
                .setParameter("email", principal.getName()).getSingleResult();
        List<Project> projects = projectService.listUserProjects(account);
        for (Project project : projects) {
            try {
                DashboardPOJO pojo = new DashboardPOJO();
                pojo.setProjectId(project.getId());
                pojo.setProjectName(project.getName());
                pojo.setCustomerName(project.getCustomer());
                Pair<JobDetail, Trigger> pair = projectScheduleRunner.getJob(project);
                if (null != pair) {
                    if ((pair.getLeft() != null) && (pair.getRight() != null)) {
                        pojo.setRunningTime(new Date(System.currentTimeMillis() - pair.getRight().getStartTime().getTime()));
                        pojo.setNextFire(pair.getRight().getNextFireTime());
                        pojo.setHits(projectRepository.count(project));
                    }
                }

//                pair = projectScheduleRunner.getJobComment(project);
//
//                if (null != pair) {
//                    if ((pair.getLeft() != null) && (pair.getRight() != null)) {
//                        pojo.setCommentRunningTime(new Date(System.currentTimeMillis() - pair.getRight().getStartTime().getTime()));
//                        pojo.setCommentNextFire(pair.getRight().getNextFireTime());
//                        pojo.setCommentHits(projectRepository.count('C', project));
//                    }
//                }
                if ((pojo.getRunningTime() != null) || (pojo.getRunningTime() != null))
                    dashboardPOJOS.add(pojo);
            } catch (Exception e) {
                log.severe(e.getMessage());
                e.printStackTrace();
            }
        }
        model.addAttribute("jobs", dashboardPOJOS);
        return principal != null ? "home/homeSignedIn" : "home/homeNotSignedIn";
    }


    @PostMapping("/kill")
    public String killProject(@RequestParam Long id, Principal principal) {
        if (principal != null) {


            Project project = projectRepository.znajdz(id);
            try {
                projectScheduleRunner.interruptJob(project);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            project.setStatus(false);
            projectRepository.save(project);

        }
        return principal != null ? "redirect:/" : "redirect:homeNotSignedIn";
    }


    @GetMapping("/login")
    public String login(@RequestParam Long id) throws Exception {
        InstaConstants instaConstants = new InstaConstants();
        Project project = projectRepository.znajdz(id);
        InstaUser user = em.createQuery("from InstaUser where instaUserName = :id", InstaUser.class).setParameter("id", project.getInstagramAccount()).getSingleResult();
        instaConstants.loginInstagram(user.getInstaUserName(), instaConstants.getUserPass(user.getInstaUserName()));

        return "redirect:/";
    }
//
//    @PostMapping("/like")
//    public String like(@RequestParam Long id, Principal principal) throws SchedulerException {
//        Project project = projectRepository.znajdz(id);
//        JobDetail jobDetail = projectScheduleRunner.getJobLike(project).getLeft();
//
//        projectScheduleRunner.getScheduler().triggerJob(jobDetail.getKey());
//
//        return principal != null ? "redirect:/" : "redirect:homeNotSignedIn";
//    }
//
//    @PostMapping("/comment")
//    public String comment(@RequestParam Long id, Principal principal) throws SchedulerException {
//        Project project = projectRepository.znajdz(id);
//        JobDetail jobDetail = projectScheduleRunner.getJobComment(project).getLeft();
//
//        projectScheduleRunner.getScheduler().triggerJob(jobDetail.getKey());
//        return principal != null ? "redirect:/" : "redirect:homeNotSignedIn";
//    }

    @PostMapping("/firejob")
    public String comment(@RequestParam Long id, Principal principal) throws SchedulerException {
        Project project = projectRepository.znajdz(id);
        JobDetail jobDetail = projectScheduleRunner.getJob(project).getLeft();

        projectScheduleRunner.getScheduler().triggerJob(jobDetail.getKey());
        return principal != null ? "redirect:/" : "redirect:homeNotSignedIn";
    }
}

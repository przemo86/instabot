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
import pl.szewczyk.projects.Project;
import pl.szewczyk.projects.ProjectRepository;
import pl.szewczyk.projects.ProjectScheduleRunner;
import pl.szewczyk.projects.ProjectService;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
class DashboardController {

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
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<DashboardPOJO> dashboardPOJOS = new ArrayList<>();
        Account account = em.createQuery("select a from Account a where a.email = :email", Account.class)
                .setParameter("email", principal.getName()).getSingleResult();
        List<Project> projects = projectService.listUserProjects(account);
        System.out.println("PROJ SIZE " + projects.size());
        for (Project project : projects) {
            try {
                DashboardPOJO pojo = new DashboardPOJO();
                pojo.setProjectId(project.getId());
                pojo.setProjectName(project.getName());
                pojo.setCustomerName(project.getCustomer());
                System.out.println("1");
                Pair<JobDetail, Trigger> pair = projectScheduleRunner.getJobLike(project);
                if (null != pair) {
                    if ((pair.getLeft() != null) && (pair.getRight() != null)) {
                        pojo.setLikeRunningTime(new Date(System.currentTimeMillis() - pair.getRight().getStartTime().getTime()));
                        pojo.setLikeNextFire(pair.getRight().getNextFireTime());
                        pojo.setLikeHits(projectRepository.count('L', project));
                    }
                }


                System.out.println("2");

                pair = projectScheduleRunner.getJobComment(project);

                if (null != pair) {
                    if ((pair.getLeft() != null) && (pair.getRight() != null)) {
                        pojo.setCommentRunningTime(new Date(System.currentTimeMillis() - pair.getRight().getStartTime().getTime()));
                        pojo.setCommentNextFire(pair.getRight().getNextFireTime());
                        pojo.setCommentHits(projectRepository.count('C', project));
                    }
                }
                if ((pojo.getLikeRunningTime() != null) || (pojo.getCommentRunningTime() != null))
                    dashboardPOJOS.add(pojo);
            } catch (Exception e) {
                System.out.println(e.getMessage());
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


}

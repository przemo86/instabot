package pl.szewczyk.projects;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.szewczyk.instagram.InstaUser;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * Created by przem on 11.09.2017.
 */
@Controller
public class ProjectsController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectScheduleRunner projectScheduleRunner;

    @ModelAttribute("module")
    String module() {
        return "projects";
    }


    @GetMapping("projects")
    public String listProjects(Model model) {
        try {
            System.out.println("RESTARTOGN");
            projectScheduleRunner.reset();
        } catch (SchedulerException e) {
            System.out.println("ERROR RESTARTING " + e.getMessage());
        }

        model.addAttribute("projectList", projectRepository.findAll());

        return "home/projects";
    }

    @GetMapping(value = "project", params = {"id"})
    public String project(Model model, @RequestParam(value = "id") Long id, HttpServletRequest request) {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Project project = null;
        Logger.getGlobal().severe("aaaaaaaaa");
        project = em.createQuery("SELECT p FROM Project p left join fetch p.excludeHashtags left JOIN FETCH p.includeHashtags where p.id = :id", Project.class).setParameter("id", id).getSingleResult();
        Logger.getGlobal().severe("bbbbbbb");
        Logger.getGlobal().severe(project.getName());
        ProjectForm projectForm = new ProjectForm(project);
        request.getSession().setAttribute("project", project);

        model.addAttribute(projectForm);

        model.addAttribute("instagramAccounts", em.createQuery("select iu from Account a join a.instaUsers iu where a.email = :email", InstaUser.class)
                .setParameter("email", ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).getResultList());

        return "home/project";
    }

    @PostMapping(value = "project")
    public String project(@ModelAttribute ProjectForm projectForm, Errors errors, HttpServletRequest request) {

        Project project = (Project) request.getSession().getAttribute("project");
        Logger.getGlobal().severe("PPPPPPPPPPPPPPPPPPPP " + projectForm + "  " + project);
        project = projectForm.toEntity(project);
        Logger.getGlobal().severe("PPPPPPPPPPPPPPPPPPPP saved" + project.getId().toString());
        projectRepository.save(project);
        return "redirect:projects";
    }
}

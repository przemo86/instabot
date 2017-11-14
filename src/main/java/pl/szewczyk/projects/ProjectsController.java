package pl.szewczyk.projects;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
import javax.validation.Valid;

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
        model.addAttribute("projectList", projectRepository.findAll(new Sort(Sort.Direction.ASC, "name")));

        return "home/projects";
    }


    @GetMapping(value = "project")
    public String project(Model model, HttpServletRequest request) {
        ProjectForm projectForm = new ProjectForm();


        model.addAttribute(projectForm);
        model.addAttribute("enumValues", HashtagSearchEnum.values());

        model.addAttribute("instagramAccounts", em.createQuery("select iu from Account a join a.instaUsers iu where a.email = :email", InstaUser.class)
                .setParameter("email", ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).getResultList());

        return "home/project";
    }


    @GetMapping(value = "project", params = {"id"})
    public String project(Model model, @RequestParam(value = "id") Long id, HttpServletRequest request) {

        Project project = null;
        project = em.createQuery("SELECT p FROM Project p where p.id = :id", Project.class).setParameter("id", id).getSingleResult();
        System.out.println(project.getName());
        ProjectForm projectForm = new ProjectForm(project);
        request.getSession().setAttribute("project", project);

        model.addAttribute(projectForm);
        model.addAttribute("enumValues", HashtagSearchEnum.values());

        System.out.println(((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());


        model.addAttribute("instagramAccounts", em.createQuery("select iu from Account a join a.instaUsers iu where a.email = :email", InstaUser.class)
                .setParameter("email", ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).getResultList());

        return "home/project";
    }

    @PostMapping(value = "project")
    public String project(@Valid @ModelAttribute ProjectForm projectForm, Errors errors, HttpServletRequest request) {

        if (errors.hasErrors())
            return "home/project";

        Project project = (Project) request.getSession().getAttribute("project");
        project = projectForm.toEntity(project);
        projectRepository.save(project);

        try {
            projectScheduleRunner.interruptJob(project);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        System.out.println("SAVING PROJECT");

        if (project.isStatus()) {
            projectScheduleRunner.startProject(project);
        }

        return "redirect:projects";
    }
}

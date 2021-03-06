package pl.szewczyk.projects;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.szewczyk.account.AccountRepository;
import pl.szewczyk.instagram.InstaUser;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by przem on 11.09.2017.
 */
@Controller
public class ProjectsController {
    protected Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectScheduleRunner projectScheduleRunner;

    @Autowired
    private AccountRepository accountRepository;

    @ModelAttribute("module")
    String module() {
        return "projects";
    }


    @GetMapping("projects")
    public String listProjects(Model model) {
        model.addAttribute("projectList", projectRepository.findAll(new Sort(Sort.Direction.ASC, "name")).stream().filter(s -> !s.isDeleted()).collect(Collectors.toList()));

        return "home/projects";
    }


    @GetMapping(value = "project")
    public String project(Model model, HttpServletRequest request) {
        ProjectForm projectForm = new ProjectForm();

        request.getSession(false).removeAttribute("project");

        request.getSession().setAttribute("project", new Project());

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
        log.info(project.getName());
        ProjectForm projectForm = new ProjectForm(project);
        request.getSession().setAttribute("project", project);

        model.addAttribute(projectForm);
        model.addAttribute("enumValues", HashtagSearchEnum.values());

        log.info(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());


        model.addAttribute("instagramAccounts", em.createQuery("select iu from Account a join a.instaUsers iu where a.email = :email", InstaUser.class)
                .setParameter("email", ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).getResultList());

        return "home/project";
    }

    @GetMapping(value = "comments")
    public String getProjectComments(HttpServletRequest request, Model model) {
        System.out.println("aaaaaa");
        List<Comment> comments;
        Project project;
        if ((project = (Project) request.getSession(false).getAttribute("project")) != null) {
            comments = project.getComments();
        } else {
            comments = new ArrayList<>();
        }

        model.addAttribute("comments", comments);

        return "fragments/components :: project-comment-list";
    }

    @GetMapping(value = "blacklisted")
    public String getBlacklisted(HttpServletRequest request, Model model) {
        System.out.println("vlack");
        Set<BlacklistedUser> blacklistedUsers;
        Project project;
        if ((project = (Project) request.getSession(false).getAttribute("project")) != null) {
            blacklistedUsers = project.getBlacklistedUsers();
        } else {
            blacklistedUsers = new HashSet<>();
        }

        model.addAttribute("blacklistedUsers", blacklistedUsers);

        return "fragments/components :: blacklisted";
    }

    @PostMapping(value = "blacklisted")
    public String addBlacklisted(HttpServletRequest request, Model model, @RequestParam(name = "username") String username, @RequestParam(name = "picture") String picture) {
        System.out.println("vlack");
        Set<BlacklistedUser> blacklistedUsers;
        Project project;
        if ((project = (Project) request.getSession(false).getAttribute("project")) != null) {
            blacklistedUsers = project.getBlacklistedUsers();
        } else {
            blacklistedUsers = new HashSet<>();
        }

        BlacklistedUser b = new BlacklistedUser();
        b.setProfilePicUrl(picture);
        b.setUsername(username);

        if (project != null)
            project.getBlacklistedUsers().add(b);

        model.addAttribute("blacklistedUsers", blacklistedUsers);
        System.out.println("returning");
        return "fragments/components :: blacklisted";
    }

    @DeleteMapping(value = "blacklisted")
    public String deleteBlacklisted(HttpServletRequest request, Model model, @RequestParam(name = "username") String username) {
        System.out.println("delete vlack");
        Set<BlacklistedUser> blacklistedUsers = null;
        Project project;
        if ((project = (Project) request.getSession(false).getAttribute("project")) != null) {
            blacklistedUsers = project.getBlacklistedUsers();
        } else {
            blacklistedUsers = new HashSet<>();
        }


        blacklistedUsers.removeAll(blacklistedUsers.stream().filter(b -> b.getUsername().equals(username)).collect(Collectors.toList()));

        model.addAttribute("blacklistedUsers", blacklistedUsers);
        System.out.println("returning");
        return "fragments/components :: blacklisted";
    }

    @PostMapping(value = "addcomment")
    public String addComment(HttpServletRequest request, Model model, @RequestParam(value = "comment", required = false) String comment, @RequestParam(value = "priority", required = false) int priority) {
        System.out.println("ADD " + comment);
        List<Comment> comments;
        Project project;
        if ((project = (Project) request.getSession(false).getAttribute("project")) != null) {
            comments = project.getComments();

        } else {
            comments = new ArrayList<>();
        }

        Comment c = new Comment();
        c.setComment(comment);
        c.setPriority(priority);
        if (project != null)
            project.getComments().add(c);
        System.out.println(comments.size());
        model.addAttribute("comments", comments);
        return "fragments/components :: project-comment-list";
    }

    @PostMapping(value = "deletecomment")
    public String deleteComment(HttpServletRequest request, Model model, @RequestParam("index") int index) {
        List<Comment> comments;
        Project project;
        if ((project = (Project) request.getSession(false).getAttribute("project")) != null) {
            comments = project.getComments();

            Comment comment = comments.get(index);
            if (comment.getId() != null) {
                comment.setProject(null);
                commentRepository.delete(commentRepository.save(comment));
            }
            comments.remove(index);

            model.addAttribute("comments", comments);
        } else {


        }
        return "fragments/components :: project-comment-list";
    }

    @PostMapping(value = "project")
    public String project(@ModelAttribute ProjectForm projectForm, BindingResult errors, HttpServletRequest request, Principal principal, RedirectAttributes ra) {
        log.severe("HAS ERORS " + errors.hasErrors());
        if (errors.hasErrors())
            return "home/project";

        if (projectForm.getLikeFrequency() == null) {
            log.severe("ENTER");
            errors.reject("like");
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bind", errors);
            ra.addFlashAttribute("projectForm", projectForm);
            return "redirect:project";
        }
        Project project = (Project) request.getSession().getAttribute("project");

        System.out.println("-----------");
        System.out.println(project.getBlacklistedUsers().size());
        if (project == null)
            project = new Project();
        project = projectForm.toEntity(project);
        System.out.println(project.getComments().size());


        project.setOwner(accountRepository.findOneByEmail(principal.getName()));
        try {
            System.out.println("SAVE PROJECT " + project);
            Project finalProject = projectRepository.save(project);
            project.getComments().stream().forEach(c -> {
                c.setProject(finalProject);
                commentRepository.save(c);
            });
        } catch (Exception e) {
            log.info("POLECIAL JAKIS BLAD " + e.getMessage());
        }

        try {
            projectScheduleRunner.interruptJob(project);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        log.info("SAVING PROJECT");

        startProject(project);
        request.getSession().removeAttribute("project");
        return "redirect:projects";
    }

    @GetMapping("/start")
    public String startProject(@RequestParam("id") Long id) {
        System.out.println("START");
        Project project = projectRepository.findOne(id);
        project.setStatus(true);
        startProject(project);
        projectRepository.save(project);

        return "redirect:projects";
    }

    public void startProject(Project project) {
        System.out.println("PROJECT SCHEDULER START");
        if (project.isStatus()) {
            projectScheduleRunner.startProject(project);
        }
    }

    @PostMapping(path = "projects/delete", params = "id")
    public String deleteProject(@RequestParam(name = "id") Long id) {
        System.out.println("pppppppppppppppppp");
        System.out.println(id);

        Project project = projectRepository.znajdz(id);
        System.out.println(project);
        project.setDeleted(true);

        projectRepository.save(project);

        return "redirect:/projects";
    }
}

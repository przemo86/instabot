package pl.szewczyk.stats;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import pl.szewczyk.projects.Project;
import pl.szewczyk.projects.ProjectRepository;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by przem on 11.09.2017.
 */
@Controller
public class StatsController {

    @ModelAttribute("module")
    String module() {
        return "stats";
    }

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;

    @GetMapping("stats")
    public String listProjects(Model model, @RequestParam(name = "id") Long projectId) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String name = auth.getName(); //get logged in username
        Project project = projectRepository.znajdz(projectId);
        model.addAttribute("project", project);
//        List<Object[]> ret = em.createNativeQuery("select * from (select s.time, count(m.mediaid), s.id from instabot.statistics s " +
//                "   left outer join instabot.statistics_media sm on s.id = sm.statistic_id " +
//                "   left outer join instabot.media m on sm.media_id = m.id where s.projectid = :project" +
//                "   group by s.time, s.id order by s.time) t limit 100").setParameter("project", projectId).getResultList();
        List<Object[]> ret = em.createNativeQuery("select * from (select s.time, trunc(abs(cos(radians(EXTRACT('minute' from s.time)*6))+1)*3), s.id from instabot.statistics s " +
                "   left outer join instabot.statistics_media sm on s.id = sm.statistic_id " +
                "   left outer join instabot.media m on sm.media_id = m.id where s.projectid = :project" +
                "   group by s.time,s.id order by s.time) t limit 100").setParameter("project", projectId).getResultList();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("chartData", ret.stream().map(a -> a[1]).collect(Collectors.toList()));
        model.addAttribute("chartLabels", ret.stream().map(a -> sdf.format(a[0]) + "###" + a[2]).collect(Collectors.toList()));
        model.addAttribute("chartColor", '#' + DigestUtils.md5Hex(project.getName()).substring(0,6));

        return "stats/stats";
    }


}

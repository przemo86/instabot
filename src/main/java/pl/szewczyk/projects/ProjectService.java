package pl.szewczyk.projects;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

@Component
public class ProjectService {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager em;



    protected void initialize() {
//        Project project = new Project();
//        project.setName("Kampania iPhone X");
//        project.setCreated(Instant.now());
//        project.setCustomer("Apple Inc.");
//        project.setStatus(true);
//
//        save(project);
//
//        project = new Project();
//        project.setName("Kampania Rolex 2017");
//        project.setCreated(Instant.now());
//        project.setCustomer("Rolex Swiss");
//        project.setStatus(true);
//
//        save(project);
    }


    public void save(Project project) {
        em.persist(project);
    }

    public List<Project> listProjects() {
        return em.createQuery("select p from Project p").getResultList();
    }

    public List<Project> listActiveProjects() {
        return em.createQuery("select p from Project p where p.status = true ").getResultList();
    }
}

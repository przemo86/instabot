package pl.szewczyk.projects;

import org.springframework.stereotype.Component;
import pl.szewczyk.account.Account;
import pl.szewczyk.account.Role;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ProjectService {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager em;

    public void save(Project project) {
        em.persist(project);
    }

    public List<Project> listProjects() {
        return em.createQuery("select p from Project p").getResultList();
    }

    public List<Project> listActiveProjects() {
        return em.createQuery("select p from Project p where p.status = true ").getResultList();
    }

    public List<Project> listUserProjects(Account account) {
        if (account.getRole().equals(Role.ROLE_ADMIN)) {
            log.info("SEARCH ALL");
            return listProjects();
        } else {
            log.info("SEARCH FOR PROJECTS");
            return em.createQuery("select p from Project p join p.owner a where a.id = :account").setParameter("account", account.getId()).getResultList();
        }
    }

}

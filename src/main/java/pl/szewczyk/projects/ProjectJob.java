package pl.szewczyk.projects;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.domain.Media;
import me.postaddict.instagram.scraper.exception.InstagramException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.stats.Statistic;
import pl.szewczyk.stats.StatisticsRepository;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by przem on 20.09.2017.
 */

public abstract class ProjectJob implements Job {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private StatisticsRepository statisticsRepository;

    private Project project;

    private InstaConstants instaConstants = new InstaConstants();

    protected void init() {
        System.out.println("INIT " + instaConstants);

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        System.out.println("AFTER INIT " + instaConstants);
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Long id = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");
        Character kind = jobExecutionContext.getJobDetail().getJobDataMap().getChar("kind");
        System.out.println("Project ID " + id);

        System.out.println("xxx " + project);
        if (project == null) {
            init();
        }
        System.out.println("FIND ME ");
        project = projectRepository.znajdz(id);

        System.out.println("1  " + project);

        InstaUser instaUser = em.createQuery("from InstaUser where instaUserName = :id", InstaUser.class).setParameter("id", project.getInstagramAccount()).getSingleResult();
        System.out.println("2");

        Instagram instagram = instaConstants.getInstagramLoggedIn(instaUser.getInstaUserName());
        System.out.println("3");

        Map<String, List<Media>> tags = new HashMap<>();
        System.out.println("4");

        for (String tag : project.getIncludeHashtags().split(",")) {
            System.out.println("5 " + tag);
            try {
                List<Media> list = searchTag(tag);
                if (list != null)
                    tags.put(tag, list);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(tag + " EXCEPTION " + e.getMessage());

            }
        }
        try {
            if (HashtagSearchEnum.ANY.equals(project.getHashtagSearch())) {
                System.out.println("ANY ");
                List<Media> searches = tags.entrySet().stream()
                        .flatMap(s -> s.getValue().stream())
                        .distinct()
                        .collect(Collectors.toList());
                Iterator<Media> it = searches.iterator();
                while (it.hasNext()) {
                    Media media = it.next();
                    if (Collections.disjoint(media.getTags().stream().map(t -> t.toLowerCase()).collect(Collectors.toSet()), Arrays.asList(project.getExcludeHashtags().toLowerCase().split(",")))) {
                        System.out.println("CHECKING    === " + projectRepository.countMediaId(media.id, kind, project));
                        if (projectRepository.countMediaId(media.id, kind, project) == 0L) {
                            System.out.println("ODPALAM ROBOTE");
                            doJob(media, project.getCommentString(), instagram);
                        } else {
                            System.out.println("JUZ TO ROBILEM...");
                            it.remove();
                        }
                    } else {
                        it.remove();
                    }
                }
                System.out.println("SAVE STATS " + jobExecutionContext.getJobDetail().getJobDataMap().getChar("kind") + " WITH SEARCHES " + searches.size());
                try {
                    Statistic statistic = new Statistic(project, searches.stream().map(m -> new pl.szewczyk.stats.Media(m)).collect(Collectors.toSet()));
                    statistic.getMedia().stream().forEach(m -> m.setStatistic(statistic));
                    statistic.setKind(kind);
                    statisticsRepository.save(statistic);
                } catch (Exception e) {
                    System.out.println("EXCEPTION " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println("SAVED");
            }


            if (HashtagSearchEnum.ALL.equals(project.getHashtagSearch())) {
                System.out.println("ALL ");
                List<Media> searches = tags.entrySet().stream()
                        .flatMap(s -> s.getValue().stream())
                        .distinct()
                        .collect(Collectors.toList());
                Iterator<Media> it = searches.iterator();
                while (it.hasNext()) {
                    Media media = it.next();
                    if (Collections.disjoint(media.getTags().stream().map(t -> t.toLowerCase()).collect(Collectors.toSet()), Arrays.asList(project.getExcludeHashtags().toLowerCase().split(","))) &&
                            media.getTags().stream().map(t -> t.toLowerCase()).collect(Collectors.toSet()).containsAll(Arrays.asList(project.getIncludeHashtags().toLowerCase().split(",")))) {
                        System.out.println("CHECKING    === " + projectRepository.countMediaId(media.id, kind, project));
                        if (projectRepository.countMediaId(media.id, kind, project) == 0L) {
                            System.out.println("ODPALAM ROBOTE");
                            try {
                                doJob(media, project.getCommentString(), instagram);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("JUZ TO ROBILEM...");
                            it.remove();
                        }
                    } else {
                        it.remove();
                    }
                }
                System.out.println("SAVE STATS " + jobExecutionContext.getJobDetail().getJobDataMap().getChar("kind") + " WITH SEARCHES " + searches.size());
                try {
                    Statistic statistic = new Statistic(project, searches.stream().map(m -> new pl.szewczyk.stats.Media(m)).distinct().collect(Collectors.toSet()));
                    statistic.getMedia().stream().forEach(m -> m.setStatistic(statistic));
                    statistic.setKind(kind);
                    statisticsRepository.save(statistic);
                } catch (Exception e) {
                    System.out.println("EXCEPTION " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println("SAVED");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Media> searchTag(String tag) {
        Instagram instagram = instaConstants.getInstagramAnonymous();

        System.out.println("TAG SEARCH " + tag);
        List<Media> mediaFeed = null;
        try {
            mediaFeed = instagram.getMediasByTag(tag, 1000);
            System.out.println("FOUND " + mediaFeed.size());
        } catch (InstagramException e) {
            System.err.println("ERRROR SEARCHING TAG " + tag + "   " + project.getName());
            System.err.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("XXXXXXXXXXX exce");
            e.printStackTrace();
        }
        return mediaFeed;
    }


    abstract void doJob(Media media, String comment, Instagram instagram) throws IOException;

}

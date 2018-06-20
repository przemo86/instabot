package pl.szewczyk.projects;

import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.domain.Media;
import me.postaddict.instagram.scraper.exception.InstagramException;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.szewczyk.instagram.InstaConstants;
import pl.szewczyk.instagram.InstaUser;
import pl.szewczyk.stats.MediaRepository;
import pl.szewczyk.stats.Statistic;
import pl.szewczyk.stats.StatisticsRepository;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by przem on 20.09.2017.
 */

@DisallowConcurrentExecution
public class ProjectJob implements InterruptableJob {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private StatisticsRepository statisticsRepository;

    private Project project;

    private InstaConstants instaConstants = new InstaConstants();

    private Instagram loggedInInstagram;

    private Long id;

    protected void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    private Thread currentThread;
    private AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        currentThread = Thread.currentThread();
        id = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");
        log.info("Project ID " + id);

        if (project == null) {
            init();

            project = projectRepository.znajdz(id);

            if (loggedInInstagram == null) {
                InstaUser instaUser = em.createQuery("from InstaUser where instaUserName = :id", InstaUser.class).setParameter("id", project.getInstagramAccount()).getSingleResult();
                loggedInInstagram = instaConstants.getInstagramLoggedIn(instaUser.getInstaUserName());
                if (loggedInInstagram == null) {
                    log.severe("NIE ZALOGOWANY DO INSTAGRAMA :(");
                    return;
                }
            }
        }
        Map<String, List<Media>> tags = new HashMap<>();
        if (project.getIncludeHashtags() != null && !"".equals(project.getIncludeHashtags())) {

            for (String tag : project.getIncludeHashtags().split(",")) {
                try {
                    List<Media> list = searchTag(tag);
                    if (list != null)
                        tags.put(tag, list);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.severe(tag + " EXCEPTION DURING SEARCG" + e.getMessage());

                }
            }
        }
        List<Media> locationsMedia = new ArrayList<>();
        if (project.getLocationId() != null && !"".equals(project.getLocationId())) {
            locationsMedia = searchLocation(project.getLocationId());
        }
        try {
            Statistic statistic = new Statistic(project, new HashSet<>());
            statistic = statisticsRepository.save(statistic);
//            if (HashtagSearchEnum.ANY.equals(project.getHashtagSearch())) {
//
//                log.info("ANY ");
//                List<Media> searches = tags.entrySet().stream()
//                        .flatMap(s -> s.getValue().stream())
//                        .distinct()
//                        .collect(Collectors.toList());
//                Iterator<Media> it = searches.iterator();
//                while (it.hasNext() && !interrupted.get()) {
//                    log.info("EXEC CURRENT THREAD " + currentThread.getId());
//                    currentThread.sleep(5000 + (long) (Math.random() * 10000));
//
//                    Media media = it.next();
//                    if (Collections.disjoint(media.getTags().stream().map(t -> t.toLowerCase()).collect(Collectors.toSet()), Arrays.asList(project.getExcludeHashtags().toLowerCase().split(",")))) {
//                        if (projectRepository.countMediaId(media.id, project) == 0L) {
//                            log.info("BEDE ROBIL " + media.shortcode + " " + (project.isLike() ? "L" : "") + " " +
//                                    (project.isComment() ? "C" : ""));
//                            if (project.isLike()) {
//                                log.info("LIKE ME " + instagram);
//                                media.liked = like(media, instagram);
//                            } else {
//                                media.liked = false;
//                            }
//
//                            media.action_time = new Date();
//
//                            if (project.isComment()) {
//                                media.myComment = project.getRandomComment();
//                                media.commented = comment(media, media.myComment, instagram);
//                            } else {
//                                media.commented = false;
//                            }
//                            pl.szewczyk.stats.Media media1 = new pl.szewczyk.stats.Media(media);
//                            media1.setStatistic(statistic);
//                            statistic.getMedia().add(media1);
//                            mediaRepository.save(media1);
//                            log.info("SAVE STATS");
//                            //                                it.remove();
//                        } else {
//                            log.info("JUZ TO ROBILEM... " + media.shortcode);
//                            it.remove();
//                        }
//                    } else {
//                        log.info(media.shortcode + " nie pasuja hashtagi");
//                        it.remove();
//                    }
//
//                }
//            }


            List<Media> searches = tags.entrySet().stream()
                    .flatMap(s -> s.getValue().stream())
                    .distinct()
                    .collect(Collectors.toList());


            if (project.getIncludeHashtags() != null && !"".equals(project.getIncludeHashtags())) {
                if (project.getLocationId() != null && !"".equals(project.getLocationId())) {
                    List<Media> finalLocationsMedia = locationsMedia;
                    searches = searches.stream().filter(s -> finalLocationsMedia.contains(s)).collect(Collectors.toList());
                }
            } else {
                if (project.getLocationId() != null && !"".equals(project.getLocationId())) {
                    searches = searchLocation(project.getLocationId());
                }
            }


            //include hashtags (many?) filter
            log.info("AFTER INCLUDE AND LOCATIONS FILTER = " + searches.size());
            searches = searches.stream()
                    .filter(s -> Collections.disjoint(
                            s.getTags().stream()
                                    .map(t -> t.toLowerCase())
                                    .collect(Collectors.toSet()), Arrays.asList(project.getExcludeHashtags().toLowerCase().split(","))
                    )).collect(Collectors.toList());
            log.info("AFTER EXCLUDE " + searches.size());
            log.info("---------");


            //blacklisted words filter
            if (project.getBlacklisted() != null && !"".equals(project.getBlacklisted())) {
                List blackListed = Arrays.asList(project.getBlacklisted().toLowerCase().split("\n")).stream().map(b -> Arrays.asList(b.split(","))).collect(Collectors.toList());

                searches.stream().filter(s -> Collections.disjoint(Arrays.asList(s.caption.toLowerCase().split(" ")), blackListed)).collect(Collectors.toList());
            }
            log.info("AFTER BLACKLIST FILTER " + searches.size());


            //user details search
            log.info("OWNER MANUAL " + searches.size());
//            if ((Objects.nonNull(project.getMinObserved()) && project.getMinObserved().longValue() > 0) || (Objects.nonNull(project.getBlacklistedUsers()) && !project.getBlacklistedUsers().isEmpty())) {
                searches.stream().forEach(m -> {
                    try {
                        m.owner = loggedInInstagram.getAccountById(Long.parseLong(m.ownerId));
                    } catch (IOException e) {
                        log.info(e.getMessage());
                    }
                });
//            }

            //min followers filter
            if (Objects.nonNull(project.getMinObserved()) && project.getMinObserved() > 0) {
                log.info("MIN OBSERVED SEARCH " + project.getMinObserved());
                searches.stream().forEach(s -> System.out.println(s.owner.username + " has " + s.owner.followedByCount + " observers"));

                searches.stream().filter(s -> s.owner.followedByCount > project.getMinObserved());
            }
            log.info("AFTER MIN OBSERVED " + searches.size());

            //blacklisted user filter
            if (project.getBlacklistedUsers() != null && !project.getBlacklistedUsers().isEmpty()) {
                List blackListed = project.getBlacklistedUsers().stream().map(b -> b.getUsername()).collect(Collectors.toList());
                searches.stream().forEach(s -> System.out.println(s.owner.username + " " + (blackListed.contains(s.owner.username) ? "is" : "isn't") + " blacklisted"));
                searches.stream().filter(s -> !blackListed.contains(s.owner.username));
            }
            log.info("AFTER BLACKLIST FILTER " + searches.size());

            //capping filter
            if (project.isCapping() && project.getCappingTime() > 0) {

                List blackListed = project.getBlacklistedUsers().stream().map(b -> b.getUsername()).collect(Collectors.toList());

                searches.stream().filter(s -> !blackListed.contains(s.owner.username));
            }
            log.info("AFTER capping " + searches.size());


            if (searches.size() > 20) {
                Collections.shuffle(searches);
                searches = searches.subList(0, 19);
            }

            log.info("AFTER SHUFFLING AND LIMITATION " + searches.size());

            Iterator<Media> it = searches.iterator();
            while (it.hasNext() && !interrupted.get()) {
                Thread.sleep(5000 + (long) (Math.random() * 10000));
                Media media = it.next();
                log.info("in while " + media.shortcode);
//                    if (media.getTags() != null) {
                if ((HashtagSearchEnum.ALL.equals(project.getHashtagSearch()) &&
                        media.getTags().stream().map(t -> t.toLowerCase()).collect(Collectors.toSet()).containsAll(Arrays.asList(project.getIncludeHashtags().toLowerCase().split(","))))
                        || HashtagSearchEnum.ANY.equals(project.getHashtagSearch()) || HashtagSearchEnum.NONE.equals(project.getHashtagSearch())) {

                    pl.szewczyk.stats.Media media1;
                    if (null == (media1 = mediaRepository.findByProjectAndMediaId(media.id, project.getId()))) {
                        media1 = new pl.szewczyk.stats.Media(media);
                        media1.setStatistic(statistic);
                    }
                    statistic.getMedia().add(media1);
                    log.info("MEDIA1 " + media1);
                    log.info("WORKING WITH " + media.shortcode + " Like " + project.isLike() + ", comment " + project.isComment() + ", follow " + project.isFollow());
                    if (project.isLike()) {
                        if (mediaRepository.countMediaLiked(media.id, project.getId()) == 0L)
                            try {
                                media1.setLiked(like(media, loggedInInstagram));
                                log.info("LIKED");
                            } catch (IOException e) {
                                media1.setLiked(false);
                                break;
                            }
                    } else {
                        media.liked = false;
                    }

                    if (project.isComment()) {
                        if (mediaRepository.countMediaCommented(media.id, project.getId()) == 0L) {
                            media.myComment = project.getRandomComment();
                            media1.setMyComment(media.myComment);
                            try {
                                media1.setCommented(comment(media, media.myComment, loggedInInstagram));
                                log.info("COMMENTED");
                            } catch (Exception e) {
                                media1.setCommented(false);
                                break;
                            }
                        }
                    } else {
                        media.commented = false;
                    }

                    if (project.isFollow()) {
                        log.info("FOLLOW USER " + media.ownerId);
                        loggedInInstagram.followUser(media.ownerId);
                    }


                    log.info("SAVE MEDIA 1");
//                    media1.setStatistic(statistic);
                    if (Objects.nonNull(media.owner)) {
                        media1.setUserFollowed(media.owner.followedByCount);
                        media1.setUserProfileImage(media.owner.profilePicUrl);
                        media1.setUserId(Long.parseLong(media.ownerId));
                    }

                    media1 = mediaRepository.save(media1);

                    log.info("SAVE MEDIA " + media1.getId());
                    log.info(media1.toString());
                    //                                it.remove();

                } else if (project.getHashtagSearch() == null) {
                    pl.szewczyk.stats.Media media1;
                    if (null == mediaRepository.findByProjectAndMediaId(media.id, project.getId())) {
                        media1 = new pl.szewczyk.stats.Media(media);
                        media1.setStatistic(statistic);
                        statistic.getMedia().add(media1);
                        log.info("SAVE MEDIA ");
                        mediaRepository.save(media1);
                    }
                } else {
                    log.info("TAGI NIE PASUJA " + media.getTags());
                    it.remove();
                }
            }


//            loggedInInstagram.likeMenttions();

            log.info("FINISHED " + project.getName() + " WITH " + statistic.getMedia().size() + " HITS");

        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }

    public List<Media> searchLocation(String locationId) {
        Instagram instagram = instaConstants.getInstagramAnonymous();

        log.info("LOCATION SEARCH " + locationId);
        List<Media> mediaFeed = null;
        try {
            mediaFeed = instagram.getLocationMediasById(locationId, 20);
            log.info("FOUND " + mediaFeed.size());
            if (null != project.getMediaAge()) {
                long maxAge = System.currentTimeMillis() - (project.getMediaAge().longValue() * 60 /*min*/ * 60 /*sec*/ * 1000/*msec*/);
                mediaFeed = mediaFeed.stream().filter(m -> m.createdTime >= maxAge).collect(Collectors.toList());
            }
            log.info("PO FILTRZE CZASOWYM " + mediaFeed.size());
        } catch (InstagramException e) {
            log.severe("ERRROR SEARCHING LOCATION " + locationId + "   " + project.getName());
            log.severe(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaFeed;
    }

    public List<Media> searchTag(String tag) {
        Instagram instagram = instaConstants.getInstagramAnonymous();

        log.info("TAG SEARCH " + tag);
        List<Media> mediaFeed = null;
        try {
            mediaFeed = instagram.getMediasByTag(tag, 20, "");
            log.info("FOUND " + mediaFeed.size());
            if (null != project.getMediaAge()) {
                long maxAge = System.currentTimeMillis() - (project.getMediaAge().longValue() * 60 /*min*/ * 60 /*sec*/ * 1000/*msec*/);
                mediaFeed = mediaFeed.stream().filter(m -> m.createdTime >= maxAge).collect(Collectors.toList());
            }
            log.info("PO FILTRZE CZASOWYM " + mediaFeed.size());
        } catch (InstagramException e) {
            log.severe("ERRROR SEARCHING TAG " + tag + "   " + project.getName());
            log.severe(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaFeed;
    }


    private boolean comment(Media media, String comment, Instagram instagram) throws Exception {
        instagram.addMediaComment(media.shortcode, comment);
        return true;

    }

    private boolean like(Media media, Instagram instagram) throws IOException {
        instagram.likeMediaByCode(media.shortcode);

        return true;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        log.info("INTERRUPT JOB");
        if (currentThread != null) {
            log.info("MY JOB " + currentThread.getId());
            interrupted.set(true);
            currentThread.interrupt();

        } else {
            log.info("NIE UMIEM ZATRZYMAC NULLA");
        }
    }

    public List<String> getLastCommentedUsers(Integer minutes) {


        return null;
    }
}

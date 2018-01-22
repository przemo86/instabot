package pl.szewczyk.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query(value = "select m " +
            "from Media m " +
            "where m.mediaId = :mediaid and m.statistic.project.id = :projectid")
    Media findByProjectAndMediaId(@Param("mediaid") String mediaid, @Param("projectid") Long project);

    @Query(value = "select count(*) " +
            "from instabot.media m join " +
            "instabot.statistics s on s.id = m.stat_id " +
            "where m.mediaid = :mediaid and s.projectid = :projectid and m.liked = true", nativeQuery = true)
    Long countMediaLiked(@Param("mediaid") String mediaid, @Param("projectid") Long project);

    @Query(value = "select count(*) " +
            "from instabot.media m join " +
            "instabot.statistics s on s.id = m.stat_id " +
            "where m.mediaid = :mediaid and s.projectid = :projectid and m.commented = true", nativeQuery = true)
    Long countMediaCommented(@Param("mediaid") String mediaid, @Param("projectid") Long project);
}
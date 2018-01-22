package pl.szewczyk.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistic, Long> {

    @Query(value = "SELECT s FROM Statistic s where s.project.id = :id order by s.time")
    List<Statistic> findStatisticByProject(@Param("id") Long id);

//    @Query(value = "select mediaid from instabot.media where id = (" +
//            "select max(m.id) from instabot.media m join instabot.statistics s ON m.stat_id = s.id " +
//            "where s.projectid = :project and s.kind = :kind)", nativeQuery = true)
//    String findLastMediaId(@Param("project") Long project, @Param("kind") Character kind);

    @Query(value = "select mediaid from instabot.media where id = (" +
            "select max(m.id) from instabot.media m join instabot.statistics s ON m.stat_id = s.id " +
            "where s.projectid = :project)", nativeQuery = true)
    String findLastMediaId(@Param("project") Long project);
}
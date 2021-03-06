package pl.szewczyk.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(value = "SELECT p FROM Project p where p.id = :id and p.deleted = false")
    Project znajdz(@Param("id") Long id);

    @Query(value = "select count(*) " +
            "from instabot.media m join " +
            "instabot.statistics s on s.id = m.stat_id " +
            "where m.mediaid = :mediaid and s.projectid = :projectid and s.kind = :kind and (select deleted from instabot.project where id = :projectid) = false", nativeQuery = true)
    Long countMediaId(@Param("mediaid") String mediaid, @Param("kind") Character kind, @Param("projectid") Project project);


    @Query(value = "select count(*) " +
            "            from instabot.media m join" +
            "            instabot.statistics s on s.id = m.stat_id" +
            "            where s.projectid = :projectid and s.kind = :kind and (select deleted from instabot.project where id = :projectid) = false", nativeQuery = true)
    Long count(@Param("kind") Character kind, @Param("projectid") Project project);




    @Query(value = "select count(*) " +
            "from instabot.media m join " +
            "instabot.statistics s on s.id = m.stat_id " +
            "where m.mediaid = :mediaid and s.projectid = :projectid and (select deleted from instabot.project where id = :projectid) = false", nativeQuery = true)
    Long countMediaId(@Param("mediaid") String mediaid, @Param("projectid") Project project);

    @Query(value = "select count(*) " +
            "            from instabot.media m join" +
            "            instabot.statistics s on s.id = m.stat_id" +
            "            where s.projectid = :projectid and (select deleted from instabot.project where id = :projectid) = false", nativeQuery = true)
    Long count(@Param("projectid") Project project);
}
package pl.szewczyk.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(value = "SELECT p FROM Project p join fetch p.excludeHashtags JOIN FETCH p.includeHashtags where p.id = :id")
    Project znajdz(@Param("id") Long id);

@Query(value = "select count(*) " +
        "from media m join " +
        "statistics_media sm on m.id = sm.media_id join " +
        "statistics s on s.id = sm.statistic_id " +
        "where m.mediaid = :mediaid and s.projectid = :projectid and s.kind = :kind", nativeQuery = true)
    Long countMediaId(@Param("mediaid") String mediaid, @Param("kind") Character kind, @Param("projectid") Project project);

    @Query(value = "select count(*) " +
            "from media m join " +
            "statistics_media sm on m.id = sm.media_id join " +
            "statistics s on s.id = sm.statistic_id " +
            "where s.projectid = :projectid and s.kind = :kind", nativeQuery = true)
    Long count(@Param("kind") Character kind, @Param("projectid") Project project);
}
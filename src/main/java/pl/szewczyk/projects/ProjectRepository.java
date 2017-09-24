package pl.szewczyk.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(value = "SELECT p FROM Project p join fetch p.excludeHashtags JOIN FETCH p.includeHashtags where p.id = :id")
    Project znajdz(@Param("id") Long id);

}
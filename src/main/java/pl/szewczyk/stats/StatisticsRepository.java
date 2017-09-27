package pl.szewczyk.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistic, Long> {

//    @Query(value = "SELECT p FROM Project p join fetch p.excludeHashtags JOIN FETCH p.includeHashtags where p.id = :id")
//    Project znajdz(@Param("id") Long id);

}
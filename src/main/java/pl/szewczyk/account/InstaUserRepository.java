package pl.szewczyk.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.szewczyk.instagram.InstaUser;

@Repository
public interface InstaUserRepository extends JpaRepository<InstaUser, Long> {
    @Query(value = "SELECT p FROM InstaUser p where p.instaUserName = :username")
    InstaUser findByUserName(@Param("username") String username);

}
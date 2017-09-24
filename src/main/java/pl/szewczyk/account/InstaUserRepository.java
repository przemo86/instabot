package pl.szewczyk.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.szewczyk.instagram.InstaUser;

@Repository
public interface InstaUserRepository extends JpaRepository<InstaUser, Long> {


}
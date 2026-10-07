package in.sharvarimore.fileshareapi.respository;

import in.sharvarimore.fileshareapi.document.UserCredits;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserCreditsRepository extends MongoRepository<UserCredits, String> {

    // Optional handle null values safely that helps avoid NullPointerException
    Optional<UserCredits> findByClerkId(String clerkId);
}

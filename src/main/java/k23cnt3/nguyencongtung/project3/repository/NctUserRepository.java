package k23cnt3.nguyencongtung.project3.repository;

import k23cnt3.nguyencongtung.project3.entity.NctUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NctUserRepository extends JpaRepository<NctUser, Long> {
    /**
     * Finds a user by their username.
     * @param nctUsername the username to search for.
     * @return an Optional containing the user if found, otherwise empty.
     */
    Optional<NctUser> findByNctUsername(String nctUsername);

    /**
     * Finds a user by their email address.
     * @param nctEmail the email to search for.
     * @return an Optional containing the user if found, otherwise empty.
     */
    Optional<NctUser> findByNctEmail(String nctEmail);

    /**
     * Checks if a user exists with the given username.
     * @param nctUsername the username to check.
     * @return true if a user exists, false otherwise.
     */
    boolean existsByNctUsername(String nctUsername);

    /**
     * Checks if a user exists with the given email.
     * @param nctEmail the email to check.
     * @return true if a user exists, false otherwise.
     */
    boolean existsByNctEmail(String nctEmail);

    /**
     * Counts the number of users with a specific role.
     * @param role the role to count users for.
     * @return the total number of users with the given role.
     */
    long countByNctRole(NctUser.NctRole role);

    /**
     * Searches for users whose username, email, or full name contains the given keyword.
     * @param username the keyword to search for in the username.
     * @param email the keyword to search for in the email.
     * @param fullName the keyword to search for in the full name.
     * @return a list of matching users.
     */
    List<NctUser> findByNctUsernameContainingOrNctEmailContainingOrNctFullNameContaining(String username, String email, String fullName);
}

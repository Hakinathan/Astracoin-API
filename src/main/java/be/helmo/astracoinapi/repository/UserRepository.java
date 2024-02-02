package be.helmo.astracoinapi.repository;

import be.helmo.astracoinapi.model.entity.Role;
import be.helmo.astracoinapi.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    @Query("select u from User u where upper(u.mail) = upper(:mail)")
    User findByMailEqualsIgnoreCase(@Param("mail") @NonNull String mail);
    User findById(@NonNull Long id);
    @Query("select u from User u where u.username = ?1")
    User findById(@NonNull String id1);

    List<User> findAll();

    List<User> findByRole(Role role);
}

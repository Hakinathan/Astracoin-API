package be.helmo.astracoinapi.repository;

import be.helmo.astracoinapi.model.entity.Folder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Integer> {

    @Query("select f from Folder f where f.currency.id = ?1 and f.user.id = ?2")
    Folder findByIdAndCoin(String id, long id1);

    @Query("select f from Folder f where f.user.id = ?1")
    List<Folder> findById(Long id1);

    @Query("select f from Folder f where f.user.username = ?1")
    List<Folder> findById(String id1);

    @Query("select f from Folder f where UPPER(f.user.mail) = UPPER(?1)")
    List<Folder> findByEmail(String id1);


    List<Folder> findByCurrency_IdEqualsAndUser_NotifiedIsTrue(@NonNull String id);




}

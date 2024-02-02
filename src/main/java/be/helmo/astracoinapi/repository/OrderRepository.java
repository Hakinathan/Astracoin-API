package be.helmo.astracoinapi.repository;

import be.helmo.astracoinapi.model.entity.DirectionOrder;
import be.helmo.astracoinapi.model.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Integer> {
    @Query("select o from Order o where o.folder.user.id = ?1 order by o.executedAt DESC")
    List<Order> findByFolder_User_Id(long id);

}

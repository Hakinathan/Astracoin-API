package be.helmo.astracoinapi.repository;

import be.helmo.astracoinapi.model.entity.Currency;
import org.hibernate.annotations.WhereJoinTable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, Integer> {

    @Override
    List<Currency> findAll();

    Currency findByIdEquals(String id);



    //@Query(value = "select c from Currency c inner join MarketData m on m.currency = c where m.issued = (select max(m1.issued) from MarketData m1)")
    //List<Currency> getAllWithLastMarketData();
}

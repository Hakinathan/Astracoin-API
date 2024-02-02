package be.helmo.astracoinapi.repository;

import be.helmo.astracoinapi.model.entity.MarketData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MarketDataRepository extends CrudRepository<MarketData, Integer> {
    @Query("select m from MarketData m where upper(m.currency.id) = upper(:id) AND m.issued >= :datetime  order by m.issued")
    List<MarketData> findMarketDataByCurrency(@Param("id") @NonNull String id, @Param("datetime") @NonNull Date dateTime);

    @Query(nativeQuery = true ,value = "select AVG(A.b) from (SELECT m.eur_value as b FROM market_data m where m.currency_id = :id order by m.issued DESC limit 1,:max) A")
    Double findAverageValue(@Param("id") String id, @Param("max") int max);

    MarketData findFirstByCurrency_IdIsOrderByIssuedDesc(String id);







}

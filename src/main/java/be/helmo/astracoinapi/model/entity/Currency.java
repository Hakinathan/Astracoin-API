package be.helmo.astracoinapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.WhereJoinTable;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    private String id;

    @NotNull
    @Column(name = "stock_symbol", length = 20)
    private String symbol;

    @NotNull
    @Column(name = "name", length = 30)
    private String name;

    @NotNull
    @Column(name = "stock_image", length = 255)
    private String stockImage;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "currency")
    private List<MarketData> marketDatas;

    @NotNull
    @Column(name = "eur_value")
    @JsonProperty("eur")
    private double EURValue;

    @Temporal(TemporalType.TIMESTAMP)
    private Date issued;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStockImage() {
        return stockImage;
    }

    public void setStockImage(String stockImage) {
        this.stockImage = stockImage;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<MarketData> getMarketDatas() {
        return marketDatas;
    }

    public void setMarketDatas(List<MarketData> marketDatas) {
        this.marketDatas = marketDatas;
    }

    public double getEURValue() {
        return EURValue;
    }

    public void setEURValue(double EURValue) {
        this.EURValue = EURValue;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }
}

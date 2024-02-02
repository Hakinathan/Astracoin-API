package be.helmo.astracoinapi.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sun.istack.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "market_data")
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonBackReference
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Currency currency;

    @NotNull
    @Column(name = "eur_value")
    private double EURValue;

    @Temporal(TemporalType.TIMESTAMP)
    private Date issued;

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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}

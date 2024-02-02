package be.helmo.astracoinapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ordering")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Column(name = "nature")
    @Enumerated(EnumType.STRING)
    private TypeOrder typeOrder;

    @NotNull
    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    private DirectionOrder direction;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    private Folder folder;

    @NotNull
    private float value;

    @NotNull
    @Column(name = "issued_at")
    private Date issuedAt;

    @Column(name = "executed_at")
    private Date executedAt;

    public TypeOrder getTypeOrder() {
        return typeOrder;
    }

    public void setTypeOrder(TypeOrder typeOrder) {
        this.typeOrder = typeOrder;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Date executedAt) {
        this.executedAt = executedAt;
    }

    public DirectionOrder getDirection() {
        return direction;
    }

    public void setDirection(DirectionOrder direction) {
        this.direction = direction;
    }
}

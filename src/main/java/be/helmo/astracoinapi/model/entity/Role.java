package be.helmo.astracoinapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private short id;

    @Column(name = "name", length = 30)
    private String name;

    public Role(short id, String name) {
        this.id = id;
        this.name = name;
    }

    public Role(){
        super();
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

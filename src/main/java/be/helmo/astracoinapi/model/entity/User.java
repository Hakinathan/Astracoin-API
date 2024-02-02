package be.helmo.astracoinapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Column(name = "email", length = 320)
    private String mail;

    @Column(name = "token", length = 255)
    private String token;

    @Column(name = "fcm", length = 255)
    private String fcm;

    @Column(name = "username", length = 60)
    private String username;

    @NotNull
    @JsonIgnore
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "created_at")
    @JsonIgnore
    private Date createAt;

    @Column(name = "wallet_euro")
    private float walletEuro;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Role role;

    @OneToMany
    private List<Folder> folders = new java.util.ArrayList<>();

    @NotNull
    @Column(name = "notified", columnDefinition = "boolean default true")
    private boolean notified;

    @NotNull
    @Column(name = "blocked", columnDefinition = "boolean default false")
    private boolean blocked;

    public List<Folder> getFolders() {
        return folders;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User() {
        super();
    }

    public Role getRole() {
        return role;
    }

    public User(long id, String mail, String token, String password, Date createAt, Role role) {
        this.id = id;
        this.mail = mail;
        this.token = token;
        this.password = password;
        this.createAt = createAt;
        this.role = role;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMail() {
        return mail;
    }

    public long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String nickname) {
        this.username = nickname;
    }

    public float getWalletEuro() {
        return walletEuro;
    }

    public void setWalletEuro(float walletEuro) {
        this.walletEuro = walletEuro;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getFcm() {
        return fcm;
    }

    public void setFcm(String fcm) {
        this.fcm = fcm;
    }
}

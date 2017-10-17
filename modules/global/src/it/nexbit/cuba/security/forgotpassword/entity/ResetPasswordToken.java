package it.nexbit.cuba.security.forgotpassword.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Date;

@Table(name = "NXSECFP_RESET_PASSWORD_TOKEN")
@Entity(name = "nxsecfp$ResetPasswordToken")
@SystemLevel
public class ResetPasswordToken extends BaseUuidEntity {
    private static final long serialVersionUID = -6467932385185823193L;
    public static final int TOKEN_LENGTH = 64;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", unique = true)
    protected User user;

    @Column(name = "TOKEN", nullable = false, length = TOKEN_LENGTH)
    protected String token;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EXPIRE_AT", nullable = false)
    protected Date expireAt;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }


}
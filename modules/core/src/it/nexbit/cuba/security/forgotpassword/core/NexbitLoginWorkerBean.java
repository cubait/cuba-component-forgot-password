/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.security.app.LoginWorkerBean;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.LoginException;

import javax.annotation.Nullable;
import java.util.List;

public class NexbitLoginWorkerBean extends LoginWorkerBean {

    @Nullable
    @Override
    public User loadUser(String login) throws LoginException {
        // first check if login is a valid user's email, and find the corresponding login name
        // to pass to the super impl
        if (login == null)
            throw new IllegalArgumentException("Login is null");

        EntityManager em = persistence.getEntityManager();
        String queryStr = "select u.loginLowerCase from sec$User u where lower(u.email) = ?1 and (u.active = true or u.active is null)";

        Query q = em.createQuery(queryStr);
        q.setParameter(1, login.toLowerCase());
        List results = q.getResultList();
        if (!results.isEmpty())
            return super.loadUser((String) results.get(0));

        return super.loadUser(login);
    }
}

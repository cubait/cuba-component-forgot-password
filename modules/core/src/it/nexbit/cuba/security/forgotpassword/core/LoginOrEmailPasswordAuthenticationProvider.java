/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.security.auth.providers.LoginPasswordAuthenticationProvider;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.LoginException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

/**
 * Extend the built-in LoginOrEmailPasswordAuthenticationProvider by adding the ability to authenticate
 * using the user's email, in addition to the login name.
 *
 * @implNote Requires a UNIQUE constraint on the email field of the User entity.
 */
public class LoginOrEmailPasswordAuthenticationProvider extends LoginPasswordAuthenticationProvider {

    @Inject
    public LoginOrEmailPasswordAuthenticationProvider(Persistence persistence, Messages messages) {
        super(persistence, messages);
    }

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

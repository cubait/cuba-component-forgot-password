/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.core;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.app.UserManagementService;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.LoginException;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import it.nexbit.cuba.security.forgotpassword.app.NexbitUserManagementService;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import it.nexbit.cuba.security.forgotpassword.entity.ResetPasswordLinkInfo;
import it.nexbit.cuba.security.forgotpassword.entity.ResetPasswordToken;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

@Service(NexbitUserManagementService.NAME)
public class NexbitUserManagementServiceBean implements NexbitUserManagementService {

    private static final Logger log = LoggerFactory.getLogger(NexbitUserManagementService.class);

    @Inject
    protected LoginOrEmailPasswordAuthenticationProvider authenticationProvider;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ForgotPasswordConfig forgotPasswordConfig;

    @Inject
    private TimeSource timeSource;

    @Inject
    protected Resources resources;

    @Inject
    protected Scripting scripting;

    @Inject
    protected MessageTools messageTools;

    @Inject
    protected EmailerAPI emailerAPI;

    @Inject
    protected UserManagementService userManagementService;

    @Inject
    protected PasswordEncryption passwordEncryption;

    @Override
    @Transactional(readOnly = true)
    public User findUser(@NotNull String loginOrEmail) {
        checkNotNullArgument(loginOrEmail, "loginOrEmail must not be null");
        try {
            return authenticationProvider.loadUser(loginOrEmail);
        } catch (LoginException e) {
            return null;
        }
    }

    @Override
    public boolean checkUserExist(@NotNull String loginOrEmail, boolean onlyActive) {
        checkNotNullArgument(loginOrEmail, "loginOrEmail must not be null");
        User user;
        try (Transaction tx = persistence.createTransaction(new TransactionParams().setReadOnly(true))) {
            user = findUser(loginOrEmail);
            tx.commit();
        }
        return user != null && (!onlyActive || user.getActive());
    }

    @Override
    @Transactional
    public ResetPasswordLinkInfo sendResetPasswordLink(@NotNull String loginOrEmail) {
        checkNotNullArgument(loginOrEmail, "loginOrEmail must not be null");
        final String baseUrl = forgotPasswordConfig.getResetPasswordLinkPortalUrl();
        return sendResetPasswordLink(loginOrEmail, baseUrl);
    }

    @Override
    @Transactional
    public @NotNull ResetPasswordLinkInfo sendResetPasswordLink(@NotNull String loginOrEmail, @NotNull String baseUrl) {
        checkNotNullArgument(loginOrEmail, "loginOrEmail must not be null");
        checkNotNullArgument(baseUrl, "baseUrl must not be null");
        final User user = findUser(loginOrEmail);
        if (user != null) {
            final Map<UUID, ResetPasswordLinkInfo> result =
                    sendResetPasswordLinks(Collections.singletonList(user.getId()), baseUrl, false);
            return result.get(user.getId());
        } else {
            return ResetPasswordLinkInfo.KO_NOT_FOUND;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserForResetPasswordTokenIfValid(@NotNull String token) {
        checkNotNullArgument(token, "token must not be null");

        ResetPasswordToken tokenEntity = findResetPasswordToken(token);

        if (tokenEntity != null) {
            // check token expiration
            if (checkResetPasswordTokenAndRemoveIfInvalid(tokenEntity)) {
                return tokenEntity.getUser();
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckTokenResponse checkResetPasswordToken(@NotNull String token) {
        User user = getUserForResetPasswordTokenIfValid(token);
        if (user != null) {
            return new CheckTokenResponse(user, true);
        }
        return new CheckTokenResponse(null, false);
    }

    @Override
    @Transactional
    public void deleteResetPasswordToken(@NotNull String token) {
        ResetPasswordToken tokenEntity = findResetPasswordToken(token);
        if (tokenEntity != null) {
            EntityManager em = persistence.getEntityManager();
            em.remove(tokenEntity);
        }
    }

    @Override
    @Transactional
    public boolean changePasswordWithToken(@NotNull String token, @NotNull String password) {
        try {
            ResetPasswordToken tokenEntity = findResetPasswordToken(token);
            if (tokenEntity != null) {
                String passwordHash = passwordEncryption.getPasswordHash(
                        tokenEntity.getUser().getId(), password);
                userManagementService.changeUserPassword(tokenEntity.getUser().getId(), passwordHash);
                EntityManager em = persistence.getEntityManager();
                em.remove(tokenEntity);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    // REQUIRES OPEN TRANSACTION
    protected ResetPasswordToken findResetPasswordToken(String token) {
        EntityManager em = persistence.getEntityManager();
        TypedQuery<ResetPasswordToken> query = em.createQuery(
                "select t from nxsecfp$ResetPasswordToken t where t.token = :token", ResetPasswordToken.class);
        query.setViewName("resetPasswordToken.validate");
        query.setParameter("token", token);

        return query.getFirstResult();
    }

    // REQUIRES OPEN TRANSACTION
    protected boolean checkResetPasswordTokenAndRemoveIfInvalid(ResetPasswordToken tokenEntity) {
        if (tokenEntity.getExpireAt().before(timeSource.currentTimestamp())) {
            EntityManager em = persistence.getEntityManager();
            em.remove(tokenEntity);
            em.flush();
            return false;
        }
        return true;
    }

    @Override
    public @NotNull Map<UUID, ResetPasswordLinkInfo> sendResetPasswordLinks(@NotNull List<UUID> userIds,
                                                                            @NotNull String baseUrl,
                                                                            boolean includeInactive) {
        checkNotNullArgument(userIds, "userIds must not be null");
        checkNotNullArgument(baseUrl, "baseUrl must not be null");
        if (userIds.isEmpty())
            return Collections.emptyMap();

        // email templates
        final String resetPasswordBodyTemplate = forgotPasswordConfig.getResetPasswordLinkEmailBodyTemplate();
        final String resetPasswordSubjectTemplate = forgotPasswordConfig.getResetPasswordLinkEmailSubjectTemplate();

        final SimpleTemplateEngine templateEngine = new SimpleTemplateEngine(scripting.getClassLoader());

        Map<String, Template> localizedBodyTemplates = new HashMap<>();
        Map<String, Template> localizedSubjectTemplates = new HashMap<>();

        // load default
        Template bodyDefaultTemplate = loadDefaultTemplate(resetPasswordBodyTemplate, templateEngine);
        Template subjectDefaultTemplate = loadDefaultTemplate(resetPasswordSubjectTemplate, templateEngine);

        final Map<UUID, ResetPasswordLinkInfo> results = new HashMap<>();
        final Map<User, String> tokens = generateResetPasswordTokens(new LinkedHashSet<>(userIds), includeInactive);
        for (Map.Entry<User, String> entry : tokens.entrySet()) {
            final User user = entry.getKey();
            if (entry.getValue() == null)   // if includeInactive == false and user is not active
                results.put(user.getId(), ResetPasswordLinkInfo.KO_NOT_ACTIVE);
            else if (StringUtils.isBlank(user.getEmail())) {
                results.put(user.getId(), ResetPasswordLinkInfo.KO_MISSING_EMAIL);
            } else {
                EmailTemplate template = getResetPasswordTemplate(user, templateEngine,
                        resetPasswordSubjectTemplate, resetPasswordBodyTemplate,
                        subjectDefaultTemplate, bodyDefaultTemplate,
                        localizedSubjectTemplates, localizedBodyTemplates);
                // send email
                String link = generateResetLink(baseUrl, entry.getValue());
                sendResetPasswordEmail(user, link, template.subjectTemplate, template.bodyTemplate);
                results.put(user.getId(), ResetPasswordLinkInfo.OK);
            }
        }

        for (UUID id : userIds) {
            results.putIfAbsent(id, ResetPasswordLinkInfo.KO_NOT_FOUND);
        }

        return results;
    }

    protected String generateResetLink(String baseUrl, String token) {
        return baseUrl.contains("?") ? baseUrl + "&token=" + token : baseUrl + "?token=" + token;
    }

    protected void sendResetPasswordEmail(User user, String link, Template subjectTemplate, Template bodyTemplate) {
        Transaction tx = persistence.getTransaction();
        String emailBody;
        String emailSubject;
        try {
            Map<String, Object> binding = new HashMap<>();
            binding.put("user", user);
            binding.put("link", link);
            binding.put("lifetimeHours", forgotPasswordConfig.getResetPasswordTokenLifetimeMinutes() / 60);
            binding.put("persistence", persistence);

            emailBody = bodyTemplate.make(binding).writeTo(new StringWriter(0)).toString();
            emailSubject = subjectTemplate.make(binding).writeTo(new StringWriter(0)).toString();

            tx.commit();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write Groovy template content", e);
        } finally {
            tx.end();
        }

        EmailInfo emailInfo = new EmailInfo(user.getEmail(), emailSubject, emailBody);
        emailerAPI.sendEmailAsync(emailInfo);
    }

    protected EmailTemplate getResetPasswordTemplate(User user,
                                                     SimpleTemplateEngine templateEngine,
                                                     String resetPasswordSubjectTemplate,
                                                     String resetPasswordBodyTemplate,
                                                     Template subjectDefaultTemplate,
                                                     Template bodyDefaultTemplate,
                                                     Map<String, Template> localizedSubjectTemplates,
                                                     Map<String, Template> localizedBodyTemplates) {

        boolean userLocaleIsUnknown = StringUtils.isEmpty(user.getLanguage());
        String locale = userLocaleIsUnknown ?
                messageTools.getDefaultLocale().getLanguage() : user.getLanguage();

        Template bodyTemplate;
        if (userLocaleIsUnknown) {
            bodyTemplate = bodyDefaultTemplate;
        } else {
            if (localizedBodyTemplates.containsKey(locale))
                bodyTemplate = localizedBodyTemplates.get(locale);
            else {
                String templateString = getLocalizedTemplateContent(resetPasswordBodyTemplate, locale);
                if (templateString == null) {
                    log.warn("Reset password links: Not found email body template for locale: '{}'", locale);
                    bodyTemplate = bodyDefaultTemplate;
                } else {
                    bodyTemplate = getTemplate(templateEngine, templateString);
                }
                localizedBodyTemplates.put(locale, bodyTemplate);
            }
        }

        Template subjectTemplate;
        if (userLocaleIsUnknown) {
            subjectTemplate = subjectDefaultTemplate;
        } else {
            if (localizedSubjectTemplates.containsKey(locale))
                subjectTemplate = localizedSubjectTemplates.get(locale);
            else {
                String templateString = getLocalizedTemplateContent(resetPasswordSubjectTemplate, locale);
                if (templateString == null) {
                    log.warn("Reset password links: Not found email subject template for locale '{}'", locale);
                    subjectTemplate = subjectDefaultTemplate;
                } else {
                    subjectTemplate = getTemplate(templateEngine, templateString);
                }
                localizedSubjectTemplates.put(locale, subjectTemplate);
            }
        }

        return new EmailTemplate(subjectTemplate, bodyTemplate);
    }

    private String getLocalizedTemplateContent(String defaultTemplateName, String locale) {
        String localizedTemplate = FilenameUtils.getFullPath(defaultTemplateName)
                + FilenameUtils.getBaseName(defaultTemplateName) +
                "_" + locale +
                "." + FilenameUtils.getExtension(defaultTemplateName);

        return resources.getResourceAsString(localizedTemplate);
    }

    protected Template getTemplate(SimpleTemplateEngine templateEngine, String templateString) {
        Template bodyTemplate;
        try {
            bodyTemplate = templateEngine.createTemplate(templateString);
        } catch (Exception e) {
            throw new RuntimeException("Unable to compile Groovy template", e);
        }
        return bodyTemplate;
    }

    protected Template loadDefaultTemplate(String templatePath, SimpleTemplateEngine templateEngine) {
        String defaultTemplateContent = resources.getResourceAsString(templatePath);
        if (defaultTemplateContent == null) {
            throw new IllegalStateException("Unable to find default template for reset password link email");
        }

        //noinspection UnnecessaryLocalVariable
        Template template = getTemplate(templateEngine, defaultTemplateContent);
        return template;
    }

    protected Map<User, String> generateResetPasswordTokens(Set<UUID> userIds, boolean includeInactive) {
        Map<User, String> tokens = new LinkedHashMap<>(userIds.size());

        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from sec$User u where u.id in :userIds", User.class);
            query.setParameter("userIds", userIds);

            List<User> users = query.getResultList();

            if (users != null) {
                for (User user : users) {
                    if (user.getActive() || includeInactive)
                        tokens.put(user, generateResetPasswordToken(user.getId()));
                    else
                        tokens.put(user, null);
                }
            }
            tx.commit();
        }

        return tokens;
    }

    protected String generateResetPasswordToken(UUID userId) {
        String token = RandomStringUtils.randomAlphanumeric(ResetPasswordToken.TOKEN_LENGTH);
        int lifetime = forgotPasswordConfig.getResetPasswordTokenLifetimeMinutes();

        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<ResetPasswordToken> query = em.createQuery(
                    "select rp from nxsecfp$ResetPasswordToken rp where rp.user.id = :userId",
                    ResetPasswordToken.class);
            query.setParameter("userId", userId);

            ResetPasswordToken resetPasswordToken = query.getFirstResult();

            if (resetPasswordToken == null) {
                resetPasswordToken = metadata.create(ResetPasswordToken.class);
                resetPasswordToken.setUser(em.getReference(User.class, userId));
            }
            Date expires = DateUtils.addMinutes(timeSource.currentTimestamp(), lifetime);
            resetPasswordToken.setToken(token);
            resetPasswordToken.setExpireAt(expires);

            em.persist(resetPasswordToken);

            tx.commit();
        }

        return token;
    }

    /**
     * Template pair : subject + body
     */
    protected static class EmailTemplate {

        private Template subjectTemplate;
        private Template bodyTemplate;

        private EmailTemplate(Template subjectTemplate, Template bodyTemplate) {
            this.subjectTemplate = subjectTemplate;
            this.bodyTemplate = bodyTemplate;
        }

        public Template getSubjectTemplate() {
            return subjectTemplate;
        }

        public Template getBodyTemplate() {
            return bodyTemplate;
        }
    }
}

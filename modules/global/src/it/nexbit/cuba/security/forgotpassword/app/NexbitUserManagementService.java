/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.app;

import com.haulmont.cuba.security.entity.User;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import it.nexbit.cuba.security.forgotpassword.entity.ResetPasswordLinkInfo;
import it.nexbit.cuba.security.forgotpassword.entity.ResetPasswordToken;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Provides additional methods for User management operations, like checking for user
 * existence, or generating and sending reset password links via email.
 */
public interface NexbitUserManagementService {
    String NAME = "extsec_UserManagementService";

    /**
     * Find a {@code User} by its login or email.
     *
     * @param loginOrEmail  the user's login name or email
     * @return a {@code User} entity if found, {@code null} otherwise
     */
    @Validated
    User findUser(@NotNull String loginOrEmail);

    /**
     * Check if a user with the specified login or email exist.
     *
     * @param loginOrEmail  the user's login name or email
     * @param onlyActive    {@code true} if only active users must be searched for
     * @return  {@code true} if a user is found, {@code false} otherwise
     */
    @Validated
    boolean checkUserExist(@NotNull String loginOrEmail, boolean onlyActive);

    /**
     * Check if an active user with the specified login or email exist.
     *
     * @param loginOrEmail  the user's login name or email
     * @return  {@code true} if a user is found, {@code false} otherwise
     */
    @Validated
    default boolean checkUserExist(@NotNull String loginOrEmail) {
        return checkUserExist(loginOrEmail, true);
    }

    /**
     * Send reset password emails to the specified user ids.
     * <p>
     *     The associated token will be valid for
     *     {@link ForgotPasswordConfig#getResetPasswordTokenLifetimeMinutes()}, and after expiration
     *     the target page should report an error, asking the user to request a new link.
     * </p>
     *
     * @param userIds  a list of user ids to send the emails to
     * @param baseUrl  the base URL to use when composing the link to be sent. A query string
     *                 in the form <i>t=TOKEN</i> will be appended at the end of the url
     * @param includeInactive  {@code true} to send reset email even to inactive users,
     *                         {@code false} otherwise.
     * @return  a {@code Map} that associate each passed UUID with the corresponding result,
     *          in the form of an {@link ResetPasswordLinkInfo} enum value
     */
    @Validated
    @NotNull
    Map<UUID, ResetPasswordLinkInfo> sendResetPasswordLinks(@NotNull List<UUID> userIds,
                                                            @NotNull String baseUrl,
                                                            boolean includeInactive);

    /**
     * Send a reset password link to the specified user by email.  The base URL for the link
     * will be the one returned by {@link ForgotPasswordConfig#getResetPasswordLinkPortalUrl()}.  A
     * query string in the form <i>t=TOKEN</i> will be appended at the end of the url.
     * <p>
     *     The associated token will be valid for
     *     {@link ForgotPasswordConfig#getResetPasswordTokenLifetimeMinutes()}, and after expiration
     *     the target page should report an error, asking the user to request a new link.
     * </p>
     *
     * <h3>Preconditions:</h3>
     * <ul>
     *     <li>the user must exist</li>
     *     <li>the user must have a valid email set</li>
     *     <li>the user must be active</li>
     * </ul>
     *
     * @param loginOrEmail  the user's login name or email
     * @return an enum indicating the result of the operation
     */
    @Validated
    @NotNull
    ResetPasswordLinkInfo sendResetPasswordLink(@NotNull String loginOrEmail);

    /**
     * Send a reset password link to the specified user by email.
     * <p>
     *     The associated token will be valid for
     *     {@link ForgotPasswordConfig#getResetPasswordTokenLifetimeMinutes()}, and after expiration
     *     the target page should report an error, asking the user to request a new link.
     * </p>
     *
     * <h3>Preconditions:</h3>
     * <ul>
     *     <li>the user must exist</li>
     *     <li>the user must have a valid email set</li>
     *     <li>the user must be active</li>
     * </ul>
     *
     * @param loginOrEmail  the user's login name or email
     * @param baseUrl       the base URL to use when composing the link to be sent. A query string
     *                      in the form <i>t=TOKEN</i> will be appended at the end of the url
     * @return an enum indicating the result of the operation
     */
    @Validated
    @NotNull
    ResetPasswordLinkInfo sendResetPasswordLink(@NotNull String loginOrEmail, @NotNull String baseUrl);

    /**
     * Get the {@code User} entity associated with the passed {@code token}, if it's valid.
     * If the token is expired, it will be removed from db.
     *
     * @param token  a token stored in {@link ResetPasswordToken}
     * @return  a {@code User} entity if token was valid, {@code null} otherwise
     */
    @Validated
    User getUserForResetPasswordTokenIfValid(@NotNull String token);

    /**
     * Check if the {@code token} is valid (found and not expired), and returns a POJO
     * describing the result.
     *
     * @param token  a token stored in {@link ResetPasswordToken}
     * @return  a {@code CheckTokenResponse} instance with {@code isValid} property set to
     *          {@code true} if token was valid, and {@code user} property set to an instance
     *          of {@link CheckTokenResponse.UserInfo} containing some key properties of
     *          token's related {@code User} entity. Otherwise, if token was not valid,
     *          {@code isValid} property will be set to {@code false}
     */
    @Validated
    @NotNull
    CheckTokenResponse checkResetPasswordToken(@NotNull String token);

    /**
     * Delete the passed {@code token} if found.
     *
     * @param token  the token to delete from {@link ResetPasswordToken}
     *               entities
     */
    @Validated
    void deleteResetPasswordToken(@NotNull String token);

    /**
     * Change a {@code User}'s password using the specified reset password {@code token}.
     * The corresponding {@link ResetPasswordToken} entity will
     * be deleted after successful password change.
     *
     * @param token     a reset password token
     * @param password  the new password to set for the {@code User} associated with the found
     *                  {@link ResetPasswordToken}
     * @return  {@code true} if the token was valid and password has been successfully changed,
     *          {@code false} otherwise
     */
    @Validated
    boolean changePasswordWithToken(@NotNull String token, @NotNull String password);

    class CheckTokenResponse implements Serializable {
        public UserInfo user;
        public boolean isValid;

        public CheckTokenResponse(User user, boolean isValid) {
            this.user = user != null ? new UserInfo(user) : null;
            this.isValid = isValid;
        }

        class UserInfo implements Serializable {
            public String login;
            public String name;
            public String timeZone;
            public String language;

            public UserInfo(User user) {
                this.login = user.getLogin();
                this.name = user.getName();
                this.timeZone = user.getTimeZone();
                this.language = user.getLanguage();
            }
        }
    }
}

/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.web.loginwindow;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.HBoxLayout;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.app.loginwindow.AppLoginWindow;
import it.nexbit.cuba.security.forgotpassword.app.NexbitUserManagementService;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import it.nexbit.cuba.security.forgotpassword.web.resetpassword.ResetPassword;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public class NexbitAppLoginWindow extends AppLoginWindow {

    @Inject
    protected NexbitUserManagementService nexbitUserManagementService;

    @Inject
    protected ForgotPasswordConfig forgotPasswordConfig;

    @Inject
    protected HBoxLayout resetControls;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        if (forgotPasswordConfig.getShowResetPasswordLinkAtLogin()) {
            resetControls.setVisible(true);
        }
    }

    public void onResetPasswordBtnClick() {
        ResetPassword resetPasswordScreen = (ResetPassword) openWindow("resetPassword",
                WindowManager.OpenType.DIALOG);
        // Add a listener to be notified when the "Restore password" screen is closed with COMMIT_ACTION_ID
        resetPasswordScreen.addCloseWithCommitListener(() -> {
            loginField.setValue(resetPasswordScreen.getLogin());
            // clear password field
            passwordField.setValue(null);
            // Set focus in password field
            passwordField.requestFocus();
        });
    }

    public void showChangePasswordDialog(String token) {
        if (StringUtils.isNotEmpty(token)) {
            // validate the token, and load associated User entity
            final User user = nexbitUserManagementService.getUserForResetPasswordTokenIfValid(token);

            if (user != null) {
                // show change password dialog
                Window changePasswordDialog = openWindow("sec$User.changePassword",
                        WindowManager.OpenType.DIALOG,
                        ParamsMap.of("currentPasswordRequired", false,
                                "cancelEnabled", true,
                                "user", user)
                );
                changePasswordDialog.addCloseListener(actionId -> {
                    if (Objects.equals(actionId, COMMIT_ACTION_ID)) {
                        loginField.setValue(user.getLoginLowerCase());
                        // clear password field
                        passwordField.setValue(null);
                        // Set focus in password field
                        passwordField.requestFocus();

                        nexbitUserManagementService.deleteResetPasswordToken(token);
                    }
                });
            } else {
                showNotification(messages.getMessage(NexbitAppLoginWindow.class, "resetPassword.invalidToken"),
                        NotificationType.ERROR);
            }
        }
    }
}

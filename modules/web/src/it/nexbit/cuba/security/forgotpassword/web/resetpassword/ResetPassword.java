/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.web.resetpassword;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.security.entity.User;
import it.nexbit.cuba.security.forgotpassword.app.NexbitUserManagementService;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Collections;

public class ResetPassword extends AbstractWindow {
    @Inject
    protected TextField loginField;
    @Inject
    protected Label warningLabel;
    @Inject
    protected NexbitUserManagementService nexbitUserManagementService;
    @Inject
    protected ForgotPasswordConfig forgotPasswordConfig;
    @Inject
    protected GlobalConfig globalConfig;

    public void onSubmit() {
        if (validateAll()) {
            User targetUser = nexbitUserManagementService.findUser(loginField.getValue());
            if (targetUser == null) {
                warningLabel.setValue(messages.getMessage(ResetPassword.class,
                        "resetPassword.loginNotFound"));
                showWarning();
            } else if (!targetUser.getActive()) {
                warningLabel.setValue(messages.getMessage(ResetPassword.class,
                        "resetPassword.userDisabled"));
                showWarning();
            } else if (StringUtils.isBlank(targetUser.getEmail())) {
                warningLabel.setValue(messages.getMessage(ResetPassword.class,
                        "resetPassword.noEmailSet"));
                showWarning();
            } else {
                nexbitUserManagementService.sendResetPasswordLinks(Collections.singletonList(targetUser.getId()),
                        globalConfig.getWebAppUrl() + forgotPasswordConfig.getResetPasswordLinkWebPath(),
                        false);
                showNotification(messages.getMainMessage("success"),
                        messages.getMainMessage("resetPassword.resetMessage"),
                        NotificationType.TRAY);
                close(COMMIT_ACTION_ID);
            }
        }
    }

    protected void showWarning() {
        warningLabel.setVisible(true);
        loginField.requestFocus();
        loginField.selectAll();
    }

    public void onCancel() {
        close(CLOSE_ACTION_ID);
    }

    public String getLogin() {
        return loginField.getValue();
    }
}

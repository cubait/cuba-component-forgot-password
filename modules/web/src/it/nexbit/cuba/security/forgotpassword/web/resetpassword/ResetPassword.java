package it.nexbit.cuba.security.forgotpassword.web.resetpassword;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.Notifications.NotificationType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.entity.User;
import it.nexbit.cuba.security.forgotpassword.app.NexbitUserManagementService;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Collections;

@UiController("resetPassword")
@UiDescriptor("reset-password.xml")
public class ResetPassword extends Screen {
    @Inject
    protected TextField<String> loginField;
    @Inject
    protected Label<String> warningLabel;

    @Inject
    protected NexbitUserManagementService nexbitUserManagementService;
    @Inject
    protected ForgotPasswordConfig forgotPasswordConfig;
    @Inject
    protected GlobalConfig globalConfig;
    @Inject
    protected Messages messages;
    @Inject
    protected Notifications notifications;

    @Subscribe("submit")
    protected void onSubmit(Action.ActionPerformedEvent event) {
        if (validateScreen()) {
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
                notifications.create(NotificationType.TRAY)
                        .withCaption(messages.getMainMessage("success"))
                        .withDescription(messages.getMainMessage("resetPassword.resetMessage"))
                        .show();
                close(WINDOW_COMMIT_AND_CLOSE_ACTION);
            }
        }
    }

    protected void showWarning() {
        warningLabel.setVisible(true);
        loginField.focus();
        loginField.selectAll();
    }

    @Subscribe("cancel")
    protected void onCancel(Action.ActionPerformedEvent event) {
        closeWithDefaultAction();
    }

    /**
     * Validates screen data. Default implementation validates visible and enabled UI components,
     * and show a notification on errors.
     * <br>
     * Can be overridden in subclasses.
     *
     * @return <code>true</code> if no errors, <code>false</code> otherwise
     */
    protected boolean validateScreen() {
        ScreenValidation screenValidation = getBeanLocator().get(ScreenValidation.NAME);
        ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
        screenValidation.showValidationErrors(this, validationErrors);
        return validationErrors.isEmpty();
    }

    public String getLogin() {
        return loginField.getValue();
    }
}
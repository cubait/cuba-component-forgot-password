/*
 * Copyright (c) 2017 Nexbit di Paolo Furini
 */

package it.nexbit.cuba.security.forgotpassword.web.loginwindow;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.LinkButton;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.screen.MapScreenOptions;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.StandardCloseAction;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.app.loginwindow.AppLoginWindow;
import com.vaadin.server.Page;
import it.nexbit.cuba.security.forgotpassword.app.NexbitUserManagementService;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import it.nexbit.cuba.security.forgotpassword.web.resetpassword.ResetPassword;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Map;

public class NexbitAppLoginWindow extends AppLoginWindow implements LoginScreen {

    @Inject
    protected NexbitUserManagementService nexbitUserManagementService;

    @Inject
    protected ForgotPasswordConfig forgotPasswordConfig;

    @Inject
    protected ScreenBuilders screenBuilders;

    @Inject
    protected Label<String> resetPasswordSpacer;
    @Inject
    protected LinkButton resetPasswordButton;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        Page.getCurrent().getStyles().add(".nx-reset-button{padding-left:0 !important;}");

        if (forgotPasswordConfig.getShowResetPasswordLinkAtLogin()) {
            resetPasswordSpacer.setVisible(true);
            resetPasswordButton.setVisible(true);
        }
    }

    public void onResetPasswordBtnClick() {
        screenBuilders.screen(this)
                .withScreenClass(ResetPassword.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.getCloseAction() == WINDOW_COMMIT_AND_CLOSE_ACTION) {
                        loginField.setValue(closeEvent.getScreen().getLogin());
                        // clear password field
                        passwordField.setValue(null);
                        // Set focus in password field
                        passwordField.focus();
                    }
                }).build().show();
    }

    public void showChangePasswordDialog(String token) {
        if (StringUtils.isNotEmpty(token)) {
            // validate the token, and load associated User entity
            final User user = nexbitUserManagementService.getUserForResetPasswordTokenIfValid(token);

            if (user != null) {
                // show change password dialog
                Screen changePasswordDialog = screenBuilders.screen(this)
                        .withScreenId("sec$User.changePassword")
                        .withOpenMode(OpenMode.DIALOG)
                        .withOptions(new MapScreenOptions(ParamsMap.of(
                                "currentPasswordRequired", false,
                                "cancelEnabled", true,
                                "user", user)))
                        .show();
                changePasswordDialog.addAfterCloseListener(closeEvent -> {
                    if (Window.COMMIT_ACTION_ID.equals(((StandardCloseAction)closeEvent.getCloseAction()).getActionId())) {
                        loginField.setValue(user.getLoginLowerCase());
                        // clear password field
                        passwordField.setValue(null);
                        // Set focus in password field
                        passwordField.focus();

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

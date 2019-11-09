package it.nexbit.cuba.security.forgotpassword.web.loginwindow;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.Notifications.NotificationType;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.LinkButton;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.vaadin.server.Page;
import it.nexbit.cuba.security.forgotpassword.app.NexbitUserManagementService;
import it.nexbit.cuba.security.forgotpassword.config.ForgotPasswordConfig;
import it.nexbit.cuba.security.forgotpassword.web.resetpassword.ResetPassword;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

@UiController("login")
@UiDescriptor("nexbit-login-screen.xml")
public class NexbitLoginScreen extends LoginScreen implements it.nexbit.cuba.security.forgotpassword.web.loginwindow.LoginScreen {

    @Inject
    protected NexbitUserManagementService nexbitUserManagementService;

    @Inject
    protected ForgotPasswordConfig forgotPasswordConfig;

    @Inject
    protected ScreenBuilders screenBuilders;

    @Inject
    protected Notifications notifications;

    @Inject
    protected LinkButton resetPasswordButton;
    @Inject
    protected Label<String> resetPasswordSpacer;

    @Override
    protected void onInit(InitEvent event) {
        super.onInit(event);

        Page.getCurrent().getStyles().add(".nx-reset-button{padding-left:0 !important;}");

        if (forgotPasswordConfig.getShowResetPasswordLinkAtLogin()) {
            resetPasswordSpacer.setVisible(true);
            resetPasswordButton.setVisible(true);
        }
    }

    @Subscribe("resetPasswordButton")
    protected void onResetPasswordButtonClick(Button.ClickEvent event) {
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
                notifications.create(NotificationType.ERROR)
                        .withCaption(messages.getMessage(NexbitAppLoginWindow.class, "resetPassword.invalidToken"))
                        .show();
            }
        }
    }
}
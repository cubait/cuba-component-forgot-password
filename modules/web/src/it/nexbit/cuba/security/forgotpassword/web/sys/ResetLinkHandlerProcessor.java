package it.nexbit.cuba.security.forgotpassword.web.sys;

import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.sys.linkhandling.ExternalLinkContext;
import com.haulmont.cuba.web.sys.linkhandling.LinkHandlerProcessor;
import it.nexbit.cuba.security.forgotpassword.web.loginwindow.LoginScreen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component(ResetLinkHandlerProcessor.NAME)
@Order(LinkHandlerProcessor.HIGHEST_PLATFORM_PRECEDENCE - 1)
public class ResetLinkHandlerProcessor implements LinkHandlerProcessor {
    public static final String NAME = "extsec_ResetLinkHandlerProcessor";

    public static final String RESET_ACTION = "reset";

    private static final Logger log = LoggerFactory.getLogger(ResetLinkHandlerProcessor.class);

    /**
     * @param linkContext
     * @return true if action with such request parameters should be handled by this processor.
     */
    @Override
    public boolean canHandle(ExternalLinkContext linkContext) {
        return linkContext.getAction().equals(RESET_ACTION);
    }

    /**
     * Called to handle action.
     *
     * @param linkContext
     */
    @Override
    public void handle(ExternalLinkContext linkContext) {
        try {
            String token = linkContext.getRequestParams().get("token");
            if (!StringUtils.isEmpty(token)) {
                Connection connection = linkContext.getApp().getConnection();
                if (!connection.isAuthenticated()) {
                    log.info("reset link token found: {}", token);

                    LoginScreen loginWindow = (LoginScreen) AppUI.getCurrent().getTopLevelWindowNN().getFrameOwner();
                    if (loginWindow != null) {
                        loginWindow.showChangePasswordDialog(token);
                    }
                } else {
                    log.debug("reset link token found but ignored because already authenticated");
                }
            } else {
                log.warn("no token found for reset link action");
            }
        } catch (Exception e) {
            log.error("unknown error in ResetLinkHandlerProcessor#handle()", e);
            throw e;
        }
    }
}

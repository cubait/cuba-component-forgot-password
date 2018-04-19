package it.nexbit.cuba.security.forgotpassword.web.sys;

import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.sys.LinkHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import it.nexbit.cuba.security.forgotpassword.web.loginwindow.NexbitAppLoginWindow;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NexbitLinkHandler extends LinkHandler {
    public static final String RESET_ACTION = "reset";

    private final Logger log = LoggerFactory.getLogger(NexbitLinkHandler.class);

    public NexbitLinkHandler(App app, String action, Map<String, String> requestParams) {
        super(app, action, requestParams);
    }

    @Override
    public void handle() {
        if (action.equals(RESET_ACTION)) {
            try {
                String token = requestParams.get("token");
                if (!StringUtils.isEmpty(token)) {
                    Connection connection = app.getConnection();
                    if (!connection.isAuthenticated()) {
                        log.info("reset link token found: {}", token);

                        NexbitAppLoginWindow loginWindow = (NexbitAppLoginWindow) app.getTopLevelWindow();
                        if (loginWindow != null) {
                            loginWindow.showChangePasswordDialog(token);
                        }
                    } else {
                        log.debug("reset link token found but ignored because already authenticated");
                    }
                } else {
                    log.warn("no token found for reset link action");
                }
            } finally {
                VaadinRequest request = VaadinService.getCurrentRequest();
                WrappedSession wrappedSession = request.getWrappedSession();
                wrappedSession.removeAttribute(AppUI.LAST_REQUEST_PARAMS_ATTR);
                wrappedSession.removeAttribute(AppUI.LAST_REQUEST_ACTION_ATTR);
            }
        } else {
            super.handle();
        }
    }

    public boolean canHandleLink(String action, Map<String, String> params) {
        return action.equals(RESET_ACTION);
    }
}

package it.nexbit.cuba.security.forgotpassword.web;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.sys.LinkHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WrappedSession;
import it.nexbit.cuba.security.forgotpassword.web.sys.NexbitLinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class NexbitAppUI extends AppUI {
    private static final Logger log = LoggerFactory.getLogger(NexbitAppUI.class);

    @Override
    public void processExternalLink(VaadinRequest request) {
        WrappedSession wrappedSession = request.getWrappedSession();
        String action = (String) wrappedSession.getAttribute(LAST_REQUEST_ACTION_ATTR);
        if (NexbitLinkHandler.RESET_ACTION.equals(action)) {
            //noinspection unchecked
            Map<String, String> params =
                    (Map<String, String>) wrappedSession.getAttribute(LAST_REQUEST_PARAMS_ATTR);
            params = params != null ? params : Collections.emptyMap();

            try {
                LinkHandler linkHandler = AppBeans.getPrototype(LinkHandler.NAME, app, action, params);
                if (((NexbitLinkHandler)linkHandler).canHandleLink(action, params)) {
                    linkHandler.handle();
                    wrappedSession.setAttribute(LAST_REQUEST_ACTION_ATTR, null);
                    return;
                }
            } catch (Exception e) {
                error(new com.vaadin.server.ErrorEvent(e));
            }
        }

        super.processExternalLink(request);
    }
}

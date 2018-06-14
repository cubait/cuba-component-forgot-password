package it.nexbit.cuba.security.forgotpassword.web.sys;

import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.sys.LinkHandler;

import java.util.Map;

public class NexbitLinkHandler extends LinkHandler {
    public NexbitLinkHandler(App app, String action, Map<String, String> requestParams) {
        super(app, action, requestParams);
    }

    @Override
    public boolean canHandleLink() {
        return super.canHandleLink() || action.equals(ResetLinkHandlerProcessor.RESET_ACTION);
    }
}

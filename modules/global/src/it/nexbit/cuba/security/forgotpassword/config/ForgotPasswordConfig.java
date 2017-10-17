package it.nexbit.cuba.security.forgotpassword.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;

public interface ForgotPasswordConfig extends Config {
    @Property("ext.security.resetPasswordTokenLifetimeMinutes")
    @DefaultInt(1440)  // 24 hours
    @Source(type = SourceType.DATABASE)
    int getResetPasswordTokenLifetimeMinutes();

    @Property("ext.security.showResetPasswordLinkAtLogin")
    @DefaultBoolean(false)
    @Source(type = SourceType.APP)
    boolean getShowResetPasswordLinkAtLogin();

    @Property("ext.security.resetPasswordLinkWebPath")
    @Default("/reset")
    @Source(type = SourceType.DATABASE)
    String getResetPasswordLinkWebPath();

    @Property("ext.security.resetPasswordLinkPortalUrl")
    @Default("http://localhost:8080/portal/reset")
    @Source(type = SourceType.DATABASE)
    String getResetPasswordLinkPortalUrl();

    @Property("ext.security.resetPasswordLinkTemplateBody")
    @Default("/it/nexbit/cuba/security/forgotpassword/app/email/reset-password-link-body.gsp")
    @Source(type = SourceType.APP)
    String getResetPasswordLinkEmailBodyTemplate();

    @Property("ext.security.resetPasswordLinkTemplateSubject")
    @Default("/it/nexbit/cuba/security/forgotpassword/app/email/reset-password-link-subject.gsp")
    @Source(type = SourceType.APP)
    String getResetPasswordLinkEmailSubjectTemplate();
}

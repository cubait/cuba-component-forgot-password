package it.nexbit.cuba.restapi.auth;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.TrustedClientService;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.restapi.auth.CubaAnonymousAuthenticationToken;
import com.haulmont.restapi.config.RestApiConfig;
import it.nexbit.cuba.restapi.RestServicesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

/**
 * This filter is used for anonymous access to CUBA REST API. If no Authorization header is present in the request and
 * if {@link GlobalConfig#getRestAnonymousEnabled()} is true, then the anonymous user session will be set to the
 * {@link SecurityContext} and the request will be authenticated.
 * Service methods can be allowed anonymous access (even when the {@link GlobalConfig#getRestAnonymousEnabled()} is false)
 * by setting the {@code allowAnonymous} attribute to {@code true} in the {@code cuba.rest.servicesConfig} configuration file.
 * This way there's no need to globally enable anonymous access only when a few business logic is needed
 * for anonymous users.
 *
 * This filter must be invoked after the
 * {@link org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter}
 */
public class NexbitAnonymousAuthenticationFilter implements Filter {

    private static final String REST_BASE_PATH = "/rest/v2/";
    private static final int REST_BASE_PATH_LEN = REST_BASE_PATH.length();

    private static final Logger log = LoggerFactory.getLogger(NexbitAnonymousAuthenticationFilter.class);

    @Inject
    protected RestApiConfig restApiConfig;

    @Inject
    protected TrustedClientService trustedClientService;

    @Inject
    protected RestServicesConfiguration restServicesConfiguration;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean isAnonymous = restApiConfig.getRestAnonymousEnabled();
        if (!isAnonymous) {
            // if global anonymous flag is not set, check if this is a service method invocation,
            // which have the allowAnonymous attribute set to true
            HttpServletRequest req = (HttpServletRequest) request;
            String path = req.getRequestURI().substring(req.getContextPath().length() + REST_BASE_PATH_LEN);
            String[] parts = path.split("/");
            if (parts.length > 2 && parts[0].equals("services")) {
                String service = parts[1];
                String method = parts[2];
                RestServicesConfiguration.RestMethodInfo methodInfo = (RestServicesConfiguration.RestMethodInfo)
                        restServicesConfiguration.getRestMethodInfo(service, method, Collections.list(req.getParameterNames()));
                if (methodInfo != null) {
                    isAnonymous = methodInfo.isAnonymous();
                    log.debug("Anonymous access for {} request determined by allowAnonymous attribute (found '{}')",
                            path, isAnonymous);
                }
            }
        }
        if (isAnonymous) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserSession anonymousSession;
                try {
                    anonymousSession = trustedClientService.getAnonymousSession(restApiConfig.getTrustedClientPassword());
                } catch (LoginException e) {
                    throw new RuntimeException("Unable to obtain anonymous session for REST", e);
                }

                CubaAnonymousAuthenticationToken anonymousAuthenticationToken =
                        new CubaAnonymousAuthenticationToken("anonymous", AuthorityUtils.createAuthorityList("ROLE_CUBA_ANONYMOUS"));
                SecurityContextHolder.getContext().setAuthentication(anonymousAuthenticationToken);
                AppContext.setSecurityContext(new SecurityContext(anonymousSession));
            } else {
                log.debug("SecurityContextHolder not populated with cuba anonymous token, as it already contained: '{}'",
                        SecurityContextHolder.getContext().getAuthentication());
            }
        } else {
            log.trace("Anonymous access for CUBA REST API is disabled");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}

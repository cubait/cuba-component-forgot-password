package it.nexbit.cuba.restapi;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.global.AppBeans;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestServicesConfiguration extends com.haulmont.restapi.config.RestServicesConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RestServicesConfiguration.class);

    @Override
    protected void loadConfig(Element rootElem) {
        for (Element serviceElem : Dom4j.elements(rootElem, "service")) {
            String serviceName = serviceElem.attributeValue("name");
            if (!AppBeans.containsBean(serviceName)) {
                log.error("Service not found: {}", serviceName);
                continue;
            }
            Object service = AppBeans.get(serviceName);
            List<RestMethodInfo> methodInfos = new ArrayList<>();

            for (Element methodElem : Dom4j.elements(serviceElem, "method")) {
                String methodName = methodElem.attributeValue("name");
                // allowAnonymous has a default of "false"
                boolean anonymous = Boolean.parseBoolean(methodElem.attributeValue("allowAnonymous", "false"));
                List<RestMethodParamInfo> params = new ArrayList<>();
                for (Element paramEl : Dom4j.elements(methodElem, "param")) {
                    params.add(new RestMethodParamInfo(paramEl.attributeValue("name"), paramEl.attributeValue("type")));
                }
                Method method = _findMethod(serviceName, methodName, params);
                if (method != null) {
                    methodInfos.add(new RestMethodInfo(methodName, params, method, anonymous));
                }
            }

            serviceInfosMap.put(serviceName, new RestServiceInfo(serviceName, Collections.unmodifiableList(methodInfos)));
        }
    }

    public static class RestMethodInfo extends com.haulmont.restapi.config.RestServicesConfiguration.RestMethodInfo {
        protected boolean anonymous;

        public RestMethodInfo(String name, List<RestMethodParamInfo> params, Method method) {
            super(name, params, method);
            this.anonymous = false;
        }
        public RestMethodInfo(String name, List<RestMethodParamInfo> params, Method method, boolean anonymous) {
            super(name, params, method);
            this.anonymous = anonymous;
        }

        public boolean isAnonymous() {
            return anonymous;
        }

        public void setAnonymous(boolean anonymous) {
            this.anonymous = anonymous;
        }
    }

}

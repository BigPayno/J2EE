package shiro.spi;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author payno
 * @date 2020/4/10 17:41
 * @description
 *      只有鉴别的用户才允许访问
 */
public abstract class AuthenticationFilter extends AccessControlFilter{
    public static final String DEFAULT_SUCCESS_URL = "/";
    private String successUrl = "/";

    public AuthenticationFilter() {
    }

    public String getSuccessUrl() {
        return this.successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = this.getSubject(request, response);
        return subject.isAuthenticated();
    }

    protected void issueSuccessRedirect(ServletRequest request, ServletResponse response) throws Exception {
        WebUtils.redirectToSavedRequest(request, response, this.getSuccessUrl());
    }
}

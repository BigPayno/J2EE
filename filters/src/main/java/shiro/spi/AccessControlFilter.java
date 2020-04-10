package shiro.spi;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author payno
 * @date 2020/4/10 17:28
 * @description
 *      进入登陆界面以及访问允许和禁止情况
 */
public abstract class AccessControlFilter extends PathMatchingFilter{
    public static final String DEFAULT_LOGIN_URL = "/login.jsp";
    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    private String loginUrl = "/login.jsp";

    public AccessControlFilter() {
    }

    public String getLoginUrl() {
        return this.loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    protected Subject getSubject(ServletRequest request, ServletResponse response) {
        return SecurityUtils.getSubject();
    }

    protected abstract boolean isAccessAllowed(ServletRequest var1, ServletResponse var2, Object var3) throws Exception;

    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return this.onAccessDenied(request, response);
    }

    protected abstract boolean onAccessDenied(ServletRequest var1, ServletResponse var2) throws Exception;

    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        /**
         *  只有不允许访问且拒绝才会不执行过滤器
         */
        return this.isAccessAllowed(request, response, mappedValue) || this.onAccessDenied(request, response, mappedValue);
    }

    protected boolean isLoginRequest(ServletRequest request, ServletResponse response) {
        return this.pathsMatch(this.getLoginUrl(), request);
    }

    protected void saveRequestAndRedirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        this.saveRequest(request);
        this.redirectToLogin(request, response);
    }

    protected void saveRequest(ServletRequest request) {
        WebUtils.saveRequest(request);
    }

    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        String loginUrl = this.getLoginUrl();
        WebUtils.issueRedirect(request, response, loginUrl);
    }
}

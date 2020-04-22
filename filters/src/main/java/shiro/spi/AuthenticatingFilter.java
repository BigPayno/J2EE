package shiro.spi;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.subject.Subject;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author payno
 * @date 2020/4/10 17:43
 * @description
 */
public abstract class AuthenticatingFilter extends AuthenticationFilter{
    public static final String PERMISSIVE = "permissive";

    public AuthenticatingFilter() {
    }

    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        //根据请求创建Token
        AuthenticationToken token = this.createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        } else {
            try {
                Subject subject = this.getSubject(request, response);
                //尝试登陆，并尝试从session中获得相关信息
                subject.login(token);
                //登陆成功时事件
                return this.onLoginSuccess(token, subject, request, response);
            } catch (AuthenticationException var5) {
                //登陆失败时事件
                return this.onLoginFailure(token, var5, request, response);
            }
        }
    }

    protected abstract AuthenticationToken createToken(ServletRequest var1, ServletResponse var2) throws Exception;

    protected AuthenticationToken createToken(String username, String password, ServletRequest request, ServletResponse response) {
        boolean rememberMe = this.isRememberMe(request);
        String host = this.getHost(request);
        return this.createToken(username, password, rememberMe, host);
    }

    protected AuthenticationToken createToken(String username, String password, boolean rememberMe, String host) {
        return new UsernamePasswordToken(username, password, rememberMe, host);
    }

    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        return true;
    }

    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        return false;
    }

    protected String getHost(ServletRequest request) {
        return request.getRemoteHost();
    }

    protected boolean isRememberMe(ServletRequest request) {
        return false;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return super.isAccessAllowed(request, response, mappedValue) || !this.isLoginRequest(request, response) && this.isPermissive(mappedValue);
    }

    protected boolean isPermissive(Object mappedValue) {
        if (mappedValue != null) {
            String[] values = (String[])((String[])mappedValue);
            return Arrays.binarySearch(values, "permissive") >= 0;
        } else {
            return false;
        }
    }

    @Override
    protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) throws ServletException, IOException {
        if (existing instanceof UnauthenticatedException || existing instanceof ServletException && existing.getCause() instanceof UnauthenticatedException) {
            try {
                this.onAccessDenied(request, response);
                existing = null;
            } catch (Exception var5) {
                existing = var5;
            }
        }

        super.cleanup(request, response, existing);
    }
}

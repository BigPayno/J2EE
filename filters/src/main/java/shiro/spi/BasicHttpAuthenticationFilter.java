package shiro.spi;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * @author payno
 * @date 2020/4/10 17:55
 * @description
 */
public class BasicHttpAuthenticationFilter extends AuthenticatingFilter  {
    private static final Logger log = LoggerFactory.getLogger(org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter.class);
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    private String applicationName = "application";
    private String authcScheme = "BASIC";
    private String authzScheme = "BASIC";

    public BasicHttpAuthenticationFilter() {
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAuthzScheme() {
        return this.authzScheme;
    }

    public void setAuthzScheme(String authzScheme) {
        this.authzScheme = authzScheme;
    }

    public String getAuthcScheme() {
        return this.authcScheme;
    }

    public void setAuthcScheme(String authcScheme) {
        this.authcScheme = authcScheme;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String httpMethod = httpRequest.getMethod();
        Set<String> methods = this.httpMethodsFromOptions((String[])((String[])mappedValue));
        boolean authcRequired = methods.size() == 0;
        Iterator var8 = methods.iterator();

        while(var8.hasNext()) {
            String m = (String)var8.next();
            if (httpMethod.toUpperCase(Locale.ENGLISH).equals(m)) {
                authcRequired = true;
                break;
            }
        }

        return authcRequired ? super.isAccessAllowed(request, response, mappedValue) : true;
    }

    private Set<String> httpMethodsFromOptions(String[] options) {
        Set<String> methods = new HashSet();
        if (options != null) {
            String[] var3 = options;
            int var4 = options.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String option = var3[var5];
                if (!option.equalsIgnoreCase("permissive")) {
                    methods.add(option.toUpperCase(Locale.ENGLISH));
                }
            }
        }

        return methods;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = false;
        //尝试登陆
        if (this.isLoginAttempt(request, response)) {
            loggedIn = this.executeLogin(request, response);
        }

        if (!loggedIn) {
            this.sendChallenge(request, response);
        }

        return loggedIn;
    }

    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        String authzHeader = this.getAuthzHeader(request);
        return authzHeader != null && this.isLoginAttempt(authzHeader);
    }

    @Override
    protected final boolean isLoginRequest(ServletRequest request, ServletResponse response) {
        return this.isLoginAttempt(request, response);
    }

    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return httpRequest.getHeader("Authorization");
    }

    protected boolean isLoginAttempt(String authzHeader) {
        String authzScheme = this.getAuthzScheme().toLowerCase(Locale.ENGLISH);
        return authzHeader.toLowerCase(Locale.ENGLISH).startsWith(authzScheme);
    }

    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        log.debug("Authentication required: sending 401 Authentication challenge response.");
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(401);
        String authcHeader = this.getAuthcScheme() + " realm=\"" + this.getApplicationName() + "\"";
        httpResponse.setHeader("WWW-Authenticate", authcHeader);
        return false;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String authorizationHeader = this.getAuthzHeader(request);
        if (authorizationHeader != null && authorizationHeader.length() != 0) {
            log.debug("Attempting to execute login with auth header");
            String[] prinCred = this.getPrincipalsAndCredentials(authorizationHeader, request);
            String username;
            if (prinCred != null && prinCred.length >= 2) {
                username = prinCred[0];
                String password = prinCred[1];
                return this.createToken(username, password, request, response);
            } else {
                username = prinCred != null && prinCred.length != 0 ? prinCred[0] : "";
                return this.createToken(username, "", request, response);
            }
        } else {
            return this.createToken("", "", request, response);
        }
    }

    protected String[] getPrincipalsAndCredentials(String authorizationHeader, ServletRequest request) {
        if (authorizationHeader == null) {
            return null;
        } else {
            String[] authTokens = authorizationHeader.split(" ");
            return authTokens != null && authTokens.length >= 2 ? this.getPrincipalsAndCredentials(authTokens[0], authTokens[1]) : null;
        }
    }

    protected String[] getPrincipalsAndCredentials(String scheme, String encoded) {
        String decoded = Base64.decodeToString(encoded);
        return decoded.split(":", 2);
    }
}

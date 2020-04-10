package shiro.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author payno
 * @date 2020/4/10 11:46
 * @description
 */
public abstract class OncePerRequestFilter extends NameableFilter{
    private static final Logger log = LoggerFactory.getLogger(org.apache.shiro.web.servlet.OncePerRequestFilter.class);
    public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";
    private boolean enabled = true;

    public OncePerRequestFilter() {
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String alreadyFilteredAttributeName = this.getAlreadyFilteredAttributeName();
        if (request.getAttribute(alreadyFilteredAttributeName) != null) {
            log.trace("Filter '{}' already executed.  Proceeding without invoking this filter.", this.getName());
            filterChain.doFilter(request, response);
        } else if (this.isEnabled(request, response) && !this.shouldNotFilter(request)) {
            log.trace("Filter '{}' not yet executed.  Executing now.", this.getName());
            request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);

            try {
                this.doFilterInternal(request, response, filterChain);
            } finally {
                request.removeAttribute(alreadyFilteredAttributeName);
            }
        } else {
            log.debug("Filter '{}' is not enabled for the current request.  Proceeding without invoking this filter.", this.getName());
            filterChain.doFilter(request, response);
        }

    }

    protected boolean isEnabled(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        return this.isEnabled();
    }

    protected String getAlreadyFilteredAttributeName() {
        String name = this.getName();
        if (name == null) {
            name = this.getClass().getName();
        }

        return name + ".FILTERED";
    }

    /** @deprecated */
    @Deprecated
    protected boolean shouldNotFilter(ServletRequest request) throws ServletException {
        return false;
    }

    protected abstract void doFilterInternal(ServletRequest var1, ServletResponse var2, FilterChain var3) throws ServletException, IOException;

}

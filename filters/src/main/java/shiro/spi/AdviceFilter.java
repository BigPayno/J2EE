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
 * @date 2020/4/10 16:27
 * @description
 *      添加了前置后置清理处理等切面
 *      使用了模板方法，只有前置处理执行返回true才会使用该过滤器，并最终执行cleanup方法
 */
public abstract class AdviceFilter extends OncePerRequestFilter{
    private static final Logger log = LoggerFactory.getLogger(org.apache.shiro.web.servlet.AdviceFilter.class);

    public AdviceFilter() {
    }

    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        return true;
    }

    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
    }

    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
    }

    protected void executeChain(ServletRequest request, ServletResponse response, FilterChain chain) throws Exception {
        chain.doFilter(request, response);
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        Exception exception = null;

        try {
            boolean continueChain = this.preHandle(request, response);
            if (log.isTraceEnabled()) {
                log.trace("Invoked preHandle method.  Continuing chain?: [" + continueChain + "]");
            }

            if (continueChain) {
                this.executeChain(request, response, chain);
            }

            this.postHandle(request, response);
            if (log.isTraceEnabled()) {
                log.trace("Successfully invoked postHandle method");
            }
        } catch (Exception var9) {
            exception = var9;
        } finally {
            this.cleanup(request, response, exception);
        }

    }

    protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) throws ServletException, IOException {
        Exception exception = existing;

        try {
            this.afterCompletion(request, response, exception);
            if (log.isTraceEnabled()) {
                log.trace("Successfully invoked afterCompletion method.");
            }
        } catch (Exception var6) {
            if (existing == null) {
                exception = var6;
            } else {
                log.debug("afterCompletion implementation threw an exception.  This will be ignored to allow the original source exception to be propagated.", var6);
            }
        }

        if (exception != null) {
            if (exception instanceof ServletException) {
                throw (ServletException)exception;
            } else if (exception instanceof IOException) {
                throw (IOException)exception;
            } else {
                if (log.isDebugEnabled()) {
                    String msg = "Filter execution resulted in an unexpected Exception (not IOException or ServletException as the Filter API recommends).  Wrapping in ServletException and propagating.";
                    log.debug(msg);
                }

                throw new ServletException(exception);
            }
        }
    }
}

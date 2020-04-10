package shiro.spi;

import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.servlet.ServletContextSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * @author payno
 * @date 2020/4/10 11:37
 * @description
 *      抽象类，拥有用FilterConfig初始化配置的能力
 *      以及获得ServletContetx的能力
 */
public abstract class AbstractFilter extends ServletContextSupport implements Filter {
    private static final transient Logger log = LoggerFactory.getLogger(org.apache.shiro.web.servlet.AbstractFilter.class);
    protected FilterConfig filterConfig;

    public AbstractFilter() {
    }

    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        this.setServletContext(filterConfig.getServletContext());
    }

    protected String getInitParam(String paramName) {
        FilterConfig config = this.getFilterConfig();
        return config != null ? StringUtils.clean(config.getInitParameter(paramName)) : null;
    }

    public final void init(FilterConfig filterConfig) throws ServletException {
        this.setFilterConfig(filterConfig);

        try {
            this.onFilterConfigSet();
        } catch (Exception var3) {
            if (var3 instanceof ServletException) {
                throw (ServletException)var3;
            } else {
                if (log.isErrorEnabled()) {
                    log.error("Unable to start Filter: [" + var3.getMessage() + "].", var3);
                }

                throw new ServletException(var3);
            }
        }
    }

    protected void onFilterConfigSet() throws Exception {
    }

    public void destroy() {
    }
}

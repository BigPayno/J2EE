package shiro.spi;

import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.PathConfigProcessor;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author payno
 * @date 2020/4/10 16:34
 * @description
 */
public abstract class PathMatchingFilter extends AdviceFilter implements PathConfigProcessor {
    private static final Logger log = LoggerFactory.getLogger(org.apache.shiro.web.filter.PathMatchingFilter.class);
    protected PatternMatcher pathMatcher = new AntPathMatcher();
    protected Map<String, Object> appliedPaths = new LinkedHashMap();

    public PathMatchingFilter() {
    }

    @Override
    public Filter processPathConfig(String path, String config) {
        String[] values = null;
        if (config != null) {
            values = StringUtils.split(config);
        }

        this.appliedPaths.put(path, values);
        return this;
    }

    protected String getPathWithinApplication(ServletRequest request) {
        return WebUtils.getPathWithinApplication(WebUtils.toHttp(request));
    }

    protected boolean pathsMatch(String path, ServletRequest request) {
        String requestURI = this.getPathWithinApplication(request);
        log.trace("Attempting to match pattern '{}' with current requestURI '{}'...", path, requestURI);
        return this.pathsMatch(path, requestURI);
    }

    protected boolean pathsMatch(String pattern, String path) {
        return this.pathMatcher.matches(pattern, path);
    }

    @Override
    /**
     *  如果Filter中配置为空或者null，那么直接跳过该Filter,否则遍历直到找到对应的Filter
     */
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        if (this.appliedPaths != null && !this.appliedPaths.isEmpty()) {
            Iterator var3 = this.appliedPaths.keySet().iterator();

            String path;
            do {
                if (!var3.hasNext()) {
                    return true;
                }

                path = (String)var3.next();
            } while(!this.pathsMatch(path, request));

            log.trace("Current requestURI matches pattern '{}'.  Determining filter chain execution...", path);
            Object config = this.appliedPaths.get(path);
            return this.isFilterChainContinued(request, response, path, config);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("appliedPaths property is null or empty.  This Filter will passthrough immediately.");
            }

            return true;
        }
    }

    private boolean isFilterChainContinued(ServletRequest request, ServletResponse response, String path, Object pathConfig) throws Exception {
        if (this.isEnabled(request, response, path, pathConfig)) {
            if (log.isTraceEnabled()) {
                log.trace("Filter '{}' is enabled for the current request under path '{}' with config [{}].  Delegating to subclass implementation for 'onPreHandle' check.", new Object[]{this.getName(), path, pathConfig});
            }

            return this.onPreHandle(request, response, pathConfig);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Filter '{}' is disabled for the current request under path '{}' with config [{}].  The next element in the FilterChain will be called immediately.", new Object[]{this.getName(), path, pathConfig});
            }

            return true;
        }
    }

    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return true;
    }

    protected boolean isEnabled(ServletRequest request, ServletResponse response, String path, Object mappedValue) throws Exception {
        return this.isEnabled(request, response);
    }
}

package shiro.spi;

import javax.servlet.FilterConfig;

/**
 * @author payno
 * @date 2020/4/10 11:41
 * @description
 *    拥有获得名字的能力
 */
public abstract class NameableFilter extends AbstractFilter implements Namable{
    private String name;

    public NameableFilter() {
    }

    protected String getName() {
        if (this.name == null) {
            FilterConfig config = this.getFilterConfig();
            if (config != null) {
                this.name = config.getFilterName();
            }
        }

        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected StringBuilder toStringBuilder() {
        String name = this.getName();
        if (name == null) {
            return super.toStringBuilder();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            return sb;
        }
    }
}

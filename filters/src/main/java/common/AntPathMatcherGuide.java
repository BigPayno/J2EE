package common;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;

/**
 * @author payno
 * @date 2020/4/10 11:25
 * @description
 * 可以做URLs匹配，规则如下
 *
 *     ？匹配一个字符
 *     *匹配0个或多个字符
 *     **匹配0个或多个目录
 *
 * */
public class AntPathMatcherGuide {
    AntPathMatcher antPathMatcher=new AntPathMatcher();

    @Test
    public void test(){
        Assert.assertEquals(false,antPathMatcher.match("/?x","/x"));
        Assert.assertEquals(true,antPathMatcher.match("/?x","/ax"));
        Assert.assertEquals(true,antPathMatcher.match("/*x","/x"));
        Assert.assertEquals(true,antPathMatcher.match("/*x","/ax"));
    }
}

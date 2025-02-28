package me.agradip.oxypaste.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheManagerFactoryBean;

import java.net.URISyntaxException;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public JCacheManagerFactoryBean cacheManagerFactoryBean() throws URISyntaxException {
        JCacheManagerFactoryBean bean = new JCacheManagerFactoryBean();
        bean.setCacheManagerUri(getClass().getResource("/ehcache.xml").toURI());
        return bean;
    }

    @Bean
    public CacheManager cacheManager(JCacheManagerFactoryBean cacheManagerFactoryBean) {
        return new org.springframework.cache.jcache.JCacheCacheManager(cacheManagerFactoryBean.getObject());
    }
}

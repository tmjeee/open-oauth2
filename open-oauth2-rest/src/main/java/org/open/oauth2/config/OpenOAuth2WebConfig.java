package org.open.oauth2.config;

import org.open.oauth2.interceptor.AuthorizationHeaderInterceptor;
import org.open.oauth2.service.*;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.*;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableConfigurationProperties({OpenOAuth2ConfigurationProperties.class})
public class OpenOAuth2WebConfig implements WebMvcConfigurer {

    @Autowired
    DataSource datasource;


    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/html/**")
                .addResourceLocations("classpath:/html/**");
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizationHeaderInterceptor());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*");
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(datasource);
    }


    @Bean
    public Jdbi getJdbi() {
        return Jdbi.create(datasource);
    }


    @Bean
    @ConditionalOnMissingBean
    public AuthorizationCodeGenerator authorizationCodeGenerator() {
        return new DefaultAuthorizationCodeGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationTokenGenerator authorizationTokenGenerator() {
        return new DefaultAuthorizationTokenGenerator();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Encryptor encryptor() throws NoSuchAlgorithmException {
        return new Encryptor();
    }
}

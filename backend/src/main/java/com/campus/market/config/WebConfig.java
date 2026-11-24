package com.campus.market.config;

import com.campus.market.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置：拦截器、跨域、静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${campus.upload.path}")
    private String uploadPath;

    @Value("${campus.upload.url-prefix}")
    private String urlPrefix;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/merchant/register",
                        "/api/auth/captcha",
                        "/api/upload"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String abs = Paths.get(uploadPath).toAbsolutePath().toString();
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + abs + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

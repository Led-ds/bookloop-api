package com.bookloop.shared.config;

import com.bookloop.organization.api.OrganizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final OrganizationInterceptor organizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Só as rotas escopadas por comunidade passam pela validação de membership.
        registry.addInterceptor(organizationInterceptor)
                .addPathPatterns("/api/v1/orgs/**");
    }
}

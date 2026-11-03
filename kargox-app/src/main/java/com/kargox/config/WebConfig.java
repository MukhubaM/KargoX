package com.kargox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${kargox.uploads.directory}")
    private String uploadsDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String uploadsPath = new File(uploadsDirectory).getAbsolutePath();
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadsPath + File.separator);

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {

                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("ws")
                                || resourcePath.startsWith("swagger-ui") || resourcePath.startsWith("v3/")
                                || resourcePath.startsWith("h2-console") || resourcePath.startsWith("actuator")
                                || resourcePath.startsWith("uploads/")) {

                            // These result in 404 normally instead of masking typos with the SPA shell
                            return null;
                        }

                        Resource requested = location.createRelative(resourcePath);

                        if (requested.exists() && requested.isReadable()) {

                            return requested;
                        }

                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}

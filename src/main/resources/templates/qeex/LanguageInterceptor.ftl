package io.quarkus.qeex.api.interceptors;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@ApplicationScoped
public class LanguageInterceptor implements ContainerRequestFilter {

    private String language;


    public String getLanguage() {
        return language;
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        if (context.getHeaders().containsKey("language")) {
            var language = context.getHeaders().getFirst("language");
            this.language = language;
        }
    }
}

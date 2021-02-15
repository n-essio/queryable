package it.coopservice.api.filter;

import org.jboss.logging.Logger;

import javax.ws.rs.container.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    Logger logger = Logger.getLogger(getClass());

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
        logger.info(requestCtx.getMethod() + " - " + requestCtx.getUriInfo().getPath());
        responseCtx.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseCtx.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");

        responseCtx.getHeaders().add("Access-Control-Max-Age", "1209600");
        responseCtx.getHeaders()
                .add("Access-Control-Allow-Headers",
                        "Authorization, Accept, accept, Accept-Language, authorization, Content-Language, Origin, Content-Type,content-type, X-Requested-With, hostname, Pragma, mobile");
        try {
            MultivaluedMap<String, String> multiValuedMap = requestCtx.getHeaders();
            if (multiValuedMap.containsKey("Origin") && !responseCtx.getHeaders()
                    .containsKey("Access-Control-Allow-Origin")) {
                {
                    responseCtx.getHeaders().add("Access-Control-Allow-Origin", requestCtx.getHeaderString("Origin"));

                }
            }
        } catch (Exception e) {
            responseCtx.getHeaders().add("Access-Control-Allow-Origin", "*");
        }

    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getMethod().equals("OPTIONS")) {
            //            logger.info("OPTIONS METHOD DETECTED");
            Response.ResponseBuilder builder = Response.ok();
            requestContext.abortWith(builder.build());
        }
    }
}

package io.quarkus.qeex.api.rs;

import io.quarkus.qeex.api.exceptions.QeexWebException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class QuarkusWebExceptionProvider implements ExceptionMapper<QeexWebException> {

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(QeexWebException exception) {
        return Response
                .status(exception.code)
                .entity(exception.toJson())
                .build();
    }
}

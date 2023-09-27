package ${groupId}.api.qeex.rs;

import ${groupId}.api.qeex.exceptions.QeexWebException;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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

package  ${groupId}.api.producer;

import org.jboss.logging.Logger;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsExceptionMapper implements ExceptionMapper<Exception> {

    Logger logger = Logger.getLogger(getClass());

    @Override
    public Response toResponse(Exception exception) {
    logger.info("CorsExceptionMapper");
    return Response
    .status(Response.Status.BAD_REQUEST)
    .entity(exception.getMessage())
    .header("Access-Control-Allow-Credentials", "true")
    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
    .header("Access-Control-Max-Age", "1209600")
    .header("Access-Control-Allow-Headers",
    "Authorization, Accept, accept, Accept-Language, authorization, Content-Language, Origin, Content-Type,content-type, X-Requested-With, hostname, Pragma, mobile")
    .header("Access-Control-Allow-Origin", "*")
    .build();
    }
    }
package ${groupId}.api.qeex.rs;

import ${groupId}.api.qeex.annotations.QeexConfig;
import ${groupId}.api.qeex.exceptions.QeexWebException;
import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuarkusWebDefaultExceptionProvider implements ExceptionMapper<Throwable> {


    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(Throwable exception) {
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        var qeexConfig = config.getConfigMapping(QeexConfig.class);
        String message = qeexConfig.default_message().get();
        if (message == null || message.isBlank()) {
            message = "no default message";
        }
        QeexWebException webException = QeexWebException
                .builder(qeexConfig.project().get())
                .language(qeexConfig.default_language().get())
                .code(qeexConfig.default_code().get())
                .message(message)
                .language(qeexConfig.default_language().get());
        return Response
                .status(webException.code)
                .entity(webException.toJson())
                .build();
    }
}

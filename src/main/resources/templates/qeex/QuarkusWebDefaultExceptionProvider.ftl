package io.quarkus.qeex.api.rs;

import io.quarkus.qeex.api.annotations.QeexConfig;
import io.quarkus.qeex.api.exceptions.QeexWebException;
import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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

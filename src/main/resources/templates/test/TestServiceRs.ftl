package ${packageName}.service.rs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import ${packageName}.model.*;
import ${packageName}.service.profile.PostgresResource;
import ${packageName}.service.util.KeycloakUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static io.restassured.RestAssured.given;
import static ${packageName}.management.AppConstants.*;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ${testClassName} {

    @Inject
    KeycloakUtils keycloakUtils;

    <#list createdItems as x>
    ${x}
    </#list>

    public String getBearerToken() {
        String token = "Bearer " + keycloakUtils.getToken();
        return token;
    }

    @Transactional
    public String createSingle() {
        return null;
    }

    @Transactional
    public int createList() {
        return 0;
    }

${allMethods}
}

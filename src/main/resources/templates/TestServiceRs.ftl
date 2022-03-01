package ${packageName}.service.rs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import ${packageName}.model.*;
import ${packageName}.service.rs.profile.PostgresResource;
import ${packageName}.service.rs.util.KeycloakUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static io.restassured.RestAssured.given;
import static ${packageName}.management.AppConstants.*;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleServiceRsTest {

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

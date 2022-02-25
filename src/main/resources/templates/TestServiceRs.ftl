package ${packageName}.test.service.rs;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import it.ness.querytester.querytester.model.Simple;
import it.ness.querytester.test.service.util.KeycloaUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static it.ness.querytester.querytester.management.AppConstants.SIMPLE_PATH;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class SimpleServiceRsTest {

    @ConfigProperty(name = "keycloak.username")
    String username;

    @ConfigProperty(name = "keycloak.password")
    String password;

    @ConfigProperty(name="quarkus.oidc.auth-server-url")
    String serverUrl;

    @ConfigProperty(name="quarkus.oidc.client-id")
    static String clientId;

    static String accessToken;

    @Test
    public void testFetch() {
        String uuid = createSingle();
        given().header(makeBearerToken())
                .when().get(SIMPLE_PATH + "/" + uuid)
                .then()
                .statusCode(200)
                .body(is("Hello RESTEasy"));
    }

    public String getAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }
        String body = String.format("client_id=%s&username=%s&password=%s&grant_type=password", clientId, username, password);
        accessToken = given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .baseUri(serverUrl)
                .body(body)
                .post("/protocol/openid-connect/token")
                .then().extract().response().jsonPath().getString("access_token");
        return accessToken;
    }

    private String createSingle() {
        Simple simple = new Simple();
        simple.persistAndFlush();
        return simple.uuid;
    }

    private int createList() {
        Simple.persist(Simple.build("flower", "the power"),
                Simple.build("more", "info"));
        return Simple.listAll().size();
    }

    @Test
    public void testList() {
        int size = createList();
        given().header(makeBearerToken())
                .when().get(SIMPLE_PATH)
                .then()
                .statusCode(200)
                .body(is("Hello RESTEasy"));
    }

    @Test
    public void testInsert() {
        // create using POST on API
        // try to use the uuid to read the object by Simple.findById()
    }

    @Test
    public void testUpdate() {
        // create using POST on API
        // update using PUT api
        // try to use the uuid to read the object by Simple.findById()

    }

    @Test
    public void testDelete() {
        // create using POST on API
        // delete using DELETE api
        // try to use the uuid to read the object by Simple.findById()
    }
}

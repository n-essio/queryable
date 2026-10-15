package it.ness.subpackagetest.reports;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.ness.subpackagetest.reports.domain.entities.archive.annual.Report;
import it.ness.subpackagetest.reports.web.rest.archive.annual.ReportServiceRs;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ReportSubpackageV4Test {
    @Inject
    ReportServiceRs service;

    @Test
    void generatedV4ServiceUsesCustomRootsAndNestedEntityAtRuntime() {
        assertNotNull(service);
        assertEquals("it.ness.subpackagetest.reports.web.rest.archive.annual",
                ReportServiceRs.class.getPackageName());
        QuarkusTransaction.requiringNew().run(() -> {
            Report.deleteAll();
            persist("1", "annual");
            persist("2", "monthly");
        });
        try {
            given().when().get("/api/v1/reports")
                    .then().statusCode(200).body("uuid", contains("1", "2"));
            given().queryParam("obj.code", "annual").when().get("/api/v1/reports")
                    .then().statusCode(200).body("uuid", contains("1"));
            long count = QuarkusTransaction.requiringNew().call(() -> Report.count());
            assertEquals(2L, count);
        } finally {
            QuarkusTransaction.requiringNew().run(() -> Report.deleteAll());
        }
    }

    private void persist(String uuid, String code) {
        Report report = new Report();
        report.uuid = uuid;
        report.code = code;
        report.persist();
    }
}

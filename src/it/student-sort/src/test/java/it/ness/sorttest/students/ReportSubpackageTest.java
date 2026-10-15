package it.ness.sorttest.students;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.ness.sorttest.students.model.finance.monthly.Report;
import it.ness.sorttest.students.service.rs.finance.monthly.ReportServiceRs;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ReportSubpackageTest {
    @Inject
    ReportServiceRs service;

    @BeforeEach
    void seedReports() {
        QuarkusTransaction.requiringNew().run(() -> {
            Report.deleteAll();
            persist("1", "Alpha", "Monthly");
            persist("2", "Alpha", "Annual");
            persist("3", "Beta", "Monthly");
        });
    }

    @Test
    void relocatedServiceIsInjectableAndRetainsManualAndGeneratedFilters() {
        assertNotNull(service);
        assertEquals("it.ness.sorttest.students.service.rs.finance.monthly",
                ReportServiceRs.class.getPackageName());
        given().queryParam("obj.code", "ALPHA").when().get("/api/reports")
                .then().statusCode(200).body("uuid", contains("1", "2"));
        given().queryParam("obj.code", "ALPHA").queryParam("obj.title", "Monthly")
                .when().get("/api/reports")
                .then().statusCode(200).body("uuid", contains("1"));
        given().when().get("/api/reports/marker")
                .then().statusCode(200).body(org.hamcrest.Matchers.equalTo("manual-endpoint"));
        long count = QuarkusTransaction.requiringNew().call(() -> Report.count());
        assertEquals(3L, count);
    }

    private void persist(String uuid, String code, String title) {
        Report report = new Report();
        report.uuid = uuid;
        report.code = code;
        report.title = title;
        report.persist();
    }
}

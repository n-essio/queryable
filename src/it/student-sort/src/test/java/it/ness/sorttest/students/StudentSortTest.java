package it.ness.sorttest.students;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import it.ness.sorttest.students.model.Student;
import it.ness.sorttest.students.service.rs.StudentServiceRs;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@QuarkusTest
class StudentSortTest {

    @Inject
    StudentServiceRs service;

    @Test
    void oidcProfileInjectsIdentityAndProvidesUsernameWithFallback() throws Exception {
        assumeTrue(Boolean.getBoolean("queryable.test.oidc"));
        Class<?> api = StudentServiceRs.class.getSuperclass();
        Field identity = api.getDeclaredField("securityIdentity");
        Method currentUser = api.getDeclaredMethod("getCurrentUser");
        Method currentUsername = api.getDeclaredMethod("getCurrentUsername");
        identity.setAccessible(true);
        currentUser.setAccessible(true);
        currentUsername.setAccessible(true);
        Object injectedIdentity = identity.get(service);
        assertNotNull(injectedIdentity);
        try {
            Object alice = identityWithPrincipal(identity.getType(), () -> "alice");
            identity.set(service, alice);
            assertSame(alice, currentUser.invoke(service));
            assertEquals("alice", currentUsername.invoke(service));
            identity.set(service, null);
            assertEquals("system", currentUsername.invoke(service));
            identity.set(service, identityWithPrincipal(identity.getType(), null));
            assertEquals("system", currentUsername.invoke(service));
        } finally {
            identity.set(service, injectedIdentity);
        }
    }

    @BeforeEach
    void seedStudents() {
        QuarkusTransaction.requiringNew().run(() -> {
            Student.deleteAll();
            persist("1", "Zoe", "Rossi");
            persist("2", "Marco", "Bianchi");
            persist("3", "Anna", "Rossi");
            persist("4", "Anna", "Bianchi");
        });
    }

    @Test
    void getStudentsUsesMultipleDefaultSortProperties() {
        given().when().get("/api/students")
                .then().statusCode(200)
                .header("listSize", "4")
                .body("uuid", contains("4", "2", "3", "1"))
                .body("surname", contains("Bianchi", "Bianchi", "Rossi", "Rossi"))
                .body("name", contains("Anna", "Marco", "Anna", "Zoe"));
    }

    @Test
    void getStudentsAcceptsMultipleSortPropertiesInQueryParameter() {
        given().queryParam("orderBy", "surname asc, name asc")
                .when().get("/api/students")
                .then().statusCode(200)
                .body("uuid", contains("4", "2", "3", "1"));
    }

    @Test
    void queryParameterOverridesDefaultAndPreservesEachDirection() {
        given().queryParam("orderBy", "surname desc, name asc")
                .when().get("/api/students")
                .then().statusCode(200)
                .body("uuid", contains("3", "1", "4", "2"));
    }

    @Test
    void paginationUsesBothSortProperties() {
        given().queryParam("startRow", 1).queryParam("pageSize", 1)
                .when().get("/api/students")
                .then().statusCode(200)
                .header("listSize", "4")
                .body("size()", equalTo(1))
                .body("uuid", contains("2"));
    }

    private void persist(String uuid, String name, String surname) {
        Student student = new Student();
        student.uuid = uuid;
        student.name = name;
        student.surname = surname;
        student.persist();
    }

    private Object identityWithPrincipal(Class<?> identityType, Principal principal) {
        return Proxy.newProxyInstance(identityType.getClassLoader(), new Class<?>[]{identityType},
                (proxy, method, arguments) -> {
                    if ("getPrincipal".equals(method.getName())) {
                        return principal;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
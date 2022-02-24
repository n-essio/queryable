    void shouldAddAnItem() {
        <#list insert as x>
        ${x}
        </#list>

        String location = given()
            .body(${classInstance})
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post(${rsPath})
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().header("Location");
        assertTrue(location.contains(${rsPath}));
    }
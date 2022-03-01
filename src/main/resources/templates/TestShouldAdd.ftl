
    @Test
    @Order(${addMethodOrder})
    public void ${addMethod}() {
        <#list insertItems as x>
        ${x}
        </#list>

        String token = getBearerToken();

        ${createdInstance} = given()
            .body(${classInstance})
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .when()
            .post(${rsPath})
            .then()
            .statusCode(OK.getStatusCode())
            .extract().body().as(${className}.class);
    }

    @Test
    @Order(${putMethodOrder})
    public void ${putMethod}() {
        <#list putItems as x>
        ${x}
        </#list>

        String token = getBearerToken();

        given()
            .body(${createdInstance})
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .when()
            .put(${rsPath} + "/" + ${createdInstance}.${id})
            .then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }


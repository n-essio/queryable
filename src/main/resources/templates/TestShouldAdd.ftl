
    void ${addMethod}() {
        <#list insertItems as x>
        ${x}
        </#list>

        ${createdInstance} = given()
            .body(${classInstance})
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post(${rsPath})
            .then()
            .statusCode(OK.getStatusCode())
            .extract().body().as(${className}.class);
    }

    void ${putMethod}() {
        <#list putItems as x>
        ${x}
        </#list>

        given()
            .body(${createdInstance})
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .put(${rsPath} + "/" + ${createdInstance}.${id})
            .then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            <#list bodyChecks as b>
            ${b}
            </#list>
    }

# @Queryable

<img src="https://badgen.net/maven/v/maven-central/it.n-ess.queryable/queryable-maven-plugin"><br/>


<img src="docs/queryable.png"><br/>

**Queryable** is a Maven plugin to generate quickly Java classes for **JAX-RS** controllers using with **Quarkus**
and **Hibernate Panache**, with **Hibernate**  @filters on @entity classes annotated.

# Scenario

Normally we use the following paradigm to developing quarkus rest app (our <a href="API.MD">api rules</a>).

1 - Let's start writing our entities with some hibernate filters:
official documentation - [Hibernate_User_Guide#pc-filter](https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#pc-filter)
Hibernate’s @Filter Annotation – Apply Dynamic Filters at Runtime (Thorben Janssen)  https://thorben-janssen.com/hibernate-filter/

```
@Entity
@Table(name = "customers")

@FilterDef(name = "Customer.obj.code", parameters = @ParamDef(name = "code", type = String.class))
@Filter(name = "Customer.obj.code", condition = "code = :code")

@FilterDef(name = "Customer.like.name", parameters = @ParamDef(name = "name", type = String.class))
@Filter(name = "Customer.like.name", condition = "lower(name) LIKE :name")

@FilterDef(name = "Customer.obj.active", parameters = @ParamDef(name = "active", type = Boolean.class))
@Filter(name = "Customer.obj.active", condition = "active = :active")

public class Customer extends PanacheEntityBase {

    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    @Id
    public String uuid;
    public String code;
    public String name;
    public boolean active;
    public String ldap_group;
    public String mail;
}
```

2 - We continue by writing one rest controller for each entity, as:

```
@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class CustomerServiceRs extends RsRepositoryServiceV3<Customer, String> {


    public CustomerServiceRs() {
        super(Customer.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "name asc";
    }

    @Override
    public PanacheQuery<Customer> getSearch(String orderBy) throws Exception {
        PanacheQuery<Customer> search;
        Sort sort = sort(orderBy);

        if (sort != null) {
            search = Customer.find(null, sort);
        } else {
            search = Customer.find(null);
        }
        if (nn("obj.code")) {
            search.filter("Customer.obj.code", Parameters.with("code", get("obj.code")));
        }
        if (nn("like.name")) {
            search.filter("Customer.like.name", Parameters.with("name", likeParamToLowerCase("like.name")));
        }
        search.filter("Customer.obj.active", Parameters.with("active", true));
        return search;
    }

}
```

3 - the customer api, will be querable using:

```
https://prj.n-ess.it/api/v1/customers?obj.code=xxxx&like.name=yyyy
```
You can find more examples <a href="API.MD">in the api rules page.</a>

The boring process is:

- the writing of hibernate filters
- the writing of search conditions using query parameters. With our annotation set, we will generate at request using
  maven goal!

## Quarkus Project Setup

Prerequisites:
- JDK 21

Well!, we will try to start a maven project: https://quarkus.io/guides/getting-started

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.39.5:create \
        -DprojectGroupId=it.queryable \
        -DprojectArtifactId=awesomeproj \
        -Dextensions="jdbc-postgresql,resteasy-jackson,hibernate-orm-panache" \
        -Dpath="/awesomeproj" \
        -DnoCode
cd awesomeproj
```

Following the guide: https://quarkus.io/guides/hibernate-orm-panache we added to the pom.xml the extensions to use Hibernate with Panache with Postgresql driver.
We will have inside the pom.xml: 

```xml

    <!-- Jackson Mapper -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-jackson</artifactId>
    </dependency>
    
    <!-- Hibernate ORM specific dependencies -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-hibernate-orm-panache</artifactId>
    </dependency>

    <!-- JDBC driver dependencies -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
```

### And then?! you should start to in our pom.xml our plugin:

Add queryable to your project:
```
./mvnw it.n-ess.queryable:queryable-maven-plugin:3.0.10:add
```

or directly on the pom.xml:

```xml

<dependency>
    <groupId>it.n-ess.queryable</groupId>
    <artifactId>queryable-maven-plugin</artifactId>
    <version>3.0.10</version>
</dependency>
```

In build section add plugin:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>it.n-ess.queryable</groupId>
            <artifactId>queryable-maven-plugin</artifactId>
            <version>3.0.10</version>
        </plugin>
    </plugins>
</build>
```

Some avaliable options in the configuration:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>it.n-ess.queryable</groupId>
            <artifactId>queryable-maven-plugin</artifactId>
            <version>3.0.10</version>
            <configuration>
                <!-- default is false -->
                <removeAnnotations>false</removeAnnotations>
                <!-- default is {groupId}/model -->
                <sourceModelDirectory>model</sourceModelDirectory>
                <!-- default is {groupId}/service/rs -->
                <sourceRestDirectory>service/rs</sourceRestDirectory>
                <!-- default is src/main/java-->
                <outputDirectory>src/main/java</outputDirectory>
                <!-- default is true -->
                <logging>true|false</logging>
                <!-- default is true -->
                <overrideAnnotations>true|false</overrideAnnotations>
                <!-- default is true -->
                <overrideSearchMethod>true|false</overrideSearchMethod>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### And then?! Our queryable maven cmd:


Before start to edit the entities, run this maven command:

```
./mvnw queryable:install
```
This command will add our minimal api and will add an entity class in the package {groupId}.{artifactId}.model.Greeeting (ie it.queryable.awesomeproj.model.Greeeting).
Our convention is:
 - the package for the api will be: {groupId}.api (ie it.queryable.api)
 - the package for model classes will be:  {groupId}.{artifactId}.model (ie it.queryable.awesomeproj.model)

# And then?! start to write your entities!

After creating your annotated entities, run the following maven command:

```
./mvnw queryable:source
```
That command will add @FilterDef on your model classes and will add the "getSearch" method on existent rest api controllers, or will generate the non existent rest api controllers (one for each model class). 

To process only specific model classes, pass their simple names separated by commas:

```
./mvnw queryable:source -Dclasses=Customer,Order
```

To process all eligible model classes except specific ones:

```
./mvnw queryable:source -DexcludeClasses=Customer,Order
```

`classes` and `excludeClasses` accept comma-separated simple class names. Explicitly excluded classes are not parsed or regenerated. Exclusions take precedence over explicit inclusions and `@QInclude`. Classes annotated with `@QExclude` remain excluded.

### Optional QEEX: REST Error Catalog

QEEX is the optional exception-generation part of Queryable. It does not generate entity filters: it turns an annotated Java interface into a CDI bean whose methods create `QeexWebException` instances. REST exception mappers expose these exceptions as JSON with a project name, stable error ID, HTTP status, message and language label.

#### 1. Install The Support Classes

From the `awesomeproj` directory created above, after `queryable:install`, run:

```bash
./mvnw queryable:qeexinstall
```

This creates annotations, configuration, a request filter and exception mappers under `it.queryable.api.qeex`, plus a sample `it.queryable.awesomeproj.service.exception.ExceptionBundle`.

**Warning:** `qeexinstall` deletes and replaces `it.queryable.api.service.RsResponseService` with the QEEX-aware template. Review or back up local customizations before running it, including on subsequent installations.

#### 2. Configure The Application

Add these entries to `src/main/resources/application.properties`, preserving the database configuration from your Quarkus setup:

```properties
qeex.project=AWESOME
qeex.default.id=100
qeex.default.code=500
qeex.default.message=Unexpected application error
qeex.default.language=en
```

Keep this file present when generating sources. Set the default values explicitly: the catch-all exception mapper reads several of them using `Optional.get()`. `qeex.project` overrides the project name declared on the bundle.

#### 3. Define An Error Catalog

Replace the installed sample interface at `src/main/java/it/queryable/awesomeproj/service/exception/ExceptionBundle.java` with:

```java
package it.queryable.awesomeproj.service.exception;

import it.queryable.api.qeex.annotations.QeexExceptionBundle;
import it.queryable.api.qeex.annotations.QeexMessage;
import it.queryable.api.qeex.exceptions.QeexWebException;

@QeexExceptionBundle(project = "AWESOME", language = "en")
public interface ExceptionBundle {

        @QeexMessage(id = 101, code = 404, message = "Customer %s not found")
        QeexWebException customerNotFound(String customerCode);

        @QeexMessage(id = 102, code = 409, message = "Customer %s is inactive")
        QeexWebException customerInactive(String customerCode);
}
```

`id` is the application error identifier; `code` is the HTTP status. Message arguments use Java `String.format` placeholders in method-parameter order. Use explicit IDs and statuses for a stable client contract: omitted IDs are assigned starting at the bundle's `id + 1` (101 by default), while an omitted method `code` currently generates 500, not the annotation's declared default of 400.

Generate the implementation:

```bash
./mvnw queryable:qeexsource
```

The goal scans interfaces annotated with `@QeexExceptionBundle` under `src/main/java` and creates `ExceptionBundleImpl` next to the interface. Edit the interface, not the generated implementation, and rerun `qeexsource` after changing the catalog. This is a separate goal from `queryable:source`.

#### 4. Throw An Error From A REST Endpoint

Create `src/main/java/it/queryable/awesomeproj/service/rs/CustomerErrorDemoRs.java`:

```java
package it.queryable.awesomeproj.service.rs;

import it.queryable.api.qeex.exceptions.QeexWebException;
import it.queryable.awesomeproj.service.exception.ExceptionBundle;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/qeex-demo/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerErrorDemoRs {

        @Inject
        ExceptionBundle errors;

        @GET
        @Path("/{code}")
        public void findCustomer(@PathParam("code") String customerCode)
                        throws QeexWebException {
                throw errors.customerNotFound(customerCode);
        }
}
```

This endpoint deliberately always throws an error, without querying the database. In a real service, throw it only when the lookup fails. The separate demo path avoids conflicts with generated customer endpoints.

Start the application with `./mvnw quarkus:dev`, then call:

```bash
curl -i -H 'language: en' \
    http://localhost:8080/api/v1/qeex-demo/customers/C001
```

Expected HTTP status: **404**. Expected JSON body:

```json
{
    "projectName": "AWESOME",
    "id": 101,
    "code": 404,
    "message": "Customer C001 not found",
    "language": "en"
}
```

#### Current Limitations

- Throw the exception out of the endpoint to use its HTTP status through `QuarkusWebExceptionProvider`. Existing controller paths that catch exceptions and call `RsResponseService.jsonErrorMessageResponse` or `jsonMessageResponse` return HTTP 500 for QEEX errors, even when the JSON `code` is 404 or 409.
- The generated methods use annotation messages and statuses directly. Although `QeexConfig` defines per-message overrides and translations, those methods do not call its `get_message`, `get_code` or `get_language` helpers. Do not expect `qeex.messages[...]` overrides or automatic translation to change this example.
- The filter reads the custom `language` header, not `Accept-Language`. Generated methods fall back to `en`, not the bundle's `language` or `qeex.default.language`. The current filter stores mutable language state in an application-scoped bean and does not clear it when the header is missing; it is not safe for per-request language isolation. The example sends `language: en` explicitly, but that does not fix concurrent-request isolation.
- The QEEX exception mapper builds JSON by string concatenation without escaping message values. Quotes, backslashes and control characters in arguments can produce invalid JSON; use controlled values for this demo and address serialization before using arbitrary user input.
- The catch-all mapper also handles non-QEEX exceptions using the configured default status and message. It does not assign `qeex.default.id`, so its current error ID is 0. Review this global error policy before enabling QEEX in an existing application.

### Optional OIDC Identity In V3 APIs

When the effective Maven project includes `io.quarkus:quarkus-oidc` (excluding test-scope dependencies), `./mvnw queryable:source` selectively updates the existing `{groupId}.api.service.RsRepositoryServiceV3`:

```java
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;

@Inject
SecurityIdentity securityIdentity;

protected SecurityIdentity getCurrentUser() {
    return securityIdentity;
}

protected String getCurrentUsername() {
    return securityIdentity != null && securityIdentity.getPrincipal() != null
            ? securityIdentity.getPrincipal().getName()
            : "system";
}
```

Inherited dependencies and active Maven profiles are considered; `dependencyManagement` alone does not enable this update. Run `./mvnw queryable:install` first if the API is missing. Existing custom identity methods and `@QExclude` methods are preserved. Repeated generation does not duplicate fields, methods, or imports. Without OIDC, the API is left unchanged, including any previously enabled identity support. V4 APIs are not modified.

## JPA @Entity classes location

The plugins searches for java JPA @Entity classes that extends io.quarkus.hibernate.orm.panache.PanacheEntityBase in
specified folder location {groupId}.{artifactId}.model

## JAX-RS classes location

The plugins searches for java classes (JAX-RS @Path @Singleton classes) in specified folder location
{groupId}.{artifactId}.service.rs with naming convention {entity_name}ServiceRs (Greeting => GreetingServiceRs)

## Usage

### Q annotations

We can attach them to classes or fields, annotations by themselves have no effect on the execution of a program.

- Q (class or field level)
- QExclude (class level)
- QInclude (class level)
- QLike (field level)
- QLikeList (field level)
- QList (field level)
- QLogicalDelete (field level)
- QNil (field level)
- QNotNil (field level)
- QOrderBy (class level)
- QRs (class level)

### Q annotation

Q can be used on class fields: String, enums, LocalDateTime, LocalDate, Date, Boolean, boolean, BigDecimal, Integer,
Long

String usage case:

```
@Q
public String code;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.code", parameters = @ParamDef(name = "code", type = String.class))
@Filter(name = "XXX.obj.code", condition = "code = :code")
```

and in rest service class will add to getSearch method

```
if (nn("obj.code")) {
	search.filter("XXX.obj.code", Parameters.with("code", get("obj.code")));
}
```

Enum usage case:

```
@Enumerated(EnumType.STRING)
@Q
public MovementReason movementReason;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.movementReason", parameters = @ParamDef(name = "movementReason", type = String.class))
@Filter(name = "XXX.obj.movementReason", condition = "movementReason = :movementReason")
```

and in rest service class will add to getSearch method

```
if (nn("obj.movementReason")) {
	search.filter("XXX.obj.movementReason", Parameters.with("movementReason", get("obj.movementReason")));
}
```

if used with QOptions:

```
@Enumerated(EnumType.STRING)
@Q(condition = "BLANK_DELIVERY", options = {QOption.EXECUTE_ALWAYS})
public OperationType operationType;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.operationType", parameters = @ParamDef(name = "operationType", type = String.class))
@Filter(name = "XXX.obj.operationType", condition = "operationType = :operationType")
```

and in rest service class will add to getSearch method (the filter will be execute in each get list request):

```
search.filter("XXX.obj.operationType", Parameters.with("operationType", "BLANK_DELIVERY"));
```

LocalDateTime, LocalDate, Date usage case:

```
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Rome")
@Q
public LocalDateTime execution_date;
```

will create two FilterDefs in model class

```
@FilterDef(name = "XXX.from.execution_date", parameters = @ParamDef(name = "execution_date", type = java.time.LocalDate.class))
@Filter(name = "XXX.from.operation_date", condition = "operation_date >= :execution_date")

@FilterDef(name = "XXX.to.execution_date", parameters = @ParamDef(name = "execution_date", type = java.time.LocalDate.class))
@Filter(name = "XXX.to.execution_date", condition = "execution_date <= :execution_date")

```

and in rest service class will add to getSearch method

```
if (nn("from.execution_date")) {
	LocalDateTime date = LocalDateTime.parse(get("from.execution_date"));
	search.filter("XXX.from.execution_date", Parameters.with("execution_date", date));
}
if (nn("to.execution_date")) {
	LocalDateTime date = LocalDateTime.parse(get("to.execution_date"));
	search.filter("XXX.to.execution_date", Parameters.with("execution_date", date));
}
```

BigDecimal usage case:

```
@Q
public BigDecimal weight;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.weight", parameters = @ParamDef(name = "weight", type = java.math.BigDecimal.class))
@Filter(name = "XXX.obj.weight", condition = "weight = :weight")
```

and in rest service class will add to getSearch method

```
if (nn("obj.weight")) {
	BigDecimal numberof = new BigDecimal(get("obj.weight"));
	search.filter("XXX.obj.weight", Parameters.with("weight", numberof));
}
```

Integer usage case:

```
@Q
public Integer quantity;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.quantity", parameters = @ParamDef(name = "quantity", type = Integer.class))
@Filter(name = "XXX.obj.quantity", condition = "quantity = :quantity")
```

and in rest service class will add to getSearch method

```
if (nn("obj.quantity")) {
	Integer numberof = _integer("obj.quantity");
	search.filter("XXX.obj.quantity", Parameters.with("quantity", numberof));
}
```

Long usage case:

```
@Q
public Long quantity;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.quantity", parameters = @ParamDef(name = "quantity", type = Long.class))
@Filter(name = "XXX.obj.quantity", condition = "quantity = :quantity")
```

and in rest service class will add to getSearch method
```
if (nn("obj.quantity")) { 
	Long numberof = _long("obj.quantity"); 
	search.filter("XXX.obj.quantity", Parameters.with("quantity", numberof)); 
}
```
Boolean usage case:

```
@Q(prefix = "not")
public boolean default_template;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.not.default_template", parameters = @ParamDef(name = "default_template", type = Boolean.class))
@Filter(name = "XXX.not.default_template", condition = "default_template = :default_template")
```

and in rest service class will add to getSearch method

```
if (nn("not.default_template")) {
	Boolean valueof = _boolean("not.default_template");
	search.filter("XXX.not.default_template", Parameters.with("default_template", valueof));
}
```

if used with condition

```
@Q(prefix = "not", condition = "false")
public boolean default_template;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.not.default_template", parameters = @ParamDef(name = "default_template", type = Boolean.class))
@Filter(name = "XXX.not.default_template", condition = "default_template = :default_template")
```

and in rest service class will add to getSearch method

```
if (nn("not.default_template")) {
	search.filter("XXX.not.default_template", Parameters.with("not.default_template", false));
}
```

### QNil, QNotNil annotations

```
@QNil
@QNotNil
public String executor;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.nil.executor")
@Filter(name = "XXX.nil.executor", condition = "executor IS NULL")
@FilterDef(name = "XXX.notNil.executor")
@Filter(name = "XXX.notNil.executor", condition = "executor IS NOT NULL")
```

and in rest service class will add to getSearch method

```
if (nn("nil.executor")) {
	search.filter("XXX.nil.executor");
}
if (nn("notNil.executor")) {
	search.filter("XXX.notNil.executor");
}
```

### QList annotation

on String field:

```
@QList
public String uuid;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.uuids", parameters = @ParamDef(name = "uuids", type = String.class))
@Filter(name = "XXX.obj.uuids", condition = "uuid IN (:uuids)")
```

and in rest service class will add to getSearch method

```
if (nn("obj.uuids")) {
	    search.filter("XXX.obj.uuids", Parameters.with("uuids", asList("obj.uuids")));
}
```
on Integer field:
```
@QList
public Integer id;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.ids", parameters = @ParamDef(name = "ids", type = Integer.class))
@Filter(name = "XXX.obj.ids", condition = "id IN (:ids)")
```

and in rest service class will add to getSearch method

```
if (nn("obj.uuids")) {
	    search.filter("XXX.obj.uuids", Parameters.with("uuids", asIntegerList("obj.uuids")));
}
```
on Long field:
```
@QList
public Long id;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.ids", parameters = @ParamDef(name = "ids", type = Integer.class))
@Filter(name = "XXX.obj.ids", condition = "id IN (:ids)")
```

and in rest service class will add to getSearch method

```
if (nn("obj.uuids")) {
	    search.filter("XXX.obj.uuids", Parameters.with("uuids", asLongList("obj.uuids")));
}
```

### QLikeList annotation

```
@QLikeList
public String tags;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.like.tags", parameters = @ParamDef(name = "tags", type = String.class))
@Filter(name = "XXX.like.tags", condition = "lower(tags) LIKE :tags")
```

and in rest service class will add to getSearch method

```
String query = null;
Map<String, Object> params = null;
if (nn("like.tags")) {
	String[] tags = get("like.tags").split(",");
	StringBuilder sb = new StringBuilder();
	if (null == params) {
		params = new HashMap<>();
	}
	for (int i = 0; i < tags.length; i++) {
		final String paramName = String.format("tags%d", i);
		sb.append(String.format("tags LIKE :%s", paramName));
		params.put(paramName, tags[i]);
		if (i < tags.length - 1) {
			sb.append(" OR ");
		}
	}
	if (null == query) {
		query = sb.toString();
	} else {
		query = query + " OR " + sb.toString();
	}
}
PanacheQuery<CostCenter> search;
Sort sort = sort(orderBy);
if (sort != null) {
	search = CostCenter.find(query, sort, params);
} else {
	search = CostCenter.find(query, params);
}
```

### QLogicalDelete annotation

```
@QLogicalDelete
public boolean active = true;
```

will create FilterDef in model class

```
@FilterDef(name = "XXX.obj.active", parameters = @ParamDef(name = "active", type = Boolean.class))
@Filter(name = "XXX.obj.active", condition = "active = :active")
```

and in rest service class will add to getSearch method

```
search.filter("XXX.obj.active", Parameters.with("active", true));
```

### QInclude annotation

Used on class level to select a class for filterdef generation. 
If QInclude is used, all other classes are ignored.
If QInclude is not used, all classes are selected.

### QExclude annotation

Used on class level to deselect a class for filterdef generation.


## Test builder annotations


### QT annotation
QT annotation is used to describe test values, and override defaults, for test classes generation. 

```
@QT(defaultValue = "default_fiscal_code", updatedValue = "updated_fiscal_code")
public String fiscal_code;
```
To generate test classes run
```
./mvnw queryable:testsources
```
The plugin will generate test stub classes for each model class, with adding, updating, deleting model items.
Headers are generated for keycloak too, using token from oidc-client, thus dependencies for oidc-client should be added, 
along with appropriate settings in application.properties 
If annotations is not used on field, default values are used depending on field type.
For String default values are
```
defaultValue = "defaultValue_" + field name
updatedValue = "updatedValue_" + field name
```
for int, Integer, long, Long 
```
defaultValue = "0";
updatedValue = "1";
```
for boolean, Boolean
```
defaultValue = "false";
updatedValue = "true";
```
for LocalDateTime
```
defaultValue = LocalDateTime.now().toString();
updatedValue = LocalDateTime.now().plusDays(1).toString();
```
for LocalDate
```
defaultValue = LocalDate.now().toString();
updatedValue = LocalDate.now().plusDays(1).toString();
```
for BigDecimal
```
defaultValue = "0";
updatedValue = "1";
```

example class, with just one overloaded default value for fiscal code
```
public class Simple extends PanacheEntityBase {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  @Column(name = "uuid", unique = true)
  public String uuid;

  public String name;

  public String surname;

  @QT(defaultValue = "BBAZNB67E68Z301G", updatedValue = "5f4984648e00e528b50")
  public String fiscal_code;

  public LocalDateTime born_date;
}
```
generated test class
```
@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleServiceRsTest {

    @Inject
    KeycloakUtils keycloakUtils;

    public static Simple createdSimple;

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


    @Test
    @Order(1)
    public void shouldAddSimpleItem() {
        Simple simple = new Simple();
        simple.fiscal_code = "BBAZNB67E68Z301G";
        simple.name = "defaultValue_name";
        simple.surname = "defaultValue_surname";
        simple.born_date = LocalDateTime.parse("2022-03-02T19:33:23.379120043");

        String token = getBearerToken();

        createdSimple = given()
            .body(simple)
            .header(CONTENT_TYPE, ContentType.JSON)
            .header(ACCEPT, ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .when()
            .post(SIMPLE_PATH)
            .then()
            .statusCode(OK.getStatusCode())
            .extract().body().as(Simple.class);
    }

    @Test
    @Order(2)
    public void shouldPutSimpleItem() {
        Simple simple = createdSimple;
        simple.fiscal_code = "5f4984648e00e528b50";
        simple.name = "updatedValue_name";
        simple.surname = "updatedValue_surname";
        simple.born_date = LocalDateTime.parse("2022-03-03T19:33:23.380619377");

        String token = getBearerToken();

        given()
            .body(createdSimple)
            .header(CONTENT_TYPE, ContentType.JSON)
            .header(ACCEPT, ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .when()
            .put(SIMPLE_PATH + "/" + createdSimple.uuid)
            .then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(3)
    public void shouldDeleteSimpleItem() {

        String token = getBearerToken();

        given()
            .header(HttpHeaders.AUTHORIZATION, token)
            .when()
            .delete(SIMPLE_PATH + "/" + createdSimple.uuid)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

    }
}
```

## Migrate to Quarkus3, from Quarkus1 and Quarkus2


```
To migrate to quarkus 3 run
```
./mvnw queryable:quarkus3conv
```
The plugin will replace javax imports with jakarta, and quarku2 filterdefs types with quarkus3 class types.
The migration is not complete, pom.xml, application.properties needs to be updated too.
quarkus version in pom.xml is not updated, must be done manually.

```
To build qeex messages in app properties, setup plugin as
```
            <plugin>
                <groupId>it.n-ess.queryable</groupId>
                <artifactId>queryable-maven-plugin</artifactId>
                <version>3.0.10</version>
                <executions>
                    <execution>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>build-lang</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```
The plugin will generate qeex messages in app propeties, with translations to IT, DE.

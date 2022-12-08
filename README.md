# @Queryable

<img src="https://badgen.net/maven/v/maven-central/it.n-ess.queryable/queryable-maven-plugin"><br/>


<img src="docs/queryable.png"><br/>

**Queryable** is a Maven plugin to generate quickly Java classes for **JAX-RS** controllers using with **Quarkus**
and **Hibernate Panache**, with **Hibernate**  @filters on @entity classes annotated.

# Scenario

Normally we use the following paradigm to developing quarkus rest app (our <a href="API.MD">api rules</a>).

1 - Let's start writing our entities with some hibernate filters:
official documentation - https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#pc-filter
Hibernate’s @Filter Annotation – Apply Dynamic Filters at Runtime (Thorben Janssen)  https://thorben-janssen.com/hibernate-filter/

```
@Entity
@Table(name = "customers")

@FilterDef(name = "Customer.obj.code", parameters = @ParamDef(name = "code", type = "string"))
@Filter(name = "Customer.obj.code", condition = "code = :code")

@FilterDef(name = "Customer.like.name", parameters = @ParamDef(name = "name", type = "string"))
@Filter(name = "Customer.like.name", condition = "lower(name) LIKE :name")

@FilterDef(name = "Customer.obj.active", parameters = @ParamDef(name = "active", type = "boolean"))
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

Well!, we will try to start a maven project: https://quarkus.io/guides/getting-started

```
mvn io.quarkus.platform:quarkus-maven-plugin:2.14.3.Final:create \
        -DprojectGroupId=it.queryable \
        -DprojectArtifactId=awesomeproj \
        -DclassName="it.queryable.awesomeproj.service.rs.GreetingResource" \
        -Dpath="/awesomeproj"
cd awesomeproj
```

Following the guide: https://quarkus.io/guides/hibernate-orm-panache we will add to the pom.xml configuration the hibernate/panache/driver dependencies:

```
./mvnw quarkus:add-extension -Dextensions="jdbc-postgresql,resteasy-jackson,hibernate-orm-panache"
```

or directly on the pom.xml: 

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
./mvnw it.n-ess.queryable:queryable-maven-plugin:1.0.14:add
```

or directly on the pom.xml:

```xml

<dependency>
    <groupId>it.n-ess.queryable</groupId>
    <artifactId>queryable-maven-plugin</artifactId>
    <version>1.0.14</version>
</dependency>
```

In build section add plugin:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>it.n-ess.queryable</groupId>
            <artifactId>queryable-maven-plugin</artifactId>
            <version>1.0.14</version>
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
            <version>1.0.14</version>
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
@FilterDef(name = "XXX.obj.code", parameters = @ParamDef(name = "code", type = "string"))
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
@FilterDef(name = "XXX.obj.movementReason", parameters = @ParamDef(name = "movementReason", type = "string"))
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
@FilterDef(name = "XXX.obj.operationType", parameters = @ParamDef(name = "operationType", type = "string"))
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
@FilterDef(name = "XXX.from.execution_date", parameters = @ParamDef(name = "execution_date", type = "LocalDateTime"))
@Filter(name = "XXX.from.operation_date", condition = "operation_date >= :execution_date")

@FilterDef(name = "XXX.to.execution_date", parameters = @ParamDef(name = "execution_date", type = "LocalDateTime"))
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
@FilterDef(name = "XXX.obj.weight", parameters = @ParamDef(name = "weight", type = "big_decimal"))
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
@FilterDef(name = "XXX.obj.quantity", parameters = @ParamDef(name = "quantity", type = "int"))
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
@FilterDef(name = "XXX.obj.quantity", parameters = @ParamDef(name = "quantity", type = "long"))
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
@FilterDef(name = "XXX.not.default_template", parameters = @ParamDef(name = "default_template", type = "boolean"))
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
@FilterDef(name = "XXX.not.default_template", parameters = @ParamDef(name = "default_template", type = "boolean"))
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
@FilterDef(name = "XXX.obj.uuids", parameters = @ParamDef(name = "uuids", type = "string"))
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
@FilterDef(name = "XXX.obj.ids", parameters = @ParamDef(name = "ids", type = "int"))
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
@FilterDef(name = "XXX.obj.ids", parameters = @ParamDef(name = "ids", type = "int"))
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
@FilterDef(name = "XXX.like.tags", parameters = @ParamDef(name = "tags", type = "string"))
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
@FilterDef(name = "XXX.obj.active", parameters = @ParamDef(name = "active", type = "boolean"))
@Filter(name = "XXX.obj.active", condition = "active = :active")
```

and in rest service class will add to getSearch method

```
search.filter("XXX.obj.active", Parameters.with("active", true));
```

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


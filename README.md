# queryable
 Maven plugin for FilterDefs

## Core Configuration
#### pom.xml

```xml
<dependency>
    <groupId>it.ness.queryable</groupId>
    <artifactId>queryable-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
        
<!-- in build section add plugin -->

<build>
    <plugins>
        <plugin>
            <groupId>it.ness.queryable.plugin</groupId>
            <artifactId>queryable-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
		<!-- optional set json file for conversion to plural -->
		<pluralsJsonFile>src/main/resources/plurals.json</pluralsJsonFile>
		<!-- default is false -->
		<removeAnnotations>false</removeAnnotations>         
		<!-- default is {groupId}/service/rs -->    
		<sourceModelDirectory>service/rs</sourceModelDirectory> 
		<!-- default is {groupId}/model -->    
		<sourceRestDirectory>model</sourceRestDirectory> 		
		<!-- default is src/main/java-->   
		<outputDirectory>src/main/java</outputDirectory> 	
			<!-- default is true -->    
		<logging>true|false</logging> 							
		<!-- default is true -->    
		<ovverideAnnotations>true|false</ovverideAnnotations> 	
		<!-- default is true -->    
		<ovverideSearchMethod>true|false</ovverideSearchMethod> 
	    </configuration>
        </plugin>
    </plugins>
</build>
```

## Model classes location (JPA @Entity classes that extends io.quarkus.hibernate.orm.panache.PanacheEntityBase) 

The plugins searches for java classes that extend PanacheEntityBase in specified folder location {groupId}\model

## Rest classes location (JAX-RS @Path @Singleton classes) 

The plugins searches for java classes in specified folder location {groupId}\service/rs with naming convention...

## Q annotations

Q can be used on class fields: String, enums, LocalDateTime, LocalDate, Date, Boolean, boolean, BigDecimal, Integer, Long

String usage case:

```
@Q
public String code;
```
will create FilterDef in model class
```
@FilterDef(name = "obj.code", parameters = @ParamDef(name = "code", type = "string"))
@Filter(name = "obj.code", condition = "code = :code")
```
and in rest service class will add to getSearch method
```
if (nn("obj.code")) {
	search.filter("obj.code", Parameters.with("code", get("obj.code")));
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
@FilterDef(name = "obj.movementReason", parameters = @ParamDef(name = "movementReason", type = "string"))
@Filter(name = "obj.movementReason", condition = "movementReason = :movementReason")
```
and in rest service class will add to getSearch method
```
if (nn("obj.movementReason")) {
	search.filter("obj.movementReason", Parameters.with("movementReason", get("obj.movementReason")));
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
@FilterDef(name = "obj.operationType", parameters = @ParamDef(name = "operationType", type = "string"))
@Filter(name = "obj.operationType", condition = "operationType = :operationType")
```
and in rest service class will add to getSearch method
```
search.filter("obj.operationType", Parameters.with("operationType", "BLANK_DELIVERY"));
```
LocalDateTime, LocalDate, Date usage case:
```
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Rome")
@Q
public LocalDateTime execution_date;
```
will create two FilterDefs in model class
```
@Filter(name = "from.execution_date", condition = "execution_date >= :execution_date")
@FilterDef(name = "to.execution_date", parameters = @ParamDef(name = "execution_date", type = "LocalDateTime"))
@Filter(name = "to.execution_date", condition = "execution_date <= :execution_date")
@FilterDef(name = "like.username", parameters = @ParamDef(name = "username", type = "string"))
```
and in rest service class will add to getSearch method
```
if (nn("from.execution_date")) {
	LocalDateTime date = LocalDateTime.parse(get("from.execution_date"));
	search.filter("from.execution_date", Parameters.with("execution_date", date));
}
if (nn("to.execution_date")) {
	LocalDateTime date = LocalDateTime.parse(get("to.execution_date"));
	search.filter("to.execution_date", Parameters.with("execution_date", date));
}
```
BigDecimal usage case:
```
@Q
public BigDecimal weight;
```
will create FilterDef in model class
```
@FilterDef(name = "obj.weight", parameters = @ParamDef(name = "weight", type = "big_decimal"))
@Filter(name = "obj.weight", condition = "weight = :weight")
```
and in rest service class will add to getSearch method
```
if (nn("obj.weight")) {
	BigDecimal numberof = new BigDecimal(get("obj.weight"));
	search.filter("obj.weight", Parameters.with("weight", numberof));
}
```
Integer usage case:
```
@Q
public Integer quantity;
```
will create FilterDef in model class
```
@FilterDef(name = "obj.quantity", parameters = @ParamDef(name = "quantity", type = "int"))
@Filter(name = "obj.quantity", condition = "quantity = :quantity")
```
and in rest service class will add to getSearch method
```
if (nn("obj.quantity")) {
	Integer numberof = _integer("obj.quantity");
	search.filter("obj.quantity", Parameters.with("quantity", numberof));
}
```
Long usage case:
```
@Q
public Long quantity;
```
will create FilterDef in model class
```
@FilterDef(name = "obj.quantity", parameters = @ParamDef(name = "quantity", type = "long"))
@Filter(name = "obj.quantity", condition = "quantity = :quantity")
```
and in rest service class will add to getSearch method

if (nn("obj.quantity")) {
	Long numberof = _long("obj.quantity");
	search.filter("obj.quantity", Parameters.with("quantity", numberof));
}

Boolean usage case:
```
@Q(prefix = "not")
public boolean default_template;
```
will create FilterDef in model class
```
@FilterDef(name = "not.default_template", parameters = @ParamDef(name = "default_template", type = "boolean"))
@Filter(name = "not.default_template", condition = "default_template = :default_template")
```
and in rest service class will add to getSearch method
```
if (nn("not.default_template")) {
	Boolean valueof = _boolean("not.default_template");
	search.filter("not.default_template", Parameters.with("default_template", valueof));
}
```
if used with condition
```
@Q(prefix = "not", condition = "false")
public boolean default_template;
```
will create FilterDef in model class
```
@FilterDef(name = "not.default_template", parameters = @ParamDef(name = "default_template", type = "boolean"))
@Filter(name = "not.default_template", condition = "default_template = :default_template")
```
and in rest service class will add to getSearch method
```
if (nn("not.default_template")) {
	search.filter("not.default_template", Parameters.with("not.default_template", false));
}
```

## Qnil, QNotNil annotations

```
@QNil
@QNotNil
public String executor;
```
will create FilterDef in model class
```
@FilterDef(name = "nil.executor")
@Filter(name = "nil.executor", condition = "executor IS NULL")
@FilterDef(name = "notNil.executor")
@Filter(name = "notNil.executor", condition = "executor IS NOT NULL")
```
and in rest service class will add to getSearch method
```
if (nn("nil.executor")) {
	search.filter("nil.executor");
}
if (nn("notNil.executor")) {
	search.filter("notNil.executor");
}
```
## QList annotation
```
@QList
public String operation_uuid;
```
will create FilterDef in model class
```
@FilterDef(name = "obj.operation_uuids", parameters = @ParamDef(name = "operation_uuids", type = "string"))
@Filter(name = "obj.operation_uuids", condition = "operation_uuid IN (:operation_uuids)")
```
and in rest service class will add to getSearch method
```
if (nn("obj.operation_uuids")) {
	String[] operation_uuids = get("obj.operation_uuids").split(",");
	getEntityManager().unwrap(Session.class).enableFilter("obj.operation_uuids")
			.setParameterList("operation_uuids", operation_uuids);
}
```

## QLogicalDelete annotation
```
@QLogicalDelete
public boolean active = true;
```
will create FilterDef in model class
```
@FilterDef(name = "obj.active", parameters = @ParamDef(name = "active", type = "boolean"))
@Filter(name = "obj.active", condition = "active = :active")
```
and in rest service class will add to getSearch method
```
search.filter("obj.active", Parameters.with("active", true));
```

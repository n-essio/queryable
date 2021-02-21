package ${groupId}.${artifactId}.model;

import it.ness.queryable.annotations.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.persistence.*;
import static ${groupId}.${artifactId}.management.AppConstants.GREETING_PATH;

@Entity
@Table(name = "greeetings")
@Q
@QRs(GREETING_PATH)
@QOrderBy("name asc")
public class Greeting extends PanacheEntityBase {

@Id
public String uuid;
public String name;

}

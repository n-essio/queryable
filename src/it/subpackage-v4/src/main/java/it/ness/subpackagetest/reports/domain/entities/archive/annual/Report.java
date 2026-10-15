package it.ness.subpackagetest.reports.domain.entities.archive.annual;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.ness.queryable.annotations.Q;
import it.ness.queryable.annotations.QOrderBy;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")
@QOrderBy("uuid asc")
public class Report extends PanacheEntityBase {
    @Id
    public String uuid;
    @Q
    public String code;
}

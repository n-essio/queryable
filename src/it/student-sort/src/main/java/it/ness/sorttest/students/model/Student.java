package it.ness.sorttest.students.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.ness.queryable.annotations.QOrderBy;
import it.ness.queryable.annotations.QRs;
import it.ness.queryable.annotations.Q;
import it.ness.queryable.annotations.QPreserveFilters;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@QOrderBy("surname asc, name asc")
@QRs("STUDENTS")
@QPreserveFilters("Student.obj.name")
@FilterDef(name = "Student.obj.name", parameters = @ParamDef(name = "manualName", type = String.class))
@Filter(name = "Student.obj.name", condition = "lower(name) = :manualName")
public class Student extends PanacheEntityBase {

    @Id
    public String uuid;
    @Q public String name;
    @Q public String surname;
}
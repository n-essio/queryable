package it.ness.sorttest.students.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.ness.queryable.annotations.QOrderBy;
import it.ness.queryable.annotations.QRs;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@QOrderBy("surname asc, name asc")
@QRs("STUDENTS")
public class Student extends PanacheEntityBase {

    @Id
    public String uuid;
    public String name;
    public String surname;
}
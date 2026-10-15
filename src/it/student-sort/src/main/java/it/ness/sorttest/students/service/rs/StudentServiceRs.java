package it.ness.sorttest.students.service.rs;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import it.ness.queryable.annotations.QPreservedFilter;
import it.ness.sorttest.api.service.RsRepositoryServiceV3;
import it.ness.sorttest.students.model.Student;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Locale;
import java.util.Map;

import static it.ness.sorttest.students.management.AppConstants.STUDENTS;

@Path("/api/students")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentServiceRs extends RsRepositoryServiceV3<Student, String> {
    public StudentServiceRs() {
        super(Student.class);
    }

    @Override
    protected String getId(Student student) {
        return student.uuid;
    }

    @QPreservedFilter("Student.obj.name")
    private void applyManualName(PanacheQuery<Student> search) {
        if (nn("obj.name")) {
            String name = get("obj.name").toLowerCase(Locale.ROOT);
            search.filter("Student.obj.name", Map.of("manualName", name));
        }
    }
}

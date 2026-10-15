package it.ness.sorttest.students.model.finance.monthly;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.ness.queryable.annotations.Q;
import it.ness.queryable.annotations.QOrderBy;
import it.ness.queryable.annotations.QPreserveFilters;
import it.ness.queryable.annotations.QRs;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@QRs("REPORTS")
@QOrderBy("uuid asc")
@QPreserveFilters("Report.obj.code")
@FilterDef(name = "Report.obj.code", parameters = @ParamDef(name = "manual", type = String.class))
@Filter(name = "Report.obj.code", condition = "lower(code) = :manual")
public class Report extends PanacheEntityBase {
    @Id
    public String uuid;
    @Q
    public String code;
    @Q
    public String title;
}

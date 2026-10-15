package it.ness.sorttest.students.service.rs;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import it.ness.queryable.annotations.QPreservedFilter;
import it.ness.sorttest.api.service.RsRepositoryServiceV3;
import it.ness.sorttest.students.model.finance.monthly.Report;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Locale;
import java.util.Map;

import static it.ness.sorttest.students.management.AppConstants.REPORTS;

@Path(REPORTS)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportServiceRs extends RsRepositoryServiceV3<Report, String> {
    public ReportServiceRs() {
        super(Report.class);
    }

    @Override
    protected String getId(Report report) {
        return report.uuid;
    }

    @QPreservedFilter("Report.obj.code")
    private void applyManualCode(PanacheQuery<Report> search) {
        if (nn("obj.code")) {
            search.filter("Report.obj.code", Map.of("manual", get("obj.code").toLowerCase(Locale.ROOT)));
        }
    }

    @GET
    @Path("/marker")
    @Produces(MediaType.TEXT_PLAIN)
    public String marker() {
        return "manual-endpoint";
    }
}

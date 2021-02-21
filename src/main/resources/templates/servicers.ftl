package ${packageName}.service.rs;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import ${groupId}.api.service.RsRepositoryServiceV3;
import ${packageName}.model.${className};

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

<#if rsPath=="NOT_SET">
<#else>
import static ${packageName}.management.AppConstants.${rsPath};
</#if>

<#if rsPath=="NOT_SET">
@Path("NOT_SET")
<#else>
@Path(${rsPath})
</#if>
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ${className}ServiceRs extends RsRepositoryServiceV3<${className}, String> {


    public ${className}ServiceRs() {
        super(${className}.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "${defaultSort}";
    }

    @Override
    public PanacheQuery<${className}> getSearch(String orderBy) throws Exception {
        PanacheQuery<${className}> search;
        return search;
    }
}
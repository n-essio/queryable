package ${packageName}.service.rs;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import ${groupId}.api.service.RsRepositoryServiceV3;
import ${packageName}.model.${className};

import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

<#if rsPath??>
import static ${packageName}.management.AppConstants.${rsPath};
@Path(${rsPath})
<#else>
@Path("NOT_SET")
</#if>
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ${className}ServiceRs extends RsRepositoryServiceV3<${className}, ${idFieldType}> {


    public ${className}ServiceRs() {
        super(${className}.class);
    }

    protected ${idFieldType} getId(${className} object) {
      // field with @id
      return object.${idFieldName};
    }

    @Override
    protected String getDefaultOrderBy() {
        <#if defaultSort??>
        return "${defaultSort}";
        <#else>
        return "not_set";
        </#if>
    }

    @Override
    public PanacheQuery<${className}> getSearch(String orderBy) throws Exception {
        PanacheQuery<${className}> search;
        return search;
    }
}
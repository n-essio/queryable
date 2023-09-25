package ${packageName}.service.rs;

import ${groupId}.api.service.RsRepositoryServiceV4;
import ${packageName}.model.${className};

import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;

<#if rsPath??>
import static ${packageName}.management.AppConstants.${rsPath};
@Path(${rsPath})
<#else>
@Path("NOT_SET")
</#if>
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ${className}ServiceRs extends RsRepositoryServiceV4<${className}, ${idFieldType}> {


    public ${className}ServiceRs() {
        super(${className}.class);
    }

    @Override
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
    public Predicate[] query(CriteriaBuilder criteriaBuilder, Root<${className}> root) throws Exception {
        var predicates = new ArrayList<Predicate>();
        return predicates.toArray(new Predicate[]{});
    }


}
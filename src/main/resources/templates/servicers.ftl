package ${packageName}.service.rs;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import ${apiPackageName}.api.service.RsRepositoryServiceV3;
import ${packageName}.model.${modelName};

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static ${packageName}.management.AppConstants.${rsPath};


@Path(${rsPath})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ${modelName}ServiceRs extends RsRepositoryServiceV3<${modelName}, String> {


    public ${modelName}ServiceRs() {
        super(${modelName}.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "${defaultSort}";
    }

    @Override
    public PanacheQuery<${modelName}> getSearch(String orderBy) throws Exception {
        PanacheQuery<${modelName}> search;
        return search;
    }
}
package ${packageName};

import io.quarkus.qeex.api.annotations.QeexConfig;
import io.quarkus.qeex.api.exceptions.QeexWebException;
import io.quarkus.qeex.api.interceptors.LanguageInterceptor;

import javax.annotation.processing.Generated;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Generated(value = "dev.queryable.QuexGenerator", date = "${creationDate}", comments = "don't touch please")
public class ${className} implements ExceptionBundle {

        // copied from application properties: qeex.project=FLW
        String classProjectName = "FLW";

        @Inject
        QeexConfig qeexConfig;

        @Inject
        LanguageInterceptor languageInterceptor;

<#list methods as method>
        @Override
        ${method}
</#list>

}
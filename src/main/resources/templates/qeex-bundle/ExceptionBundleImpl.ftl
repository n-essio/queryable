package ${packageName};

import  ${groupId}.api.qeex.annotations.QeexConfig;
import  ${groupId}.api.qeex.exceptions.QeexWebException;
import  ${groupId}.api.qeex.interceptors.LanguageInterceptor;

import io.quarkus.Generated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Generated(value = "dev.queryable.QuexGenerator", date = "${creationDate}", comments = "don't touch please")
public class ${className} implements ExceptionBundle {

// copied from application properties: qeex.project=FLW
String classProjectName = "${project}";

@Inject
QeexConfig qeexConfig;

@Inject
LanguageInterceptor languageInterceptor;

<#list methods as method>
    @Override
    ${method}
</#list>

}
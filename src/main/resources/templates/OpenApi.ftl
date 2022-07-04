openapi: 3.0.0
info:
  version: '1.0'
  title: ${title}
  description: ${description}
paths:
    <#list pathItems as x>
    ${x}
    </#list>


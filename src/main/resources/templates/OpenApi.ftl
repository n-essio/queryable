---
openapi: 3.0.0
info:
  version: '1.0'
  title: Cps Signature App API
  description: API per la versione mobile di cps-signature
paths:
<#list pathItems as x>
   ${x}
   <#list x.methods as y>
   ${y}
   </#list>
</#list>
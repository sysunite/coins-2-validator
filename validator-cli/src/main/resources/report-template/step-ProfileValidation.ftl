<#assign stepName = step.type>

<a name="${container.code}_${stepName}"></a>

<#list step.bundleNames as bundleKey>
<#assign bundle = step.bundles[bundleKey]>

  <#if instanceOf(bundle, "ValidationBundleStatistics")>
    <h4><#if bundle.valid >&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;${stepName+'/'+bundleKey}</h4>

    <p>${bundle.parseDescription()}</p>

    <table>
      <tr><th colspan="4">${bundleKey}</th></tr>
      <#list bundle.queries as query>



        <tr><td><#if query.valid>&#x2705;<#else>&#x26D4;</#if></td><th>${query.reference}</th><td>${query.parseDescription()}</td></tr>

        <#if query.formattedResults?has_content>
        <tr><td colspan="3">
        <ul>
        <#list 0..query.formattedResults?size-1 as i>
          <li>${query.formattedResults[i]}</li>
        </#list>
        </ul>
        </td></tr>
        </#if>


      </#list>
    </table>
  <#else>

    <#--<h4><#if bundle.valid >&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;${stepName+'/'+bundleKey}</h4>-->
    <#--<table>-->
      <#--<tr><th colspan="4">${bundleKey}</th></tr>-->
      <#--<tr><th>${bundle.reference}</th><td>${bundle.description}</td><td>${bundle.quadsAdded} (${bundle.runs})</td></tr>-->
    <#--</table>-->

  </#if>


</#list>


<#list validation.bundleNames as bundleKey>

  <table>

    <#assign bundle = validation.bundleResults[bundleKey] />
    <#if instanceOf(bundle, "InferenceBundleStatistics")>
      <#--<tr><th colspan="3">${bundleKey}</th></tr>-->
      <#--<tr><th>${bundle.reference}</th><td>${bundle.description}</td><td>${bundle.quadsAdded} (${bundle.runs})</td></tr>-->
    <#else>
      <tr><th colspan="4">${bundleKey}</th></tr>
      <#list bundle?keys as queryKey>
        <#assign query = bundle[queryKey] />


        <#if instanceOf(query, "ValidationQueryResult")>

          <tr><td><#if query.passed>&#x2705;<#else>&#x1F6AB;</#if></td><th>${query.reference}</th><td>${query.description}</td></tr>

          <#if query.formattedResults?has_content>
            <tr><td colspan="3">
              <ul>
                <#list 0..query.formattedResults?size-1 as i>
                  <li>${query.formattedResults[i]}</li>
                </#list>
              </ul>
            </td></tr>
          </#if>
        </#if>

      </#list>
    </#if>
  </table>

</#list>
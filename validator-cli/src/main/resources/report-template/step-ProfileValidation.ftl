<#list validation.bundleNames as bundleKey>

<table>
  <tr><th colspan="3">${bundleKey}</th></tr>



  <#assign bundle = validation.bundleResults[bundleKey] />
  <#list bundle?keys as queryKey>
    <#assign query = bundle[queryKey] />

    <#if query.validationQuery>


      <tr><th>${query.reference}</th><td>${query.description}</td><td><#if query.passed>✅<#else>⛔️</#if></td></tr>

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


    <#if query.inferenceQuery>


      <tr><th>${query.reference}</th><td>${query.description}</td><td>${query.quadsAdded}</td></tr>



    </#if>

  </#list>
</table>

</#list>
<table>
  <tr><td>internalDocumentReferences</td>
    <td>
    <#if validation.internalDocumentReferences?has_content>
      <#list validation.internalDocumentReferences as id>
      ${id}<br/>
      </#list>
    <#else>
      <i>none</i>
    </#if>
    </td></tr>
</table>
<#assign stepName = step.type>

<a name="${container.code}_${stepName}"></a>
<h4><#if step.valid >&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;${stepName}</h4>

<table>
  <tr>
    <td><#if step.valid>&#x2705;<#else>&#x26D4;</#if></td>
    <th>unmatchedInternalDocumentReferences</th>
    <td>All internalDocumentReferences should point to available attachment files.
    <#if step.unmatchedInternalDocumentReferences?has_content>
    Listing invalid file references:
      <ul>
        <#list step.unmatchedInternalDocumentReferences?keys as doc>
          <li>${step.unmatchedInternalDocumentReferences[doc]} (${doc})</li>
        </#list>
      </ul>
    </#if>
    </td>
  </tr>
</table>
<#assign stepName = step.type>

<a name="${container.code}_${stepName}"></a>
<h4><#if step.valid >&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;${stepName}</h4>

<table>
  <tr>
    <td></td>
    <th>internalDocumentReferences</th>
    <td>Available internalDocumentReferences:<br/>
    <#if step.internalDocumentReferences?has_content>
      <ul>
        <#list step.internalDocumentReferences?keys as doc>
          <li>${step.internalDocumentReferences[doc]} (${doc})</li>
        </#list>
      </ul>
    </#if>
    </td>
  </tr>
  <tr>
    <td><#if step.valid>&#x2705;<#else>&#x26D4;</#if></td>
    <th>unmatched references</th>
    <td>All internalDocumentReferences should point to available attachment files (listing invalid files):<br/>
    <#if step.unmatchedInternalDocumentReferences?has_content>
      <ul>
        <#list step.unmatchedInternalDocumentReferences?keys as doc>
          <li>${step.unmatchedInternalDocumentReferences[doc]} (${doc})</li>
        </#list>
      </ul>
    </#if>
    </td>
  </tr>
</table>
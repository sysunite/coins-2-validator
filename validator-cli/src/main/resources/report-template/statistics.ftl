<#assign containerFile = container.getContainerFile()>

<#assign contentFiles         = containerFile.getContentFiles()>
<#assign invalidContentFiles  = containerFile.getInvalidContentFiles()>
<#assign libraries            = containerFile.getRepositoryFiles()>
<#assign invalidLibraries     = containerFile.getInvalidRepositoryFiles()>
<#assign attachments          = containerFile.getAttachmentFiles()>


<table>
  <tr><th>name</th><td>${containerFile.getName()}</td></tr>
  <tr><th>size</th><td>${containerFile.length() / 1024 / 1024} Mb</td></tr>


  <tr>
    <th>content files</th>
    <#if contentFiles?has_content>
      <td>
        <#list 0..contentFiles?size-1 as i>
          &#x1F4C4;&nbsp;&nbsp;${contentFiles[i]}<br/>
        </#list>
      </td>
    <#else>
      <td>
        <i>none</i>
      </td>
    </#if>
  </tr>


  <tr>
    <th>library files</th>

    <#if libraries?has_content>
      <td>
        <#if libraries?size lt 10>
          <#list 0..libraries?size-1 as i>


            <#assign availableNamespaces = containerFile.getRepositoryFileNamespaces(libraries[i])?join(", ", "")>
            &#x1F4D3;&nbsp;&nbsp;${libraries[i]} (${availableNamespaces})<br/>

          </#list>
        <#else>
          <#list 0..9 as i>


            <#assign availableNamespaces = containerFile.getRepositoryFileNamespaces(libraries[i])?join(", ", "")>
            &#x1F4D3;&nbsp;&nbsp;${libraries[i]}  (${availableNamespaces})<br/>

          </#list>
          <div style="display:none" id="libraries_list_${container.code}">

            <#list 10..libraries?size-1 as i>


              <#assign availableNamespaces = containerFile.getRepositoryFileNamespaces(libraries[i])?join(", ", "")>
              &#x1F4D3;&nbsp;&nbsp;${libraries[i]} (${availableNamespaces})<br/>

            </#list>
          </div>
          <div onclick="document.getElementById('libraries_list_${container.code}').className+=' active';">show all...</div>
        </#if>
      </td>
    <#else>
      <td>
        <i>none</i>
      </td>
    </#if>
  </tr>

  <tr>
    <th>attachments</th>
    <td>
      <#if attachments?has_content>
        <#if attachments?size lt 10>
          <#list 0..attachments?size-1 as i>
            &#x1F4CE;&nbsp;&nbsp;${attachments[i]}<br/>
          </#list>
        <#else>
          <#list 0..9 as i>
            &#x1F4CE;&nbsp;&nbsp;${attachments[i]} <br/>
          </#list>
          <div style="display:none" id="attachment_list_${container.code}">

            <#list 10..attachments?size-1 as i>
              &#x1F4CE;&nbsp;&nbsp;${attachments[i]} <br/>
            </#list>
          </div>
          <div onclick="document.getElementById('attachment_list_${container.code}').className+=' active';">show all...</div>
        </#if>
      <#else>
        <i>none</i>
      </#if>
    </td>
  </tr>




  <tr>
    <th>steps</th>
    <td>
      <#list container.steps as step>
        <#assign stepName = step.type>
        <#if step.valid>&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;<a href="#${container.code}_${stepName}">${stepName}</a><br/>
      </#list>
    </td>
  </tr>


</table>

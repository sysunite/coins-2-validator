<#assign stepName = step.type>

<a name="${container.code}_${stepName}"></a>
<h4><#if step.valid >&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;${stepName}</h4>

<table>
  <tr>
    <td>${printBoolean(step.nonCorruptZip, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>nonCorruptZip</th>
    <td>The container file should be a valid zip file</td>
  </tr>
  <tr>
    <td>${printBoolean(step.forwardSlashes, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>forwardSlashes</th>
    <td>The container should use the right type of slashes (/)</td>
  </tr>
  <tr>
    <td>${printBoolean(step.oneRepoFile, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>oneRepoFile</th>
    <td>There should be precisely one rdf-file in the bim-folder</td>
  </tr>
  <tr>
    <td>${printBoolean(step.noWrongContentFile, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noWrongContentFile</th>
    <td>All files in content folder should be valid rdf-files (listing invalid files):<br/>
      <#if invalidContentFiles?has_content>
      <ul>
        <#list 0..invalidContentFiles?size-1 as i>
          <li>${invalidContentFiles[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <td>${printBoolean(step.noWrongRepositoryFile, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noWrongRepositoryFile</th>
    <td>All files in repository folder should be valid rdf-files (listing invalid files):<br/>
      <#if invalidLibraries?has_content>
      <ul>
        <#list 0..invalidLibraries?size-1 as i>
          <li>${invalidLibraries[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <td>${printBoolean(step.noSubsInBim, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noSubsInBim</th>
    <td>There should be no sub folders in the bim-folder</td>
  </tr>
  <tr>
    <td>${printBoolean(step.noOrphans, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noOrphans</th>
    <td>There should be no files in the root folder or in a non-supported folder</td>
  </tr>
  <tr>
    <td></td>
    <th>imports</th>
    <td>Import statements in the rdf-file from the bim-folder (listing all):<br/>
      <#if step.imports?has_content>
      <ul>
        <#list 0..step.imports?size-1 as i>
        <li>${step.imports[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <td>${printBoolean(step.allImportsImportable, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>allImportsImportable</th>
    <td>All import statements in the rdf-file from the bim-folder should be resolvable with files from the bim/repository folder (listing unresolvable imports):<br/>
      <#if step.unmatchedImports?has_content>
      <ul>
        <#list 0..step.unmatchedImports?size-1 as i>
        <li>${step.unmatchedImports[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
</table>


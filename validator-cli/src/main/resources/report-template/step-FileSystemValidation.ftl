<#assign stepName = step.type>

<a name="${container.code}_${stepName}"></a>
<h4><#if step.valid >&#x2705;<#else>&#x26D4;</#if>&nbsp;&nbsp;${stepName}</h4>

<table>
  <tr>
    <th colspan="3">zip file</th>
  </tr>
  <tr>
    <td>${printBoolean(step.fileFound, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>fileFound</th>
    <td>The container file should be found.</td>
  </tr>
  <tr>
    <td>${printBoolean(step.nonCorruptZip, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>nonCorruptZip</th>
    <td>The container file should be a valid zip file.</td>
  </tr>
  <tr>
    <td>${printBoolean(step.forwardSlashes, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>forwardSlashes</th>
    <td>The container should use the right type of slashes (/).</td>
  </tr>
  <tr>
    <th colspan="3">triple files</th>
  </tr>
  <tr>
    <td>${printBoolean(step.oneRepoFile, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>oneRepoFile</th>
    <td>There should be precisely one file in the <i>bim</i> folder.</td>
  </tr>
  <tr>
    <td>${printBoolean(step.noWrongContentFile, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noWrongContentFile</th>
    <td>All files in the <i>bim</i> folder should be valid triple files.
      <#if invalidContentFiles?has_content>
      Listing invalid files:
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
    <td>All files in the <i>bim/repository</i> folder should be valid triple files.
      <#if invalidLibraries?has_content>
      Listing invalid files:
      <ul>
        <#list 0..invalidLibraries?size-1 as i>
          <li>${invalidLibraries[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <td>${printBoolean(step.noCollidingNamespaces, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noCollidingNamespaces</th>
    <td>All triple files (both in <i>bim</i> and the <i>bim/repository</i> folder) should contain unique contexts.
      <#if collidingNamespaces?has_content>
      Listing colliding contexts:
      <ul>
        <#list 0..collidingNamespaces?size-1 as i>
        <li>${collidingNamespaces[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <td>${printBoolean(step.allImportsImportable, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>allImportsImportable</th>
    <td>All import statements in the triple file from the <i>bim</i> folder should be resolvable with files from the <i>bim/repository</i> folder.
      <#if step.unmatchedImports?has_content>
      Listing unresolvable imports:
      <ul>
        <#list 0..step.unmatchedImports?size-1 as i>
        <li>${step.unmatchedImports[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <th colspan="3">other files</th>
  </tr>
  <tr>
    <td>${printBoolean(step.noOrphans, 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>noOrphans</th>
    <td>There should be no files or folders in the root folder or in any unsupported folders.
      <#if orphanFiles?has_content>
      Listing illegal files:
      <ul>
        <#list 0..orphanFiles?size-1 as i>
        <li>${orphanFiles[i]}</li>
        </#list>
      </ul>
      </#if>
    </td>
  </tr>
  <tr>
    <th colspan="3">validator environment</th>
  </tr>
  <tr>
    <td>${printBoolean(step.isLoadableAsGraphSet(), 'skipped','&#x2705;','&#x26D4;')}</td>
    <th>couldBeLoaded</th>
    <td>In order for the validator to finish loading the triple content should succeed.</td>
  </tr>

</table>


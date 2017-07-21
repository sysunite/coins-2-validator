<table>
  <tr>
    <td><#if step.nonCorruptZip>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>nonCorruptZip</th>
    <td>The container file should be a valid zip file</td>
  </tr>
  <tr>
    <td><#if step.forwardSlashes>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>forwardSlashes</th>
    <td>The container should use the right type of slashes (/)</td>
  </tr>
  <tr>
    <td><#if step.oneRepoFile>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>oneRepoFile</th>
    <td>There should be precisely one rdf-file in the bim - folder</td>
  </tr>
  <tr>
    <td><#if step.noWrongContentFile>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>noWrongContentFile</th>
    <td>All files in content folder should be valid rdf files (listing invalid files):<br/>
      <#if invalidContentFiles?has_content>
        <#list 0..invalidContentFiles?size-1 as i>
          ${invalidContentFiles[i]}<br/>
        </#list>
      </#if>
    </td>
  </tr>
  <tr>
    <td><#if step.noWrongRepositoryFile>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>noWrongRepositoryFile</th>
    <td>All files in repository folder should be valid rdf files (listing invalid files):<br/>
      <#if invalidLibraries?has_content>
        <#list 0..invalidLibraries?size-1 as i>
          ${invalidLibraries[i]}<br/>
        </#list>
      </#if>
    </td>
  </tr>
  <tr>
    <td><#if step.noSubsInBim>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>noSubsInBim</th>
    <td>There should be no sub folders in the bim - folder</td>
  </tr>
  <tr>
    <td><#if step.noOrphans>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>noOrphans</th>
    <td>There should be no files in the root folder or in a non-supported folder</td>
  </tr>
  <tr>
    <td><#if step.allImportsImportable>&#x2705;<#else>&#x1F6AB;</#if></td>
    <th>allImportsImportable</th>
    <td>All import statements in the rdf-file from the bim - folder should be resolvable with files from the bim/repository folder</td>
  </tr>
</table>


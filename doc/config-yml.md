# Config.yml

The validation process is configured by the config.yml. This is a yml file. The most basic structure is:
```yaml

version: '2.0.8'
 
environment:
 
  store:
    type: virtuoso
    config: 
      ...
      
  cleanUp: false
  ...
  
run:
 
  containers:
   
  - type: container
    location:
      type: file
      path: override.ccr

    variables:
      ...
      
    graphs:
      ...


      
  steps:
 
  - type: FileSystemValidation
    ...
 
  - type: DocumentReferenceValidation
    ...
 
  - type: ProfileValidation
    ...
 
  reports:
  
  - type: html
    location:
      type: file
      path: report.html


```

The version field is introduced since version 2.0.8.

## Environment

The **environment** section configures which rdf **store** to use and has some *general settings*.

Currently three types of stores are allowed:

#### RDF4J in-memory
```yaml
  store:
    type: rdf4j-sail-memory
```

#### Ontotext GraphDB
```yaml
  store:
    type: graphdb
    config:
      endpoint: http://localhost:7200
      repositoryId: validator-repo
```

The repositoryId is optional, if it is left out the id ```'validator-generated'``` is used.

#### OpenLink Virtuoso
```yaml
  store:
    type: virtuoso
    config:
      endpoint: localhost:1111
      user: dba
      password: dba
```

#### General environment settings:

```yaml
  cleanUp: false
```
Clean up the graphs that were created in the validation process.

```yaml
  createRepo: true
```
If the no usable repo is found in the store create it (if possible for this store type).

```yaml
  destroyRepo: false
```
After validation remove the repo that was used (if possible for this store type) even if it already existed. Also if the validation could not be finished.

## Run

The **run** section consists of three parts:

* The **containers** section goes into details which content from a container to use. It is possible here to inject custom files into the validation process.
* The **steps** section specifies which validation steps need to be executed during validation for all specified containers.
* The **reports** section configures which report files should be generated.

### Containers

During the validation process a container is considered in two ways:
* as a zip file consisting of rdf-files and attachments
* as a set of rdf contexts (graphs)

Which graphs are used in the validation process can be tuned. Graphs are mapped to variables. Also graphs can be copied to new graphs to make unions. 

The definition of one container consists of four parts:
* **type** is either ```'container'``` or ```'virtual'``` (for composing a non-existing container)
* **location** to point to the location of the file for non-virtual type
* **variables** is the map of variables and initial uri's (these will updated depending of the configuration and content of the store)
* **graphs** mappings of sources to variables

#### type
The default configuration is
 ```yaml
  - type: container
 ```
 
For custom usages this setting is also supported
 ```yaml
  - type: virtual
 ```
#### Location

There are two options to point to non-virtual container file. By relative (or absolute) file path:
 ```yaml
    location:
      type: file
      path: folder/container.ccr
 ```
 
 Or to a network link:
 ```yaml
    location:
      type: online
      uri: http://localhost:8080/container.ccr
 ```
 

#### Variables
The default configuration of variables. Specifying them here means the validation steps can guaranteed address them during validation steps. The uri's will be updated depending of the configuration and content of the store.
 ```yaml
    variables:
    - graphname: http://full/union
      variable: FULL_UNION_GRAPH
    - graphname: http://instances/union
      variable: INSTANCE_UNION_GRAPH
    - graphname: http://library/union
      variable: SCHEMA_UNION_GRAPH
 ```


#### Graphs
Specifies which (file) sources should be uploaded to the store. This is the default configuration for a container:
 ```yaml
    graphs:
    - source:
        type: container
        path: bim/*
        graphname: '*'
      as:
      - INSTANCE_UNION_GRAPH
    - source:
        type: container
        path: bim/repository/*
        graphname: '*'
      as:
      - SCHEMA_UNION_GRAPH
    - source:
        type: store
        graph: INSTANCE_UNION_GRAPH
      as:
      - FULL_UNION_GRAPH
    - source:
        type: store
        graph: SCHEMA_UNION_GRAPH
      as:
      - FULL_UNION_GRAPH
 ```
 
Each item in this array has one source and one or more ```as``` variables
```yaml
    - source:
        type: ...
        ...
      as:
      - ...
      - ...
```
 
#### Source

To address a set of triples within an rdf source the ```graphname``` key is used. If ```'*'``` is given as ```graphname``` the source configuration is duplicated for all found graphnames.

A source can be of these four types:
```yaml
    - source:
        type: file
        path: folder/file.rdf
        graphname: 'http://w3c.com/lib'
```
Select the graph with the specified graphname from the rdf-file.
```yaml
    - source:
        type: online
        uri: http://localhost:8080/file.rdf
        graphname: 'http://w3c.com/lib'
```
Select the graph with the specified graphname from the online rdf source.
```yaml
    - source:
        type: container
        path: bim/content.rdf
        graphname: 'http://w3c.com/lib'
```

Wildcards can be used to import all graphs that are found in file(s) in the direct ```bim``` folder and the file(s) in the ```bim/repository``` folder. During the initialisation of the validation process this wildcards are replaced by real files and graphnames (uri's also called contexts). See the ```--yml-to-console``` cli argument.


Select the graph with the specified graphname from the rdf-file inside the container at hand.
```yaml
    - source:
        type: store
        graph: VARIABLE
```
Select a graph in the store that is already identified by some VARIABLE.


### Steps

Any step gets access to content and statistics of the container zip-file and the prepared set of graphs in the store. The step then is executed and can both return the following:
* whether the container is **valid** (resulting in a green check mark in the html-report)
* whether the execution could be finished (needed for the exit code ```0```)
* variables that can be printed in the report

These steps are currently available:

#### File system validation

```yaml
  - type: FileSystemValidation
    lookIn: INSTANCE_UNION_GRAPH
```

This step checks all basic requirements of the coins container zip-file content wise.

#### Document reference validation

```yaml
  - type: DocumentReferenceValidation
    lookIn: INSTANCE_UNION_GRAPH
```

This looks for pointers to files and checks the values of these.

#### Profile validation

```yaml
  - type: ProfileValidation
    profile:
      type: online
      uri: http://localhost:9877/profile.lite-9.85-virtuoso.xml
    maxResults: 100
    reportInferenceResults: false
```

This step executes all the bundles in a profile.xml. Different validation profiles can be chosen. Also a profile.xml can be custom made. For official validation an certified profile should be used.

A profile.xml consists of (SPARQL) queries that can either create extra data (inference queries) or check requirements (validation queries). If a validation query has results this means the validator detected issues and the container is not valid.

Read more about the [profile.xml](https://github.com/sysunite/coins-2-validator/blob/develop/doc/profile-xml.md) in it's own section.

### Reports
Three types of reports are available. Each can be either saved to a file or be uploaded to a network location using a POST request.

```yaml
  - type: html
    location:
      type: file
      path: report.html
```
The html report uses freemarker templates to map all variables from the steps to readable html. Read more about in the [html-report](https://github.com/sysunite/coins-2-validator/blob/develop/doc/html-report.md) section.

```yaml
  - type: xml
    location:
      type: file
      path: report.xml
```
The xml report has the same structure of the config.yml and the profile.xml linked via the profileValidation step.
```yaml
  - type: json
    location:
      type: online
      uri: http://localhost:9911/validationStatistics
```
The json report is identical to the xml report.
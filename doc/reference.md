# Code reference

The coins-validator project consinsts of these packages:

#### validator-core
Mainly contains java interfaces, as little dependencies as possible.

#### validator-cli
All the implementations, depends on graphdb api with rdf4j.

#### alidator-parser-config-yml
Parser for the config.yml. With pojo's that can be separatly used from the cli.

#### validator-parser-profile-xml
Parser for the profile.xml. With pojo's that can be separatly used from the cli.

## Main components

### cli
The cli can be started with a number of arguments. See the [documentation](https://github.com/sysunite/coins-2-validator/blob/develop/doc/command.md) page. The cli starts a runner from the `com.sysunite.coinsweb.runner` package. Either `Describe` for the **describe** mode and `Validation` for the **run** mode. 

### ContainerFileImpl
A coins container is represented in two ways. As a file-archive consisting of triple files and attachments and as a set of graphs in a triple store (see next section).

By instantiating a ContainerFileImpl the file-archive can be read.

```java
ContainerFile containerFile = new ContainerFileImpl("/tmp/container.ccr");
Set<String> libraryFiles = containerFile.getRepositoryFiles();
```

### ContainerGraphSetImpl
A graphset is a selection of graphs (contexts) that represents the content of the coins container. Using a ```Connector``` a connection with a Graph Database is made. The graphs of the graphset are uploaded to this database. After this initialisation the graphset can be queried with SPARQL queries.

```java
ContainerGraphSet graphSet = ContainerGraphSetFactory.lazyLoad(containerFile, containerConfig, connector, inferencePreference);
List<Object> result = graphSet.select("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }");
```

The graphSet sends the query via the connector so the whole database is targetted. To be able to address the graphs belonging to this graphSet (and thus the specific container content) the graphSet keeps track of a map of GraphVar's to context uri's. These variables can be used in the queries in the profile.xml.

```java
Map<GraphVar, String> contexts = graphSet.contextMap();
```

```sparql
SELECT DISTINCT ?a ?b ?c 
WHERE { GRAPH ${SCHEMA_UNION_GRAPH} {
  ?a ?b ?c 
```

### Steps
In the package `com.sysunite.coinsweb.steps` a number of implementations of the interface `ValidationStep` are given. The config.yml can assign a number of steps to a validation run. Using the configuration the config.yml provides each step is instantiated and ran. Every validation step is provided with both the `ContainerFile` and the `ContainerGraphSet` so the step can addres every aspect of the container desired.

```java
public interface ValidationStep {
  void execute(ContainerFile container, ContainerGraphSet graphSet);
```

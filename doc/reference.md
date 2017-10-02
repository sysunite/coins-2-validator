# Code reference


## Contents

* [Brief overview](https://github.com/sysunite/coins-2-validator/edit/develop/doc/brief.md)
* [Connectors](https://github.com/sysunite/coins-2-validator/edit/develop/doc/connectors.md)

## Modules

The coins-validator project consinsts of these packages:

#### validator-core
Mainly contains java interfaces, as little dependencies as possible.

#### validator-cli
All the implementations, depends on graphdb api with rdf4j.

#### alidator-parser-config-yml
Parser for the config.yml. With pojo's that can be separatly used from the cli.

#### validator-parser-profile-xml
Parser for the profile.xml. With pojo's that can be separatly used from the cli.

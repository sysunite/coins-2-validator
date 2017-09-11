# COINS 2.0 Validator

![Coins logo](https://github.com/sysunite/coins-2-validator/blob/develop/icon.png)  Get a validation report of a coins container.

### Use command line interface (cross platform)
* download fat jar [(latest)](https://github.com/sysunite/coins-2-validator/releases/download/v2.0.7/validator-cli-2.0.7.jar) from https://github.com/sysunite/coins-2-validator/releases
* run the command:

```$ java -Xms6g -Xmx8g -jar validator-cli-2.0.7.jar [args]```

* or use a [.bat](https://github.com/sysunite/coins-2-validator/blob/develop/doc/coins-validator.bat) or [.sh](https://github.com/sysunite/coins-2-validator/blob/develop/doc/coins-validator.sh) script to wrap this java command with the name coins-validator:

```$ coins-validator [args]```

![cli help](https://github.com/sysunite/coins-2-validator/blob/develop/doc/cli-help.png)

Read [HOWTO use the CLI](https://github.com/sysunite/coins-2-validator/blob/develop/doc/command.md) for a desrcription how to use the command line interface.

### Use java source in your project

* Include the source in your project
```xml
<dependency>
  <groupId>com.sysunite.coinsweb</groupId>
  <artifactId>coins-validator</artifactId>
  <version>2.0.7</version>
</dependency>
```

Read the [Code reference](https://github.com/sysunite/coins-2-validator/blob/develop/doc/reference.md) for an explanation of the Java code.

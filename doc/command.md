# Command line interface

The **coins-validator** can be run in two modes:
* **describe** mode to generate a part of a configuration file
* **run** mode to execute the validation proces and create a report



## Describe mode
```$ coins-validator describe [args] container.ccr ```

argument | description
--- | ---
-a | make absolute paths in generated config
-h | print help message
-l | write a log file
--yml-to-console | print the generated config
*container.ccr* | the container file being described

If the command was able to generate a configuration fragment the **exit code** is ```0```. In all other cases the **exit code** is ```1```. Add the ```-l``` argument and read the log for more information.

For a description of the the generated configuration read about the [config.yml](https://github.com/sysunite/coins-2-validator/blob/develop/doc/config-yml.md).


## Run mode

For a default configuration:

```$ coins-validator run [args] container.ccr```

For explicit configuration (you want to do this):

```$ coins-validator run [args] config.yml [container.ccr ...]```

argument | description
--- | ---
-h | print help message
-l | write a log file
[config.yml](https://github.com/sysunite/coins-2-validator/blob/develop/doc/config-yml.md) | the configuration of the validation run
*container.ccr ...* | zero or more containers that override the path in the config.yml

If the command was able to generate a report the **exit code** is ```0```. In all other cases the **exit code** is ```1```. Add the ```-l``` argument and read the log for more information.

Read about the [config.yml](https://github.com/sysunite/coins-2-validator/blob/develop/doc/config-yml.md) for a description what the validator does and how it can be configured.

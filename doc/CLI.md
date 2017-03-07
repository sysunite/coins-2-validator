A validation run needs a configuration file (config.yml) and a profile (validation.profile).

```bash
coins-validation validate -c config.yml -p validation.profile container.ccr
```

The config.yml can specify the validation plan in great detail. To have some more information about the container, this can be executed first:

```bash
coins-validation describe container.ccr
```
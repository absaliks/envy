# envy

## Overview

The purpose of the project is to simplify creation and maintenance of multi-environment local run configurations for
Java applications, at the same time improving security by not storing secrets (e.g. api keys and passwords) on the disk.

## Quickstart

### Envy 

#### Define environment tree:

```yaml
dev:
  - name: dev-eu
    k8s-context: nonprod.eu-west-1
  - name: dev-us
    k8s-context: nonprod.us-east-1
prod:
  - prod-eu:
    - name: prod-eu-west
      k8s-context: prod.eu-west-1
    - name: prod-eu-central
      k8s-context: prod.eu-central-1 
```

#### Define properties:

Property values can be env-agnostic, one per environment or can target group of environments:
```yaml
my-email: john.doe@email.test                    # env-agnostic (same value applied for all envs)
redis-cache: http://localhost:6379
email-service:
  base-url:
    prod-eu-west: https://email-service.prod-eu.com  # env-specific
    prod-eu-east: https://email-service.prod-eu.com
    dev: https://email-service.dev.com               # group of environments (value applied for dev-us & dev-eu)
  api-key: ${k8s-secret:email-service-secrets:api-key}  # value can be extracted from k8s secrets
```

#### Build

Run `mvn package`. It converts a YAML file into properties files, one per environment; removes env postfixes, since the
files are env-specific; and packages the property files inside the Java agent JAR.
```properties
# dev-eu.properties
my-email=john.doe@email.test
redis-cache=http://localhost:6379
email-service.base-url=https://email-service.dev.com
email-service.api-key=${k8s-secret:email-service-secrets:api-key}
```

### Your service

#### Attach Java agent

In the run configuration via VM options: `-javaagent:/.../envy-agent.jar`

#### Setup property mappings service ~ envy

In the run configuration, using the "Override configuration properties" feature.

| Name                              | Value                          |
|-----------------------------------|--------------------------------|
| spring.data.redis.host            | ${envy.redis-cache}            |
| webclients.email-service.base-url | ${envy.email-service.base-url} |
| email.to                          | ${envy.my-email}               |

[//]: # (TODO: add info about setting environment)

## How it works

to be continued 

[//]: # (TODO: finish me)
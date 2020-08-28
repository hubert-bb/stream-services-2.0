![](logo.svg)

[![Jenkins][jenkins-image]][jenkins-url] 
[![Artifacts][artifacts-image]][artifacts-url] 
[![Pull Requests][pr-image]][pr-url] 
[![Slack][slack-image]][slack-url] 
[![Contributors][contributors-image]][contributors-url] 
[![Sonar][sonar-image]][sonar-url] 
[![Coverage][coverage-image]][coverage-url] 
[![Dept][dept-image]][dept-url] 
[![Violations][violations-image]][violations-url] 
[![Maint][maint-image]][maint-url] 

## Stream SDK 1.0
The Stream SDK accelerate the development of Reactive Java components for the Backbase Platform. It contain starters to quickly build (reactive) micro services which can be used in Spring Cloud Data Flow or Stream All-In-One services. 


## Documentation
The Stream SDK contains the Stream DBS Web Client which you can use to interact with Backbase DBS Presentation services using Oauth 2.0 client credentials. 
There are also several starters to aid you in development of Backbase Stream components. 

### Stream DBS Web Client
The Stream DBS Web Client is a Reactive Web Client that you can use to interact with Backbase DSB using HTTP communication. 
It is preconfigured to use OpenID Connect 2.0 client credentials configured in the application.yml

To use the Stream DBS Web Client, add the following dependency to your project:

```xml
<dependency>
    <groupId>com.backbase.stream</groupId>
    <artifactId>stream-dbs-web-client</artifactId>
    <version></version>
</dependency>
```

A typical use of the `WebClient` bean is to inject it in the creation of Reactive OpenAPI clients generated by the OpenAPI plugin:

```
    @Bean
    protected com.backbase.dbs.user.presentation.service.ApiClient usersApiClient(WebClient dbsWebClient,
                                                                                  ObjectMapper objectMapper,
                                                                                  DateFormat dateFormat) {
        com.backbase.dbs.user.presentation.service.ApiClient apiClient = new com.backbase.dbs.user.presentation.service.ApiClient(
                dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(backbaseStreamDbsConfigurationProperties.getUserPresentationBaseUrl());
        return apiClient;
    }

    @Bean
    protected com.backbase.dbs.accessgroup.presentation.service.ApiClient accessGroupApiClient(WebClient dbsWebClient,
                                                                                               ObjectMapper objectMapper,
                                                                                               DateFormat dateFormat) {
        com.backbase.dbs.accessgroup.presentation.service.ApiClient apiClient = new com.backbase.dbs.accessgroup.presentation.service.ApiClient(
                dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(backbaseStreamDbsConfigurationProperties.getAccessGroupPresentationBaseUrl());
        return apiClient;
    }
```

The `dbsWebClient`, `objectMapper` and `dateFormat` are defined by the `stream-dbs-web-client`. 
The `objectMapper` is a preconfigured instance of a Jackson JSON Object Mapper with correct modules set for handling dates. 
Teh `dateFormat` is a helper bean that defines the correct format to aid serialisation and deserialisation of Date and Date Time objects. 


### Stream All-In-One Starter
The Stream All-In-One (AIO) Starter is used to create Stream services that work standalone on environment without a message broker.

AIO Services are responsible for responding to DBS Events, Connecting to MQ or any other way you would like to integrate with a remote environment and can then use the Stream Libraries to work with DBS
You would not split up these responsibilities in separate micro services, but would put it all in a single service. This is usefull when you don't have a dynamic runtime environment available, but you would like to use the Stream Services.  

An AIO Service is comparable with a Direct Integration Route:
- AIO Allows Reactive Programming from [Project Reactor](https://projectreactor.io/)
- Does not include Apache Camel
- Have an low memory footprint
- Supports Java 11
- Packages into Distroless docker container

Example Stream AIO pom:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.backbase.stream</groupId>
        <artifactId>stream-aio-starter-parent</artifactId>
        <version>1.0.5-SNAPSHOT</version>
        <relativePath/>
    </parent>

    <groupId>com.backbase.stream.dbs.transactions</groupId>
    <artifactId>stream-generate-ingest-transactions-on-login-reactive</artifactId>
    <packaging>jar</packaging>
    <name>Backbase Stream :: DBS :: Transaction Ingestion Client Demo</name>

    <dependencies>
        <dependency>
            <groupId>com.backbase.stream</groupId>
            <artifactId>stream-cursor</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.backbase.stream</groupId>
            <artifactId>stream-access-control</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.backbase.stream</groupId>
            <artifactId>stream-transactions</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.backbase.stream</groupId>
            <artifactId>transaction-generator</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

You can then create a micro service that would for instance listen to the login event, gather all entitlements, generate transactions and ingest the transactions as shown in the example below:

```java
package com.backbase.stream;

import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.stream.cursor.model.IngestionCursor;
import com.backbase.stream.cursor.events.AuditLoginEventListener;
import com.backbase.stream.generator.TransactionsDataGenerator;
import com.backbase.stream.service.EntitlementsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class GenerateIngestTransactionOnLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenerateIngestTransactionOnLoginApplication.class, args);
    }

    @Configuration
    public class GenerateIngestTransactionOnLoginConfiguration {
        @Bean
        public AuditLoginEventListener auditLoginEventListener(EntitlementsService entitlementsService, ReactiveTransactionService reactiveTransactionService) {
            return new AuditLoginEventListener(entitlementsService, reactiveTransactionService);
        }
    }

    @Component
    @AllArgsConstructor
    @Slf4j
    class IngestOnLoginRunner implements ApplicationRunner {

        private final AuditLoginEventListener auditLoginEventListener;
        private final ReactiveTransactionService reactiveTransactionService;

        public void run(ApplicationArguments args) {
            Hooks.onOperatorDebug();
            Flux.from(auditLoginEventListener.getLoginEventProcessor())
                    .map(this::generateRandomTransactionForIngestionCursor)
                    .flatMap(reactiveTransactionService::processTransactions)
                    .collectList()
                    .doOnError(throwable -> {
                        log.error("Failed to ingest: ", throwable);
                    })
                    .subscribe(ids -> log.info("Ingested Transactions: {}", ids));
        }

        private Flux<TransactionItemPost> generateRandomTransactionForIngestionCursor(IngestionCursor ingestionCursor) {
            return Flux.fromIterable(TransactionsDataGenerator.generate(ingestionCursor, 10, 40));
        }
    }
}
```

A working example can be found in stream-aio-generate-ingest-transactions-on-login

## Release

To deploy a new version of Stream SDK on repo:

Set version:

```shell script
./set-version.sh 1.4.0
```
To deploy on `repo` , you must have deploy rights to the `backbase-stream-releases` repository on repo.backbase.com

```shell script
mvn deploy -DaltDeploymentRepository=repo::default::https://repo.backbase.com/backbase-stream-releases/
```


## Contributing
Please open an [issue][issue-url] first to discuss potential changes/additions/bugs.
Want to contribute to the code? Please take a moment to read our [Contributing](CONTRIBUTING.md) guide to learn about our development process.

## Core Maintainers
Thanks to all the people who contribute, and a special thanks to the core team.

[![Contributor](https://secure.gravatar.com/avatar/0085ce875dd7e68b7ec5ef37c38e1fba.jpg?s=192&d=mm)](https://stash.backbase.com/users/bartv)

[jenkins-url]: https://jenkins.backbase.eu/job/Technology%20Prototyping/job/STREAM/job/stream-sdk/
[jenkins-image]: https://shields.backbase.eu/jenkins/build/https/jenkins.backbase.eu/job/Technology%20Prototyping/job/STREAM/job/stream-sdk/job/master

[artifacts-url]: https://artifacts.backbase.com/webapp/#/artifacts/browse/tree/General/staging/com/backbase/stream/stream-sdk
[artifacts-image]: https://shields.backbase.eu/endpoint?url=https://artifacts.backbase.com/webapp/#/artifacts/browse/tree/General/staging/com/backbase/stream/stream-sdk
[pr-url]: https://stash.backbase.com/projects/STREAM/repos/stream-sdk/pull-requests
[pr-image]: https://shields.backbase.eu/endpoint?url=https://stash.backbase.com/projects/STREAM/repos/stream-sdk/pull-requests
[slack-url]: https://slack.com/app_redirect?channel=stream
[slack-image]: https://shields.backbase.eu/static/v1?label=slack&message=stream&color=red
[contributors-url]: https://stash.backbase.com/projects/STREAM/repos/stream-sdk/commits
[contributors-image]: https://shields.backbase.eu/endpoint?url=https://backbase-shields.backbase.eu/shields/bitbucket/v1/contributors/STREAM/stream-sdk
[sonar-url]: http://sonar.backbase.test/dashboard?id=com.backbase.stream%3Astream-sdk
[sonar-image]: https://shields.backbase.eu/sonar/alert_status/com.backbase.stream:stream-sdk:master?server=http%3A%2F%2Fsonar.backbase.test
[coverage-url]: http://sonar.backbase.test/dashboard?id=com.backbase.stream%3Astream-sdk%3Amaster
[coverage-image]: https://shields.backbase.eu/sonar/coverage/com.backbase.stream:stream-sdk:master?server=http%3A%2F%2Fsonar.backbase.test
[dept-url]: http://sonar.backbase.test/dashboard?id=com.backbase.stream%stream-sdk%3Amaster
[dept-image]: https://shields.backbase.eu/sonar/tech_debt/com.backbase.stream:stream-sdk:master?server=http%3A%2F%2Fsonar.backbase.test
[violations-url]: http://sonar.backbase.test/dashboard?id=com.backbase.stream%3Astream-sdk%3Amaster
[violations-image]: https://shields.backbase.eu/sonar/violations/com.backbase.stream:stream-sdk:master?format=long&server=http%3A%2F%2Fsonar.backbase.test
[maint-url]: https://backbase.atlassian.net/projects/MAINT/issues
[maint-image]: https://shields.backbase.eu/endpoint?url=https://backbase-shields.backbase.eu/shields/jira/v1/maint/ips/stream-sdk

[issue-url]: https://backbase.atlassian.net/projects/MAINT/issues
# POC WebFlux-R2DBC H2-Kotlin

Test project for Spring Boot/Kotlin with coroutines integration and R2DBC repositories.

### Base project

[stat.spring.io](https://start.spring.io/#!type=gradle-project&language=kotlin&platformVersion=2.2.1.RELEASE&packaging=jar&jvmVersion=1.8&groupId=net.razvan.poc.spring-boot&artifactId=webflux-r2dbc-kotlin&name=webflux-r2dbc-kotlin&description=Demo%20project%20for%20Spring%20Boot&packageName=net.razvan.poc.spring-boot.webflux-r2dbc-kotlin&dependencies=webflux,data-r2dbc,h2,actuator)
 - gradle (kotlin)
 - kotlin
 - webflux
 - spring-data-r2dbc
 - h2
 - actuator
 
### Extra dependencies

````kotlin
implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.0.0.RELEASE")
testImplementation("io.mockk:mockk:1.9")
````

### Extra remarks
- Had to manual set the h2 version to a previous one as r2dbc-h2 can't handle (yet) the changes in 1.4.200
- Unit tests and Integration tests are including (100% coverage :))
- Upgraded to Gradle 6.0

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/gradle-plugin/reference/html/)
* [Coroutines section of the Spring Framework Documentation](https://docs.spring.io/spring/docs/5.2.1.RELEASE/spring-framework-reference/languages.html#coroutines)
* [Spring Data R2DBC [Experimental]](https://docs.spring.io/spring-data/r2dbc/docs/1.0.x/reference/html/#reference)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/htmlsingle/#production-ready)

### Guides
The following guides illustrate how to use some features concretely:

* [R2DBC example](https://github.com/spring-projects-experimental/spring-boot-r2dbc/tree/master/spring-boot-example-h2)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [R2DBC Homepage](https://r2dbc.io)


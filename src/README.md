# lc-commons

Reusable utilities for Java.
It depends on [Apache commons](https://commons.apache.org/) and add more utilities.

## Configuration

Maven
```xml
<dependency>
  <groupId>net.lecousin.commons</groupId>
  <artifactId>lc-commons</artifactId>
  <version>${project.version}</version>
</dependency>
```

Gradle
```groovy
implementation group: 'net.lecousin.commons', name: 'lc-commons', version: '${project.version}'
```

## lc-commons-test

Utilities for tests.

Maven
```xml
<dependency>
  <groupId>net.lecousin.commons</groupId>
  <artifactId>lc-commons-test</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
```

Gradle
```groovy
testImplementation("ne.lecousin.commons:lc-commons-test:${project.version}")
```

# lc-commons

<a href="https://search.maven.org/artifact/net.lecousin.commons/lc-commons"><img src="https://img.shields.io/maven-central/v/net.lecousin.commons/lc-commons.svg"></a> &nbsp;
<a href="https://www.javadoc.io/doc/net.lecousin.commons/lc-commons/${project.version}"><img src="https://img.shields.io/badge/javadoc-${project.version}-brightgreen.svg"></a> &nbsp;
<a href="https://github.com/lecousin/lc-commons/actions/workflows/maven.yml"><img src="https://github.com/lecousin/lc-commons/actions/workflows/maven.yml/badge.svg"></a> &nbsp;
<br/>
<a href="https://codecov.io/gh/lecousin/lc-commons/branch/master"><img src="https://codecov.io/gh/lecousin/lc-commons/graph/badge.svg?token=KNR9CFV9LY"></a> &nbsp;
<a href="https://sonarcloud.io/summary/new_code?id=lecousin_lc-commons"><img src="https://sonarcloud.io/api/project_badges/measure?project=lecousin_lc-commons&metric=coverage"></a> &nbsp;
<a href="https://sonarcloud.io/summary/new_code?id=lecousin_lc-commons"><img src="https://sonarcloud.io/api/project_badges/measure?project=lecousin_lc-commons&metric=code_smells"></a> &nbsp;
<a href="https://sonarcloud.io/summary/new_code?id=lecousin_lc-commons"><img src="https://sonarcloud.io/api/project_badges/measure?project=lecousin_lc-commons&metric=sqale_rating"></a> &nbsp;


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

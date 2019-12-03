# activiti-build

[![Join Us in Gitter](https://badges.gitter.im/Activiti/Activiti7.svg)](https://gitter.im/Activiti/Activiti7?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status Travis](https://travis-ci.org/Activiti/activiti-build.svg?branch=master)](https://travis-ci.org/Activiti/activiti-build)
[![ASL 2.0](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/Activiti/activiti-build/blob/master/LICENSE.txt)
[![CLA](https://cla-assistant.io/readme/badge/Activiti/activiti-build)](https://cla-assistant.io/Activiti/activiti-build)
[![Known Vulnerabilities](https://snyk.io/test/github/Activiti/activiti-build/badge.svg)](https://snyk.io/test/github/Activiti/activiti-build)
[![security status](https://www.meterian.com/badge/gh/Activiti/activiti-build/security)](https://www.meterian.com/report/gh/Activiti/activiti-build)
[![stability status](https://www.meterian.com/badge/gh/Activiti/activiti-build/stability)](https://www.meterian.com/report/gh/Activiti/activiti-build)

BOMs (Bill of Materials) and scripts for all repos related with Activiti


## activiti-parent
This pom.xml file handle all 3rd Party dependencies that are shared by Activiti modules. Spring version is defined here.

## activiti-dependencies
This BOM (Bill Of Materials) allow you to easily add the following section to your maven pom.xml file:

```
<dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.activiti.cloud</groupId>
        <artifactId>activiti-dependencies</artifactId>
        <version>${activiti.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  ```
To automatically manage all activiti artifact versions and make sure that you don't mix different versions from different releases of these components.
Then you can include any activiti module by just adding the GroupId and ArtifactId

For example:
```
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>activiti-engine</artifactId>
</dependency>
```

Versions are going to be handled by maven.

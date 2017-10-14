# activiti-build
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
        <version>${version.activiti}</version>
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

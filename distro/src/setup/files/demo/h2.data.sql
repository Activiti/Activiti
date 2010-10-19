insert into ACT_ID_GROUP values ('admin',       1, 'System administrator', 'security-role');
insert into ACT_ID_GROUP values ('user',        1, 'User', 'security-role');
insert into ACT_ID_GROUP values ('manager',     1, 'Manager', 'security-role');
insert into ACT_ID_GROUP values ('management',  1, 'Management',  'assignment');
insert into ACT_ID_GROUP values ('accountancy', 1, 'Accountancy', 'assignment');
insert into ACT_ID_GROUP values ('engineering', 1, 'Engineering', 'assignment');
insert into ACT_ID_GROUP values ('sales',       1, 'Sales', 'assignment');

insert into ACT_ID_USER values ('kermit', 1, 'Kermit', 'the Frog', 'kermit@localhost', 'kermit');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'admin');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'manager');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'management');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'accountancy');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'engineering');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'sales');

insert into ACT_ID_USER values ('fozzie', 1, 'Fozzie', 'Bear', 'fozzie@localhost', 'fozzie');
insert into ACT_ID_MEMBERSHIP values ('fozzie', 'user');
insert into ACT_ID_MEMBERSHIP values ('fozzie', 'accountancy');

insert into ACT_ID_USER values ('gonzo', 1, 'Gonzo', 'the Great', 'gonzo@localhost', 'gonzo');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'manager');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'management');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'accountancy');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'sales');

update ACT_GE_PROPERTY
set VALUE_ = '10'
where NAME_ = 'next.dbid';


insert into ACT_CY_CONFIG values ('kermit', '<org.activiti.cycle.impl.conf.ConfigurationContainer>
  <name>kermit</name>
  <linkedConnectors>
	  <org.activiti.cycle.impl.connector.demo.DemoConnectorConfiguration> 
      <id>Demo</id> 
      <name>Demo</name> 
    </org.activiti.cycle.impl.connector.demo.DemoConnectorConfiguration>
    <org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>
      <id>Activiti</id>
      <name>Activiti Modeler</name>
      <credentialsSaved>false</credentialsSaved>
      <signavioBaseUrl>@activiti.modeler.base.url@</signavioBaseUrl>
      <loginRequired>false</loginRequired>
    </org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>
    <org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>
      <id>Workspace</id>
      <name>Eclipse Workspace (File System)</name>
      <baseFilePath>@cycle.base.file.path@</baseFilePath>
    </org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>
  </linkedConnectors>
  <parentContainers/>
</org.activiti.cycle.impl.conf.ConfigurationContainer>', 1);
insert into ACT_CY_CONFIG values ('fozzie', '<org.activiti.cycle.impl.conf.ConfigurationContainer>
  <name>fozzie</name>
  <linkedConnectors>
    <org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>
      <id>Activiti</id>
      <name>Activiti Modeler</name>
      <credentialsSaved>false</credentialsSaved>
      <signavioBaseUrl>@activiti.modeler.base.url@</signavioBaseUrl>
      <loginRequired>false</loginRequired>
    </org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>
    <org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>
      <id>Workspace</id>
      <name>Eclipse Workspace (File System)</name>
      <baseFilePath>@cycle.base.file.path@</baseFilePath>
    </org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>
  </linkedConnectors>
  <parentContainers/>
</org.activiti.cycle.impl.conf.ConfigurationContainer>', 1);
insert into ACT_CY_CONFIG values ('gonzo', '<org.activiti.cycle.impl.conf.ConfigurationContainer>
  <name>gonzo</name>
  <linkedConnectors>
    <org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>
      <id>Activiti</id>
      <name>Activiti Modeler</name>
      <credentialsSaved>false</credentialsSaved>
      <signavioBaseUrl>http://localhost:8080/activiti-modeler/</signavioBaseUrl>
      <loginRequired>false</loginRequired>
    </org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>
    <org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>
      <id>Workspace</id>
      <name>Eclipse Workspace (File System)</name>
      <baseFilePath>@cycle.base.file.path@</baseFilePath>
    </org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>
  </linkedConnectors>
  <parentContainers/>
</org.activiti.cycle.impl.conf.ConfigurationContainer>', 1);
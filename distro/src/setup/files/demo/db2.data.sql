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

insert into ACT_CY_CONN_CONFIG values ('1', 
				'org.activiti.cycle.impl.connector.fs.FileSystemConnector',
				'Eclipse Workspace (File System)',
				'Workspace',
				'kermit', '',
				'<map><entry><string>basePath</string><string>@cycle.base.file.path@</string></entry></map>');
insert into ACT_CY_CONN_CONFIG values ('2', 
				'org.activiti.cycle.impl.connector.signavio.SignavioConnector',
				'Activiti Modeler',
				'Activiti',
				'kermit', '',
				'<map>
					<entry><string>signavioBaseUrl</string><string>@activiti.modeler.base.url@</string></entry>
					<entry><string>loginRequired</string><boolean>false</boolean></entry>
				</map>');

insert into ACT_CY_CONN_CONFIG values ('3', 
				'org.activiti.cycle.impl.connector.fs.FileSystemConnector',
				'Eclipse Workspace (File System)',
				'Workspace',
				'fozzie', '',
				'<map><entry><string>basePath</string><string>@cycle.base.file.path@</string></entry></map>');
insert into ACT_CY_CONN_CONFIG values ('4', 
				'org.activiti.cycle.impl.connector.signavio.SignavioConnector',
				'Activiti Modeler',
				'Activiti',
				'fozzie', '',
				'<map>
					<entry><string>signavioBaseUrl</string><string>@activiti.modeler.base.url@</string></entry>
					<entry><string>loginRequired</string><boolean>false</boolean></entry>
				</map>');

insert into ACT_CY_CONN_CONFIG values ('5', 
				'org.activiti.cycle.impl.connector.fs.FileSystemConnector',
				'Eclipse Workspace (File System)',
				'Workspace',
				'gonzo', '',
				'<map><entry><string>basePath</string><string>@cycle.base.file.path@</string></entry></map>');
insert into ACT_CY_CONN_CONFIG values ('6', 
				'org.activiti.cycle.impl.connector.signavio.SignavioConnector',
				'Activiti Modeler',
				'Activiti',
				'gonzo', '',
				'<map>
					<entry><string>signavioBaseUrl</string><string>@activiti.modeler.base.url@</string></entry>
					<entry><string>loginRequired</string><boolean>false</boolean></entry>
				</map>');

insert  into  ACT_CY_CONFIG values ('1', 
									'processSolutionTemplates', 
									'default',
									'<processSolutionTemplate> 
										<vFolder type="Management" name="Management" connectorId="Workspace" referencedNodeId="/" /> 
										<vFolder type="Processes" name="Processes" connectorId="Activiti" referencedNodeId="/root-directory" /> 
										<vFolder type="Requirements" name="Requirements" connectorId="Workspace" referencedNodeId="/" /> 
										<vFolder type="Implementation" name="Implementation" connectorId="Workspace" referencedNodeId="/" /> 
									</processSolutionTemplate>');	

insert 	into ACT_CY_PROCESS_SOLUTION values ('ps1',
											 'Easy Bugfiling Process',
											 'IN_SPECIFICATION');
											 
insert into ACT_CY_V_FOLDER values('ps1Management',
								   'Management',
								   'Workspace',
								   '/EasyBugFilingProcess/Management',
								   'ps1',
								   'Management');
insert into ACT_CY_V_FOLDER values('ps1Requirements',
								   'Requirements',
								   'Workspace',
								   '/EasyBugFilingProcess/Requirements',
								   'ps1',
								   'Requirements');
insert into ACT_CY_V_FOLDER values('ps1Implementation',
								   'Implementation',
								   'Workspace',
								   '/EasyBugFilingProcess/Implementation',
								   'ps1',
								   'Implementation');
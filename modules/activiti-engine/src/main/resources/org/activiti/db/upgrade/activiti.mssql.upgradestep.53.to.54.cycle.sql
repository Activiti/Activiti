if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_CY_CONFIG') drop table ACT_CY_CONFIG;

create table ACT_CY_CONN_CONFIG (
	ID_ nvarchar(255),
	PLUGIN_ID_ nvarchar(255),
	INSTANCE_NAME_ nvarchar(255),
	INSTANCE_ID_ nvarchar(255),
	USER_ nvarchar(255),
	GROUP_ nvarchar(255),
	VALUES_ nvarchar(1024),
	primary key (ID_)
);

create table ACT_CY_CONFIG (
	ID_ nvarchar(255),
	GROUP_ nvarchar(255),
	KEY_ nvarchar(255),
	VALUE_ clob,
	primary key (ID_)
);

create table ACT_CY_PROCESS_SOLUTION (
	ID_ nvarchar(255) NOT NULL,
	LABEL_ nvarchar(255) NOT NULL,
	STATE_ nvarchar(255) NOT NULL,
	primary key(ID_)
);

create table ACT_CY_V_FOLDER (
	ID_ nvarchar(255) NOT NULL,
	LABEL_ nvarchar(255) NOT NULL,
	CONNECTOR_ID_ nvarchar(255) NOT NULL,
	REFERENCED_NODE_ID_ nvarchar(255) NOT NULL,
	PROCESS_SOLUTION_ID_ nvarchar(255) NOT NULL,
	TYPE_ nvarchar(255) NOT NULL,
	primary key(ID_)
);
alter table ACT_CY_V_FOLDER 
    add constraint FK_CY_PROCESS_SOLUTION_ID 
    foreign key (PROCESS_SOLUTION_ID_) 
    references ACT_CY_PROCESS_SOLUTION;
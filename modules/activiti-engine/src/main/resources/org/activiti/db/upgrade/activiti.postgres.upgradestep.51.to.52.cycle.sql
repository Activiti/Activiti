create table ACT_CY_CONN_CONFIG (
	ID_ varchar(255) NOT NULL,
	PLUGIN_ID_ varchar(255) NOT NULL,
	INSTANCE_NAME_ varchar(255) NOT NULL, 
	INSTANCE_ID_ varchar(255) NOT NULL,  
	USER_ varchar(255),
	GROUP_ varchar(255),
	VALUES_ text,	
	primary key (ID_)
);

drop table ACT_CY_CONN_CONFIG;

create table ACT_CY_CONFIG (
	ID_ varchar(255) NOT NULL,
	GROUP_ varchar(255) NOT NULL,
	KEY_ varchar(255) NOT NULL,
	VALUE_ text,
	primary key (ID_)
);
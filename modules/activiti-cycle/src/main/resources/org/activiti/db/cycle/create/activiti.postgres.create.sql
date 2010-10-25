create table ACT_CY_CONFIG (
	ID_ varchar(64),
    VALUE_ text,
    REV_ integer,
    primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ varchar(255) AUTO_INCREMENT,
	SOURCE_CONNECTOR_ID_ varchar(255),
	TARGET_CONNECTOR_ID_ varchar(255),	
	SOURCE_ARTIFACT_ID_ varchar(255),
	TARGET_ARTIFACT_ID_ varchar(255),
	SOURCE_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	TARGET_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar(255) DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar(255) DEFAULT NULL,
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_REVISION_ bigint DEFAULT NULL,
	LINK_TYPE_ varchar(255),
	DESCRIPTION_ varchar(255),
	LINKED_BOTH_WAYS_ boolean,
	primary key(ID_)
);

create table ACT_CY_TAG (
	ID_ bigint,
	NAME_ varchar(255),
	ALIAS_ varchar(255),
	primary key(ID_)
);

create table ACT_CY_TAG (
	ID_ varchar(255),
	NAME_ varchar(700),
	CONNECTOR_ID_ varchar(255),
	ARTIFACT_ID_ varchar(550),
	ALIAS_ varchar(255),
	primary key(ID_)	
);
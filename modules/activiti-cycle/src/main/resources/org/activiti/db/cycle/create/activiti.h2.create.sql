create table ACT_CY_CONFIG (
	ID_ varchar,
    VALUE_ clob,
    REV_ integer,
    primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ varchar AUTO_INCREMENT,
	SOURCE_CONNECTOR_ID_ varchar,
	TARGET_CONNECTOR_ID_ varchar,
	SOURCE_ARTIFACT_ID_ varchar,
	TARGET_ARTIFACT_ID_ varchar,
	SOURCE_ELEMENT_ID_ varchar DEFAULT NULL,
	TARGET_ELEMENT_ID_ varchar DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar DEFAULT NULL,
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_REVISION_ bigint DEFAULT NULL,
	LINK_TYPE_ varchar,
	DESCRIPTION_ varchar,
	LINKED_BOTH_WAYS_ boolean,
	primary key(ID_)
);

create table ACT_CY_TAG (
	ID_ varchar,
	NAME_ varchar,
	CONNECTOR_ID_ varchar,
	ARTIFACT_ID_ varchar,
	ALIAS_ varchar,
	primary key(ID_)	
);
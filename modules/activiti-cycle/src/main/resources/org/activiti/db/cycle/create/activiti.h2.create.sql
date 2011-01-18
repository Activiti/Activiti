create table ACT_CY_CONN_CONFIG (
	ID_ varchar NOT NULL,
	PLUGIN_ID_ varchar NOT NULL,
	INSTANCE_NAME_ varchar NOT NULL, 
	INSTANCE_ID_ varchar NOT NULL,  
	USER_ varchar,
	GROUP_ varchar,
	VALUES_ clob,	
	primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ varchar NOT NULL,
	SOURCE_CONNECTOR_ID_ varchar,
	SOURCE_ARTIFACT_ID_ varchar,
	SOURCE_ELEMENT_ID_ varchar DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar DEFAULT NULL,
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_CONNECTOR_ID_ varchar,
	TARGET_ARTIFACT_ID_ varchar,
	TARGET_ELEMENT_ID_ varchar DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar DEFAULT NULL,
	TARGET_REVISION_ bigint DEFAULT NULL,
	LINK_TYPE_ varchar,
	COMMENT_ varchar,
	LINKED_BOTH_WAYS_ boolean,
	primary key(ID_)
);

create table ACT_CY_PEOPLE_LINK (
	ID_ varchar NOT NULL,
	SOURCE_CONNECTOR_ID_ varchar,
	SOURCE_ARTIFACT_ID_ varchar,
	SOURCE_REVISION_ bigint DEFAULT NULL,
	USER_ID_ varchar,
	GROUP_ID_ varchar,
	LINK_TYPE_ varchar,
	COMMENT_ varchar,
	primary key(ID_)
);

create table ACT_CY_TAG (
	ID_ varchar NOT NULL,
	NAME_ varchar NOT NULL,
	CONNECTOR_ID_ varchar NOT NULL,
	ARTIFACT_ID_ varchar NOT NULL,
	ALIAS_ varchar DEFAULT NULL,
	primary key(ID_)	
);

create table ACT_CY_COMMENT (
	ID_ varchar NOT NULL,
	CONNECTOR_ID_ varchar NOT NULL,
	NODE_ID_ varchar NOT NULL,
	ELEMENT_ID_ varchar DEFAULT NULL,
	CONTENT_ varchar NOT NULL,
	AUTHOR_ varchar,
	DATE_ timestamp NOT NULL,
	ANSWERED_COMMENT_ID_ varchar DEFAULT NULL,
	primary key(ID_)
);
 
create index ACT_CY_IDX_COMMENT on ACT_CY_COMMENT(ANSWERED_COMMENT_ID_);
alter table ACT_CY_COMMENT 
    add constraint FK_CY_COMMENT_COMMENT 
    foreign key (ANSWERED_COMMENT_ID_) 
    references ACT_CY_COMMENT;

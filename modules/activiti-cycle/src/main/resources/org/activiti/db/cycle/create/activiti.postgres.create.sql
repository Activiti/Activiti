create table ACT_CY_CONFIG (
	ID_ varchar(64),
    VALUE_ text,
    REV_ integer,
    primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ varchar(255) NOT NULL,
	SOURCE_CONNECTOR_ID_ varchar(255),
	SOURCE_ARTIFACT_ID_ varchar(550),
	SOURCE_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar(255) DEFAULT NULL,
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_CONNECTOR_ID_ varchar(255),	
	TARGET_ARTIFACT_ID_ varchar(550),
	TARGET_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar(255) DEFAULT NULL,
	TARGET_REVISION_ bigint DEFAULT NULL,
	LINK_TYPE_ varchar(255),
	COMMENT_ varchar(1000),
	LINKED_BOTH_WAYS_ boolean,
	primary key(ID_)
);

create table ACT_CY_PEOPLE_LINK (
	ID_ varchar(255) NOT NULL,
	SOURCE_CONNECTOR_ID_ varchar(255),
	SOURCE_ARTIFACT_ID_ varchar(550),
	SOURCE_REVISION_ bigint DEFAULT NULL,
	USER_ID_ varchar(255),
	GROUP_ID_ varchar(255),
	LINK_TYPE_ varchar(255),
	COMMENT_ varchar(1000),
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

create table ACT_CY_COMMENT (
	ID_ varchar(255) NOT NULL,
	CONNECTOR_ID_ varchar(255) NOT NULL,
	NODE_ID_ varchar(550) NOT NULL,
	ELEMENT_ID_ varchar(255) DEFAULT NULL,
	CONTENT_ varchar(5000) NOT NULL,
	AUTHOR_ varchar(255),
	DATE_ timestamp NOT NULL,
	ANSWERED_COMMENT_ID_ varchar(255) DEFAULT NULL,
	primary key(ID_)
);

create index ACT_CY_IDX_COMMENT on ACT_CY_COMMENT(ANSWERED_COMMENT_ID_);
alter table ACT_CY_COMMENT 
    add constraint FK_CY_COMMENT_COMMENT 
    foreign key (ANSWERED_COMMENT_ID_) 
    references ACT_CY_COMMENT (ID_);
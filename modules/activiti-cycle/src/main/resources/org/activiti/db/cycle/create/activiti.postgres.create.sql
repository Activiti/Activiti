create table ACT_CY_CONFIG (
	ID_ varchar(64),
    VALUE_ text,
    REV_ integer,
    primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ bigint,
	SOURCE_ARTIFACT_ID_ varchar(255),
	SOURCE_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar(255),
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_ARTIFACT_ID_ varchar(255),
	TARGET_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar(255),
	TARGET_REVISION_ bigint DEFAULT NULL,
	LINK_TYPE_ varchar(255),
	DESCRIPTION_ varchar(255),
	LINKED_BOTH_WAYS_ boolean,
	primary key (ID_)
);

create table ACT_CY_TAG (
	ID_ bigint,
	NAME_ varchar(255),
	ALIAS_ varchar(255),
	primary key(ID_)
);
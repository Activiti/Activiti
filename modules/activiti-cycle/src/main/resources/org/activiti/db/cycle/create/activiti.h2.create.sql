create table CYCLE_CONFIG (
	ID_ varchar,
    VALUE_ varchar,
    REV_ integer,
    primary key (ID_)
);

create table CYCLE_LINK (
	ID_ bigint,
	SOURCE_ARTIFACT_ID_ varchar,
	SOURCE_ELEMENT_ID_ varchar DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar,
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_ARTIFACT_ID_ varchar,
	TARGET_ELEMENT_ID_ varchar DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar,
	TARGET_REVISION_ bigint DEFAULT NULL,
	LINK_TYPE_ varchar,
	DESCRIPTION_ varchar,
	LINKED_BOTH_WAYS_ boolean,
	primary key (ID_)
);

create table CYCLE_TAG (
	ID_ bigint,
	NAME_ varchar,
	ALIAS_ varchar,
	primary key(ID_)
)
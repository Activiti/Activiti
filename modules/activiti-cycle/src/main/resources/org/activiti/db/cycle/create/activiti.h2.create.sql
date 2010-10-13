create table ACT_CY_CONFIG (
	ID_ varchar,
    VALUE_ clob,
    REV_ integer,
    primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ varchar,
	TARGET_ELEMENT_ID_ varchar DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar,
	TARGET_REVISION_ bigint DEFAULT NULL,
	SOURCE_ARTIFACT_ID_ varchar,
	LINK_TYPE_ varchar,
	DESCRIPTION_ varchar,
	LINKED_BOTH_WAYS_ boolean,
	primary key(ID_)
);

create table ACT_CY_ARTIFACT (
	ID_ varchar,
	SOURCE_ELEMENT_ID_ varchar DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar,
	primary key (ID_)
);

create table ACT_CY_ARTIFACT_REVISION (
    ID_ varchar auto_increment,
    ARTIFACT_ID_ varchar,
    REVISION_ bigint,
    primary key (ID_)
);

create table ACT_CY_TAG (
	ID_ bigint,
	NAME_ varchar,
	ALIAS_ varchar,
	primary key(ID_)
);

alter table ACT_CY_LINK
    add constraint FK_LINK_ARTIFACT
    foreign key (SOURCE_ARTIFACT_ID_)
    references ACT_CY_ARTIFACT (ID_);

alter table ACT_CY_ARTIFACT_REVISION
    add constraint FK_REVISION_ARTIFACT
    foreign key (ARTIFACT_ID_)
    references ACT_CY_ARTIFACT (ID_);

create table ACT_CY_CONFIG (
	ID_ varchar(64),
    VALUE_ text,
    REV_ integer,
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_CY_LINK (
	ID_ varchar(255),
	TARGET_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	TARGET_ELEMENT_NAME_ varchar(255),
	TARGET_REVISION_ bigint DEFAULT NULL,
	SOURCE_ARTIFACT_ID_ varchar(255),
	LINK_TYPE_ varchar(255),
	DESCRIPTION_ varchar(255),
	LINKED_BOTH_WAYS_ boolean,
	primary key(ID_)
)TYPE=InnoDB;

create table ACT_CY_ARTIFACT (
	ID_ varchar(255),
	SOURCE_ELEMENT_ID_ varchar(255) DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ varchar(255),
	primary key (ID_)
)TYPE=InnoDB;

create table ACT_CY_ARTIFACT_REVISION (
    ID_ varchar(255) auto_increment,
    ARTIFACT_ID_ varchar(255),
    REVISION_ bigint,
    primary key (ID_)
)TYPE=InnoDB;

create table ACT_CY_TAG (
	ID_ bigint,
	NAME_ varchar(255),
	ALIAS_ varchar(255),
	primary key(ID_)
)TYPE=InnoDB;

alter table ACT_CY_LINK
    add constraint FK_LINK_ARTIFACT
    foreign key (SOURCE_ARTIFACT_ID_)
    references ACT_CY_ARTIFACT (ID_);

alter table ACT_CY_ARTIFACT_REVISION
    add constraint FK_REVISION_ARTIFACT
    foreign key (ARTIFACT_ID_)
    references ACT_CY_ARTIFACT (ID_);
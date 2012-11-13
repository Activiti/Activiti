create table ACT_CY_CONFIG (
	ID_ nvarchar(64),
    VALUE_ nvarchar(1024),
    REV_ int,
    primary key (ID_)
);

create table ACT_CY_LINK (
	ID_ nvarchar(255) NOT NULL,
	SOURCE_CONNECTOR_ID_ nvarchar(255),
	SOURCE_ARTIFACT_ID_ nvarchar(550),
	SOURCE_ELEMENT_ID_ nvarchar(255) DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ nvarchar(255) DEFAULT NULL,
	SOURCE_REVISION_ numeric(19,0) DEFAULT NULL,
	TARGET_CONNECTOR_ID_ nvarchar(255),	
	TARGET_ARTIFACT_ID_ nvarchar(550),
	TARGET_ELEMENT_ID_ nvarchar(255) DEFAULT NULL,
	TARGET_ELEMENT_NAME_ nvarchar(255) DEFAULT NULL,
	TARGET_REVISION_ numeric(19,0) DEFAULT NULL,
	LINK_TYPE_ nvarchar(255) ,
	COMMENT_ nvarchar(1000),
	LINKED_BOTH_WAYS_ bit,
	primary key(ID_)
);

create table ACT_CY_PEOPLE_LINK (
	ID_ nvarchar(255) NOT NULL,
	SOURCE_CONNECTOR_ID_ nvarchar(255),
	SOURCE_ARTIFACT_ID_ nvarchar(550),
	SOURCE_REVISION_ numeric(19,0) DEFAULT NULL,
	USER_ID_ nvarchar(255),
	GROUP_ID_ nvarchar(255),
	LINK_TYPE_ nvarchar(255),
	COMMENT_ nvarchar(1000),
	primary key(ID_)
);

create table ACT_CY_TAG (
	ID_ nvarchar(255),
	NAME_ nvarchar(255),
	CONNECTOR_ID_ nvarchar(255),
	ARTIFACT_ID_ nvarchar(550),
	ALIAS_ nvarchar(255),
	primary key(ID_)	
);

create table ACT_CY_COMMENT (
	ID_ nvarchar(255) NOT NULL,
	CONNECTOR_ID_ nvarchar(255) NOT NULL,
	NODE_ID_ nvarchar(550) NOT NULL,
	ELEMENT_ID_ nvarchar(255) DEFAULT NULL,
	CONTENT_ nvarchar(1024) NOT NULL,
	AUTHOR_ nvarchar(255),
	DATE_ datetime NOT NULL,
	ANSWERED_COMMENT_ID_ nvarchar(255) DEFAULT NULL,
	primary key(ID_)
);

create index ACT_CY_IDX_COMMENT on ACT_CY_COMMENT(ANSWERED_COMMENT_ID_);
alter table ACT_CY_COMMENT 
    add constraint FK_CY_COMMENT_COMMENT 
    foreign key (ANSWERED_COMMENT_ID_) 
    references ACT_CY_COMMENT (ID_);
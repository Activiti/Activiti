create table ACT_CY_CONN_CONFIG (
  ID_ varchar(255) not null,
  PLUGIN_ID_ varchar(255) not null,
  INSTANCE_NAME_ varchar(255) not null, 
  INSTANCE_ID_ varchar(255) not null,  
  USER_ varchar(255),
  GROUP_ varchar(255),
  VALUES_ varchar(1024),  
  primary key (ID_)
);

create table ACT_CY_CONFIG (
  ID_ varchar(255) not null,
  GROUP_ varchar(255) not null,
  KEY_ varchar(255) not null,
  VALUE_ varchar(1024) not null,
  primary key (ID_)
);

create table ACT_CY_LINK (
  ID_ varchar(255) not null,
  SOURCE_CONNECTOR_ID_ varchar(255),
  SOURCE_ARTIFACT_ID_ varchar(550),
  SOURCE_ELEMENT_ID_ varchar(255) default null,
  SOURCE_ELEMENT_NAME_ varchar(255) default null,
  SOURCE_REVISION_ bigint default null,
  TARGET_CONNECTOR_ID_ varchar(255),  
  TARGET_ARTIFACT_ID_ varchar(550),
  TARGET_ELEMENT_ID_ varchar(255) default null,
  TARGET_ELEMENT_NAME_ varchar(255) default null,
  TARGET_REVISION_ bigint default null,
  LINK_TYPE_ varchar(255) ,
  COMMENT_ varchar(1000),
  LINKED_BOTH_WAYS_ smallint check(LINKED_BOTH_WAYS_ in (1,0)),
  primary key(ID_)
);

create table ACT_CY_PEOPLE_LINK (
  ID_ varchar(255) not null,
  SOURCE_CONNECTOR_ID_ varchar(255),
  SOURCE_ARTIFACT_ID_ varchar(550),
  SOURCE_REVISION_ bigint default null,
  USER_ID_ varchar(255),
  GROUP_ID_ varchar(255),
  LINK_TYPE_ varchar(255),
  COMMENT_ varchar(1000),
  primary key(ID_)
);

create table ACT_CY_TAG (
  ID_ varchar(255) not null,
  NAME_ varchar(255),
  CONNECTOR_ID_ varchar(255),
  ARTIFACT_ID_ varchar(550),
  ALIAS_ varchar(255),
  primary key(ID_)  
);

create table ACT_CY_COMMENT (
  ID_ varchar(255) not null,
  CONNECTOR_ID_ varchar(255) not null,
  NODE_ID_ varchar(550) not null,
  ELEMENT_ID_ varchar(255) default null,
  CONTENT_ varchar(1024) not null,
  AUTHOR_ varchar(255),
  DATE_ timestamp not null,
  ANSWERED_COMMENT_ID_ varchar(255) default null,
  primary key(ID_)
);

create index ACT_CY_IDX_COMMENT on ACT_CY_COMMENT(ANSWERED_COMMENT_ID_);
alter table ACT_CY_COMMENT 
    add constraint FK_CY_COMMENT_COMMENT 
    foreign key (ANSWERED_COMMENT_ID_) 
    references ACT_CY_COMMENT (ID_);
    
create table ACT_CY_PROCESS_SOLUTION (
	ID_ varchar(128) NOT NULL,
	LABEL_ varchar(255) NOT NULL,
	STATE_ varchar(32) NOT NULL,
	primary key(ID_)
);

create table ACT_CY_V_FOLDER (
	ID_ varchar(128) NOT NULL,
	LABEL_ v(255) NOT NULL,
	CONNECTOR_ID_ varchar(128) NOT NULL,
	REFERENCED_NODE_ID_ varchar(550) NOT NULL,
	PROCESS_SOLUTION_ID_ varchar(128) NOT NULL,
	TYPE_ varchar(32) NOT NULL,
	primary key(ID_)
);

alter table ACT_CY_V_FOLDER 
    add constraint FK_CY_PROCESS_SOLUTION_ID 
    foreign key (PROCESS_SOLUTION_ID_) 
    references ACT_CY_PROCESS_SOLUTION;

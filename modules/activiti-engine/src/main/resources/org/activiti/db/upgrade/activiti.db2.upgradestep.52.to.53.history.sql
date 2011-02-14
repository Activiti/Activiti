execute java org.activiti.engine.impl.db.upgrade.DbUpgradeStep52To53InsertPropertyHistoryLevel

-- removing not null constraint from ACT_HI_DETAIL.PROC_INST_ID_ and ACT_HI_DETAIL.EXECUTION_ID_

create table ACT_HI_DETAIL_TMP (
Ê Ê ID_ varchar(64) not null,
Ê Ê TYPE_ varchar(255) not null,
Ê Ê PROC_INST_ID_ varchar(64),
Ê Ê EXECUTION_ID_ varchar(64),
Ê Ê TASK_ID_ varchar(64),
Ê Ê ACT_INST_ID_ varchar(64),
Ê Ê NAME_ varchar(255),
Ê Ê VAR_TYPE_ varchar(255),
Ê Ê REV_ integer,
Ê Ê TIME_ timestamp not null,
Ê Ê BYTEARRAY_ID_ varchar(64),
Ê Ê DOUBLE_ double precision,
Ê Ê LONG_ bigint,
Ê Ê TEXT_ varchar(255),
Ê Ê TEXT2_ varchar(255),
Ê Ê primary key (ID_)
);

insert into ACT_HI_DETAIL_TMP
select * from ACT_HI_DETAIL;

drop table ACT_HI_DETAIL;

create table ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    TIME_ timestamp not null,
    NAME_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    VAR_TYPE_ varchar(255),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(255),
    TEXT2_ varchar(255),
    primary key (ID_)
);

create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);

insert into ACT_HI_DETAIL
select * from ACT_HI_DETAIL_TMP;

drop table ACT_HI_DETAIL_TMP;

-- Add column PRIORITY_ to ACT_HI_TASKINST and set to default priority (ACT-484)
alter table ACT_HI_TASKINST add column PRIORITY_ integer;
update ACT_HI_TASKINST set PRIORITY_ = 50;
      

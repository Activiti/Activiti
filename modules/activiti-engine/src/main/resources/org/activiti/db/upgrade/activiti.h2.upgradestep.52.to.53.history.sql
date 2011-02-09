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
Ê Ê DOUBLE_ double,
Ê Ê LONG_ bigint,
Ê Ê TEXT_ varchar(255),
Ê Ê TEXT2_ varchar(255),
Ê Ê primary key (ID_)
);

insert into ACT_HI_DETAIL_TMP
select * from ACT_HI_DETAIL;

drop table ACT_HI_DETAIL;

create table ACT_HI_DETAIL (
Ê Ê ID_ varchar(64) not null,
Ê Ê TYPE_ varchar(255) not null,
Ê Ê TIME_ timestamp not null,
Ê Ê NAME_ varchar(255),
Ê Ê PROC_INST_ID_ varchar(64),
Ê Ê EXECUTION_ID_ varchar(64),
Ê Ê TASK_ID_ varchar(64),
Ê Ê ACT_INST_ID_ varchar(64),
Ê Ê VAR_TYPE_ varchar(255),
Ê Ê REV_ integer,
Ê Ê BYTEARRAY_ID_ varchar(64),
Ê Ê DOUBLE_ double,
Ê Ê LONG_ bigint,
Ê Ê TEXT_ varchar(255),
Ê Ê TEXT2_ varchar(255),
Ê Ê primary key (ID_)
);

insert into ACT_HI_DETAIL
select * from ACT_HI_DETAIL_TMP;

drop table ACT_HI_DETAIL_TMP;

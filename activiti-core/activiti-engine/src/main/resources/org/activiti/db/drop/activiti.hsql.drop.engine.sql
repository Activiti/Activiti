drop table if exists ACT_GE_PROPERTY cascade;
drop table if exists ACT_GE_BYTEARRAY cascade;
drop table if exists ACT_RE_DEPLOYMENT cascade;
drop table if exists ACT_RE_MODEL cascade;
drop table if exists ACT_RU_EXECUTION cascade;
drop table if exists ACT_RU_JOB cascade;
drop table if exists ACT_RU_TIMER_JOB cascade;
drop table if exists ACT_RU_SUSPENDED_JOB cascade;
drop table if exists ACT_RU_DEADLETTER_JOB cascade;
drop table if exists ACT_RE_PROCDEF cascade;
drop table if exists ACT_RU_TASK cascade;
drop table if exists ACT_RU_IDENTITYLINK cascade;
drop table if exists ACT_RU_VARIABLE cascade;
drop table if exists ACT_RU_EVENT_SUBSCR cascade;
drop table if exists ACT_EVT_LOG cascade;
drop table if exists ACT_PROCDEF_INFO cascade;
drop table if exists ACT_RU_INTEGRATION cascade;

drop index if exists ACT_IDX_EXEC_BUSKEY;
drop index if exists ACT_IDX_TASK_CREATE;
drop index if exists ACT_IDX_IDENT_LNK_USER;
drop index if exists ACT_IDX_IDENT_LNK_GROUP;
drop index if exists ACT_IDX_VARIABLE_TASK_ID;
drop index if exists ACT_IDX_EVENT_SUBSCR_CONFIG_;
drop index if exists ACT_IDX_ATHRZ_PROCEDEF;
drop index if exists ACT_IDX_INFO_PROCDEF;

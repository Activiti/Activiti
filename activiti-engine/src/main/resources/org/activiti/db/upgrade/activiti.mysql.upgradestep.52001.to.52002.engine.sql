update ACT_RU_EVENT_SUBSCR set PROC_DEF_ID_ = CONFIGURATION_ where EVENT_TYPE_ = 'message' and PROC_INST_ID_ is null and EXECUTION_ID_ is null;

update ACT_GE_PROPERTY set VALUE_ = '5.20.0.2' where NAME_ = 'schema.version';

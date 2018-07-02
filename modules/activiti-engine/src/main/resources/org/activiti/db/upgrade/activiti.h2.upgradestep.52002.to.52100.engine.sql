update ACT_GE_PROPERTY set VALUE_ = '5.21.0.0' where NAME_ = 'schema.version';

select if (
	exists(
		select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_RU_VARIABLE' 
              and index_name = 'IX_ACT_RU_VARIABLE_TYPE_'
    )
    , 'select ''index IX_ACT_RU_VARIABLE_TYPE_ exists'' _______;'
    , 'create index IX_ACT_RU_VARIABLE_TYPE_ on ACT_RU_VARIABLE(TYPE_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


select if (
	exists(
		select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_RU_VARIABLE' 
              and index_name = 'IX_ACT_RU_VARIABLE_NAME_'
    )
    , 'select ''index IX_ACT_RU_VARIABLE_NAME_ exists'' _______;'
    , 'create index IX_ACT_RU_VARIABLE_NAME_ on ACT_RU_VARIABLE(NAME_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


select if (
	exists(
		select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_RU_JOB' 
              and index_name = 'IX_ACT_RU_JOB_PROCESS_INSTANCE_ID_'
    )
    , 'select ''index IX_ACT_RU_JOB_PROCESS_INSTANCE_ID_ exists'' _______;'
    , 'create index IX_ACT_RU_JOB_PROCESS_INSTANCE_ID_ on ACT_RU_JOB(PROCESS_INSTANCE_ID_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

select if (
	exists(
		select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_HI_VARINST' 
              and index_name = 'IX_ACT_HI_VARINST_VAR_TYPE_'
    )
    , 'select ''index IX_ACT_HI_VARINST_VAR_TYPE_ exists'' _______;'
    , 'create index IX_ACT_HI_VARINST_VAR_TYPE_ on ACT_HI_VARINST(VAR_TYPE_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


select if (
	exists(
		select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_HI_VARINST' 
              and index_name = 'IX_ACT_RU_VARIABLE_TYPE_'
    )
    , 'select ''index IX_ACT_HI_VARINST_NAME_ exists'' _______;'
    , 'create index IX_ACT_HI_VARINST_NAME_ on ACT_HI_VARINST(NAME_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


select if (
    exists(
        select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_HI_COMMENT' 
              and index_name = 'IX_ACT_HI_COMMENT_TASK_ID_'
    )
    , 'select ''index IX_ACT_HI_COMMENT_TASK_ID_ exists'' _______;'
    , 'create index IX_ACT_HI_COMMENT_TASK_ID_ on ACT_HI_COMMENT(TASK_ID_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


select if (
    exists(
        select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_HI_COMMENT' 
              and index_name = 'IX_ACT_HI_COMMENT_TYPE_'
    )
    , 'select ''index IX_ACT_HI_COMMENT_TYPE_ exists'' _______;'
    , 'create index IX_ACT_HI_COMMENT_TYPE_ on ACT_HI_COMMENT(TYPE_)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


select if (
    exists(
        select distinct index_name
        from information_schema.statistics
        where table_schema = 'activiti' and table_name = 'ACT_HI_COMMENT' 
              and index_name = 'IX_ACT_HI_COMMENT_TIME_'
    )
    , 'select ''index IX_ACT_HI_COMMENT_TIME_ exists'' _______;'
    , 'create index IX_ACT_HI_COMMENT_TIME_ on ACT_HI_COMMENT(TIME_ desc)'
) into @a;

PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

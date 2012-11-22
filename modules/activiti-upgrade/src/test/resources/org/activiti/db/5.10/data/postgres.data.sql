insert into ACT_RE_DEPLOYMENT(ID_, NAME_, DEPLOY_TIME_)
    values('1', 'simpleTaskProcess', '2012-11-22 14:24:55');

insert into ACT_GE_BYTEARRAY(ID_, REV_, NAME_, BYTES_, DEPLOYMENT_ID_, GENERATED_)
    values ('2', 1, 'org/activiti/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml', E'\\x3C3F786D6C2076657273696F6E3D22312E302220656E636F64696E673D225554462D38223F3E0A0A3C646566696E6974696F6E732069643D227461736B41737369676E65654578616D706C6522200A20202020202020202020202020786D6C6E733D22687474703A2F2F7777772E6F6D672E6F72672F737065632F42504D4E2F32303130303532342F4D4F44454C220A20202020202020202020202020786D6C6E733A61637469766974693D22687474703A2F2F61637469766974692E6F72672F62706D6E220A202020202020202020202020207461726765744E616D6573706163653D2255706772616465223E0A20200A20203C70726F636573732069643D2273696D706C655461736B50726F63657373223E0A20200A202020203C73746172744576656E742069643D227374617274222F3E0A202020200A202020203C73657175656E6365466C6F772069643D22666C6F77312220736F757263655265663D22737461727422207461726765745265663D2273696D706C655461736B3122202F3E0A0A202020203C757365725461736B2069643D2273696D706C655461736B3122206E616D653D2273696D706C655461736B222061637469766974693A61737369676E65653D226B65726D6974222F3E0A202020200A202020203C73657175656E6365466C6F772069643D22666C6F77322220736F757263655265663D2273696D706C655461736B3122207461726765745265663D2273696D706C655461736B3222202F3E0A202020200A202020203C757365725461736B2069643D2273696D706C655461736B3222206E616D653D2273696D706C655461736B32222061637469766974693A61737369676E65653D227377656469736863686566223E0A2020202020203C646F63756D656E746174696F6E3E73706963792073617563653C2F646F63756D656E746174696F6E3E0A202020203C2F757365725461736B3E0A202020200A202020203C73657175656E6365466C6F772069643D22666C6F77332220736F757263655265663D2273696D706C655461736B3222207461726765745265663D2273696D706C655461736B3322202F3E0A202020200A202020203C757365725461736B2069643D2273696D706C655461736B3322206E616D653D2273696D706C655461736B3322202F3E0A202020200A202020203C73657175656E6365466C6F772069643D22666C6F77342220736F757263655265663D2273696D706C655461736B3322207461726765745265663D22656E6422202F3E0A202020200A202020203C656E644576656E742069643D22656E6422202F3E0A202020200A20203C2F70726F636573733E0A0A3C2F646566696E6974696F6E733E', '1', false);

insert into ACT_RE_PROCDEF(ID_, REV_, CATEGORY_, NAME_, KEY_, VERSION_, DEPLOYMENT_ID_, RESOURCE_NAME_, DGRM_RESOURCE_NAME_, HAS_START_FORM_KEY_, SUSPENSION_STATE_)
    values ('simpleTaskProcess:1:3',
    		1,
            'Upgrade',
            null,
            'simpleTaskProcess', 
            1,
            '1',
            'org/activiti/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml',
            null,
            false,
            1);

insert into ACT_RU_EXECUTION (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, ACT_ID_, IS_ACTIVE_, IS_CONCURRENT_, IS_SCOPE_,IS_EVENT_SCOPE_, PARENT_ID_, SUPER_EXEC_, SUSPENSION_STATE_, CACHED_ENT_STATE_)
    values (
      '4',
      1,
      '4',
      null,
      'simpleTaskProcess:1:3',
      'simpleTask1',
      true,
      false,
      true,
      false,
      null,
      null,
      1,
      2
    );

insert into ACT_HI_PROCINST (
        ID_,
        PROC_INST_ID_,
        BUSINESS_KEY_,
        PROC_DEF_ID_,
        START_TIME_,
        END_TIME_,
        DURATION_,
        START_USER_ID_,
        START_ACT_ID_,
        END_ACT_ID_,
        SUPER_PROCESS_INSTANCE_ID_,
        DELETE_REASON_
      ) values (
        '4',
        '4',
        null,
        'simpleTaskProcess:1:3',
        '2012-11-22 14:24:55',
        null,
        null,
        null,
        'start',
        null,
        null,
        null
      );

insert into ACT_HI_ACTINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        ACT_ID_,
        ACT_NAME_,
        ACT_TYPE_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_
      ) values (
        '5',
        'simpleTaskProcess:1:3',
        '4',
        '4',
        'start',
        null,
        'startEvent',
        null,
        '2012-11-22 14:24:55',
        '2012-11-22 14:24:55',
        5
      );

insert into ACT_HI_ACTINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        ACT_ID_,
        ACT_NAME_,
        ACT_TYPE_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_
      ) values (
        '6',
        'simpleTaskProcess:1:3',
        '4',
        '4',
        'simpleTask1',
        'simpleTask',
        'userTask',
        'kermit',
        '2012-11-22 14:24:55',
        null,
        null
      );

insert into ACT_RU_TASK (ID_, REV_, NAME_, PARENT_TASK_ID_, DESCRIPTION_, PRIORITY_, CREATE_TIME_, OWNER_,
                      ASSIGNEE_, DELEGATION_, EXECUTION_ID_, PROC_INST_ID_, PROC_DEF_ID_, TASK_DEF_KEY_, DUE_DATE_)
    values ('7',
            1,
            'simpleTask',
            null,
            null,
            50,
            '2012-11-22 14:24:55',
            null,
            'kermit',
            null,
            '4',
            '4',
            'simpleTaskProcess:1:3',
            'simpleTask1',
            null
           );

insert into ACT_HI_TASKINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        NAME_,
        PARENT_TASK_ID_,
        DESCRIPTION_,
        OWNER_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_,
        DELETE_REASON_,
        TASK_DEF_KEY_,
        PRIORITY_,
        DUE_DATE_
      ) values (
        '7',
        'simpleTaskProcess:1:3',
        '4',
        '4',
        'simpleTask',
        null,
        null,
        null,
        'kermit',
        '2012-11-22 14:24:55',
        null,
        null,
        null,
        'simpleTask1',
        50,
        null
      );

insert into ACT_HI_ACTINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        ACT_ID_,
        ACT_NAME_,
        ACT_TYPE_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_
      ) values (
        '8',
        'simpleTaskProcess:1:3',
        '4',
        '4',
        'simpleTask2',
        'simpleTask2',
        'userTask',
        'swedishchef',
        '2012-11-22 14:24:55',
        null,
        null
      );

insert into ACT_RU_TASK (ID_, REV_, NAME_, PARENT_TASK_ID_, DESCRIPTION_, PRIORITY_, CREATE_TIME_, OWNER_,
                      ASSIGNEE_, DELEGATION_, EXECUTION_ID_, PROC_INST_ID_, PROC_DEF_ID_, TASK_DEF_KEY_, DUE_DATE_)
    values ('9',
            1,
            'simpleTask2',
            null,
            'spicy sauce',
            50,
            '2012-11-22 14:24:55',
            null,
            'swedishchef',
            null,
            '4',
            '4',
            'simpleTaskProcess:1:3',
            'simpleTask2',
            null
           );

insert into ACT_HI_TASKINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        NAME_,
        PARENT_TASK_ID_,
        DESCRIPTION_,
        OWNER_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_,
        DELETE_REASON_,
        TASK_DEF_KEY_,
        PRIORITY_,
        DUE_DATE_
      ) values (
        '9',
        'simpleTaskProcess:1:3',
        '4',
        '4',
        'simpleTask2',
        null,
        'spicy sauce',
        null,
        'swedishchef',
        '2012-11-22 14:24:55',
        null,
        null,
        null,
        'simpleTask2',
        50,
        null
      );

update ACT_RU_EXECUTION set
      REV_ = 2,
      PROC_DEF_ID_ = 'simpleTaskProcess:1:3',
      ACT_ID_ = 'simpleTask2',
      IS_ACTIVE_ = true,
      IS_CONCURRENT_ = false,
      IS_SCOPE_ = true,
      IS_EVENT_SCOPE_ = false,
      PARENT_ID_ = null,
      SUPER_EXEC_ = null,
      SUSPENSION_STATE_ = 1,
      CACHED_ENT_STATE_ = 2
    where ID_ = '4'
      and REV_ = 1;

update ACT_HI_ACTINST set
      EXECUTION_ID_ = '4',
      ASSIGNEE_ = 'kermit',
      END_TIME_ = '2012-11-22 14:24:55',
      DURATION_ = 109
    where ID_ = '6';

update ACT_HI_TASKINST set
      EXECUTION_ID_ = '4',
      NAME_ = 'simpleTask',
      PARENT_TASK_ID_ = null,
      DESCRIPTION_ = null,
      OWNER_ = null,
      ASSIGNEE_ = 'kermit',
      END_TIME_ = '2012-11-22 14:24:55',
      DURATION_ = 99,
      DELETE_REASON_ = 'completed',
      TASK_DEF_KEY_ = 'simpleTask1',
      PRIORITY_ = 50,
      DUE_DATE_ = null
    where ID_ = '7';

delete from ACT_RU_TASK where ID_ = '7';

insert into ACT_RE_DEPLOYMENT(ID_, NAME_, DEPLOY_TIME_)
    values('10', 'simpleTaskProcess', '2012-11-22 14:24:55');

insert into ACT_GE_BYTEARRAY(ID_, REV_, NAME_, BYTES_, DEPLOYMENT_ID_, GENERATED_)
    values ('11', 1, 'org/activiti/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml', E'\\x3C3F786D6C2076657273696F6E3D22312E302220656E636F64696E673D225554462D38223F3E0A0A3C646566696E6974696F6E732069643D227461736B41737369676E65654578616D706C6522200A20202020202020202020202020786D6C6E733D22687474703A2F2F7777772E6F6D672E6F72672F737065632F42504D4E2F32303130303532342F4D4F44454C220A20202020202020202020202020786D6C6E733A61637469766974693D22687474703A2F2F61637469766974692E6F72672F62706D6E220A202020202020202020202020207461726765744E616D6573706163653D2255706772616465223E0A20200A20203C70726F636573732069643D227461736B57697468457865637574696F6E5661726961626C657350726F63657373223E0A20200A202020203C73746172744576656E742069643D227374617274222F3E0A202020200A202020203C73657175656E6365466C6F772069643D22666C6F77312220736F757263655265663D22737461727422207461726765745265663D227461736B57697468457865637574696F6E5661726961626C657322202F3E0A0A202020203C757365725461736B2069643D227461736B57697468457865637574696F6E5661726961626C657322206E616D653D227461736B57697468457865637574696F6E5661726961626C657322202F3E0A202020200A202020203C73657175656E6365466C6F772069643D22666C6F77322220736F757263655265663D227461736B57697468457865637574696F6E5661726961626C657322207461726765745265663D22656E6422202F3E0A202020200A202020203C656E644576656E742069643D22656E6422202F3E0A202020200A20203C2F70726F636573733E0A0A3C2F646566696E6974696F6E733E', '10', false);

insert into ACT_RE_PROCDEF(ID_, REV_, CATEGORY_, NAME_, KEY_, VERSION_, DEPLOYMENT_ID_, RESOURCE_NAME_, DGRM_RESOURCE_NAME_, HAS_START_FORM_KEY_, SUSPENSION_STATE_)
    values ('taskWithExecutionVariablesProcess:1:12',
    		1,
            'Upgrade',
            null,
            'taskWithExecutionVariablesProcess', 
            1,
            '10',
            'org/activiti/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml',
            null,
            false,
            1);

insert into ACT_RU_EXECUTION (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, ACT_ID_, IS_ACTIVE_, IS_CONCURRENT_, IS_SCOPE_,IS_EVENT_SCOPE_, PARENT_ID_, SUPER_EXEC_, SUSPENSION_STATE_, CACHED_ENT_STATE_)
    values (
      '13',
      1,
      '13',
      null,
      'taskWithExecutionVariablesProcess:1:12',
      'taskWithExecutionVariables',
      true,
      false,
      true,
      false,
      null,
      null,
      1,
      2
    );

insert into ACT_HI_PROCINST (
        ID_,
        PROC_INST_ID_,
        BUSINESS_KEY_,
        PROC_DEF_ID_,
        START_TIME_,
        END_TIME_,
        DURATION_,
        START_USER_ID_,
        START_ACT_ID_,
        END_ACT_ID_,
        SUPER_PROCESS_INSTANCE_ID_,
        DELETE_REASON_
      ) values (
        '13',
        '13',
        null,
        'taskWithExecutionVariablesProcess:1:12',
        '2012-11-22 14:24:55',
        null,
        null,
        null,
        'start',
        null,
        null,
        null
      );

insert into ACT_HI_ACTINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        ACT_ID_,
        ACT_NAME_,
        ACT_TYPE_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_
      ) values (
        '14',
        'taskWithExecutionVariablesProcess:1:12',
        '13',
        '13',
        'start',
        null,
        'startEvent',
        null,
        '2012-11-22 14:24:55',
        '2012-11-22 14:24:55',
        0
      );

insert into ACT_RU_VARIABLE (ID_, REV_, TYPE_, NAME_, PROC_INST_ID_, EXECUTION_ID_, TASK_ID_, BYTEARRAY_ID_, DOUBLE_, LONG_ , TEXT_, TEXT2_)
    values (
	    '15',
	    1,
	    'string',
	    'player',
      '13',
	    '13',
      null,
	    null,
	    null,
	    null,
	    'gonzo',
	    null
    );

insert into ACT_HI_DETAIL (ID_, TYPE_, PROC_INST_ID_, EXECUTION_ID_, ACT_INST_ID_, TASK_ID_, NAME_, REV_, VAR_TYPE_, TIME_, BYTEARRAY_ID_, DOUBLE_, LONG_ , TEXT_, TEXT2_)
    values (
      '16',
      'VariableUpdate',
      '13',
      '13',
      '14',
      null,
      'player',
      0,
      'string',
      '2012-11-22 14:24:55',
      null,
      null,
      null,
      'gonzo',
      null
    );

insert into ACT_RU_VARIABLE (ID_, REV_, TYPE_, NAME_, PROC_INST_ID_, EXECUTION_ID_, TASK_ID_, BYTEARRAY_ID_, DOUBLE_, LONG_ , TEXT_, TEXT2_)
    values (
	    '17',
	    1,
	    'string',
	    'instrument',
      '13',
	    '13',
      null,
	    null,
	    null,
	    null,
	    'trumpet',
	    null
    );

insert into ACT_HI_DETAIL (ID_, TYPE_, PROC_INST_ID_, EXECUTION_ID_, ACT_INST_ID_, TASK_ID_, NAME_, REV_, VAR_TYPE_, TIME_, BYTEARRAY_ID_, DOUBLE_, LONG_ , TEXT_, TEXT2_)
    values (
      '18',
      'VariableUpdate',
      '13',
      '13',
      '14',
      null,
      'instrument',
      0,
      'string',
      '2012-11-22 14:24:55',
      null,
      null,
      null,
      'trumpet',
      null
    );

insert into ACT_HI_ACTINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        ACT_ID_,
        ACT_NAME_,
        ACT_TYPE_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_
      ) values (
        '19',
        'taskWithExecutionVariablesProcess:1:12',
        '13',
        '13',
        'taskWithExecutionVariables',
        'taskWithExecutionVariables',
        'userTask',
        null,
        '2012-11-22 14:24:55',
        null,
        null
      );

insert into ACT_RU_TASK (ID_, REV_, NAME_, PARENT_TASK_ID_, DESCRIPTION_, PRIORITY_, CREATE_TIME_, OWNER_,
                      ASSIGNEE_, DELEGATION_, EXECUTION_ID_, PROC_INST_ID_, PROC_DEF_ID_, TASK_DEF_KEY_, DUE_DATE_)
    values ('20',
            1,
            'taskWithExecutionVariables',
            null,
            null,
            50,
            '2012-11-22 14:24:55',
            null,
            null,
            null,
            '13',
            '13',
            'taskWithExecutionVariablesProcess:1:12',
            'taskWithExecutionVariables',
            null
           );

insert into ACT_HI_TASKINST (
        ID_,
        PROC_DEF_ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        NAME_,
        PARENT_TASK_ID_,
        DESCRIPTION_,
        OWNER_,
        ASSIGNEE_,
        START_TIME_,
        END_TIME_,
        DURATION_,
        DELETE_REASON_,
        TASK_DEF_KEY_,
        PRIORITY_,
        DUE_DATE_
      ) values (
        '20',
        'taskWithExecutionVariablesProcess:1:12',
        '13',
        '13',
        'taskWithExecutionVariables',
        null,
        null,
        null,
        null,
        '2012-11-22 14:24:55',
        null,
        null,
        null,
        'taskWithExecutionVariables',
        50,
        null
      );


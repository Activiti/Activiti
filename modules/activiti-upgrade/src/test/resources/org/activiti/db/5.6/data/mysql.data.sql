insert into ACT_RE_DEPLOYMENT(ID_, NAME_, DEPLOY_TIME_)
    values('1', 'simpleTaskProcess', '2012-11-13 16:57:09');

insert into ACT_GE_BYTEARRAY(ID_, REV_, NAME_, BYTES_, DEPLOYMENT_ID_)
    values ('2', 1, 'org/activiti/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml', 0x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d38223f3eaa3c646566696e6974696f6e732069643d227461736b41737369676e65654578616d706c652220a20202020202020202020202020786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c22a20202020202020202020202020786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e22a202020202020202020202020207461726765744e616d6573706163653d2255706772616465223ea2020a20203c70726f636573732069643d2273696d706c655461736b50726f63657373223ea2020a202020203c73746172744576656e742069643d227374617274222f3ea20202020a202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273696d706c655461736b3122202f3eaa202020203c757365725461736b2069643d2273696d706c655461736b3122206e616d653d2273696d706c655461736b222061637469766974693a61737369676e65653d226b65726d6974222f3ea20202020a202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273696d706c655461736b3122207461726765745265663d2273696d706c655461736b3222202f3ea20202020a202020203c757365725461736b2069643d2273696d706c655461736b3222206e616d653d2273696d706c655461736b32222061637469766974693a61737369676e65653d227377656469736863686566223ea2020202020203c646f63756d656e746174696f6e3e73706963792073617563653c2f646f63756d656e746174696f6e3ea202020203c2f757365725461736b3ea20202020a202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d2273696d706c655461736b3222207461726765745265663d2273696d706c655461736b3322202f3ea20202020a202020203c757365725461736b2069643d2273696d706c655461736b3322206e616d653d2273696d706c655461736b3322202f3ea20202020a202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d2273696d706c655461736b3322207461726765745265663d22656e6422202f3ea20202020a202020203c656e644576656e742069643d22656e6422202f3ea20202020a20203c2f70726f636573733eaa3c2f646566696e6974696f6e733e, '1');

insert into ACT_RE_PROCDEF(ID_, CATEGORY_, NAME_, KEY_, VERSION_, DEPLOYMENT_ID_, RESOURCE_NAME_, DGRM_RESOURCE_NAME_, HAS_START_FORM_KEY_)
    values ('simpleTaskProcess:1:3',
            'Upgrade',
            null,
            'simpleTaskProcess', 
            1,
            '1',
            'org/activiti/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml',
            null,
            false);

insert into ACT_RU_EXECUTION (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, ACT_ID_, IS_ACTIVE_, IS_CONCURRENT_, IS_SCOPE_, PARENT_ID_, SUPER_EXEC_)
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
      null,
      null
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
        END_ACT_ID_
      ) values (
        '4',
        '4',
        null,
        'simpleTaskProcess:1:3',
        '2012-11-13 16:57:09',
        null,
        null,
        null,
        'start',
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
        '2012-11-13 16:57:09',
        '2012-11-13 16:57:09',
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
        '2012-11-13 16:57:09',
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
            '2012-11-13 16:57:09',
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
        '2012-11-13 16:57:09',
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
        '2012-11-13 16:57:09',
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
            '2012-11-13 16:57:09',
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
        '2012-11-13 16:57:09',
        null,
        null,
        null,
        'simpleTask2',
        50,
        null
      );

update ACT_HI_TASKINST set
      EXECUTION_ID_ = '4',
      NAME_ = 'simpleTask',
      PARENT_TASK_ID_ = null,
      DESCRIPTION_ = null,
      OWNER_ = null,
      ASSIGNEE_ = 'kermit',
      END_TIME_ = '2012-11-13 16:57:09',
      DURATION_ = 273,
      DELETE_REASON_ = 'completed',
      TASK_DEF_KEY_ = 'simpleTask1',
      PRIORITY_ = 50,
      DUE_DATE_ = null
    where ID_ = '7';

update ACT_HI_ACTINST set
      EXECUTION_ID_ = '4',
      ASSIGNEE_ = 'kermit',
      END_TIME_ = '2012-11-13 16:57:09',
      DURATION_ = 280
    where ID_ = '6';

update ACT_RU_EXECUTION set
      REV_ = 2,
      PROC_DEF_ID_ = 'simpleTaskProcess:1:3',
      ACT_ID_ = 'simpleTask2',
      IS_ACTIVE_ = true,
      IS_CONCURRENT_ = false,
      IS_SCOPE_ = true,
      PARENT_ID_ = null,
      SUPER_EXEC_ = null
    where ID_ = '4'
      and REV_ = 1;

delete from ACT_RU_TASK where ID_ = '7';

insert into ACT_RE_DEPLOYMENT(ID_, NAME_, DEPLOY_TIME_)
    values('10', 'simpleTaskProcess', '2012-11-13 16:57:09');

insert into ACT_GE_BYTEARRAY(ID_, REV_, NAME_, BYTES_, DEPLOYMENT_ID_)
    values ('11', 1, 'org/activiti/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml', 0x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d38223f3eaa3c646566696e6974696f6e732069643d227461736b41737369676e65654578616d706c652220a20202020202020202020202020786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c22a20202020202020202020202020786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e22a202020202020202020202020207461726765744e616d6573706163653d2255706772616465223ea2020a20203c70726f636573732069643d227461736b57697468457865637574696f6e5661726961626c657350726f63657373223ea2020a202020203c73746172744576656e742069643d227374617274222f3ea20202020a202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d227461736b57697468457865637574696f6e5661726961626c657322202f3eaa202020203c757365725461736b2069643d227461736b57697468457865637574696f6e5661726961626c657322206e616d653d227461736b57697468457865637574696f6e5661726961626c657322202f3ea20202020a202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d227461736b57697468457865637574696f6e5661726961626c657322207461726765745265663d22656e6422202f3ea20202020a202020203c656e644576656e742069643d22656e6422202f3ea20202020a20203c2f70726f636573733eaa3c2f646566696e6974696f6e733e, '10');

insert into ACT_RE_PROCDEF(ID_, CATEGORY_, NAME_, KEY_, VERSION_, DEPLOYMENT_ID_, RESOURCE_NAME_, DGRM_RESOURCE_NAME_, HAS_START_FORM_KEY_)
    values ('taskWithExecutionVariablesProcess:1:12',
            'Upgrade',
            null,
            'taskWithExecutionVariablesProcess', 
            1,
            '10',
            'org/activiti/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml',
            null,
            false);

insert into ACT_RU_EXECUTION (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, ACT_ID_, IS_ACTIVE_, IS_CONCURRENT_, IS_SCOPE_, PARENT_ID_, SUPER_EXEC_)
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
      null,
      null
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
        END_ACT_ID_
      ) values (
        '13',
        '13',
        null,
        'taskWithExecutionVariablesProcess:1:12',
        '2012-11-13 16:57:09',
        null,
        null,
        null,
        'start',
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
        '2012-11-13 16:57:09',
        '2012-11-13 16:57:09',
        1
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
      '2012-11-13 16:57:09',
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
      '2012-11-13 16:57:09',
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
        '2012-11-13 16:57:09',
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
            '2012-11-13 16:57:09',
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
        '2012-11-13 16:57:09',
        null,
        null,
        null,
        'taskWithExecutionVariables',
        50,
        null
      );


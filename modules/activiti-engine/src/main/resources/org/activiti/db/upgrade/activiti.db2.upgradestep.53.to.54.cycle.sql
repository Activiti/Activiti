alter table ACT_CY_CONFIG alter VALUE_ varchar(1024);

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
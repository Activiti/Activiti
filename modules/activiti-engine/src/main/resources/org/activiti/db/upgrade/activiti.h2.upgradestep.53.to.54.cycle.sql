alter table ACT_CY_CONFIG alter VALUE_ clob;

create table ACT_CY_PROCESS_SOLUTION (
	ID_ varchar NOT NULL,
	LABEL_ varchar NOT NULL,
	STATE_ varchar NOT NULL,
	primary key(ID_)
);

create table ACT_CY_V_FOLDER (
	ID_ varchar NOT NULL,
	LABEL_ varchar NOT NULL,
	CONNECTOR_ID_ varchar NOT NULL,
	REFERENCED_NODE_ID_ varchar NOT NULL,
	PROCESS_SOLUTION_ID_ varchar NOT NULL,
	TYPE_ varchar NOT NULL,
	primary key(ID_)
);

alter table ACT_CY_V_FOLDER 
    add constraint FK_CY_PROCESS_SOLUTION_ID 
    foreign key (PROCESS_SOLUTION_ID_) 
    references ACT_CY_PROCESS_SOLUTION;
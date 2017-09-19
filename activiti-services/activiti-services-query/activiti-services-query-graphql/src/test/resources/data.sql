insert into process_instance (process_instance_id, last_modified, last_modified_from, last_modified_to, process_definition_id, status) values
  (0, null, null, null, 'process_definition_id', 'Running'),
  (1, null, null, null, 'process_definition_id', 'Running');

insert into task (id, assignee, category, create_time, description, due_date, last_modified, last_modified_from, last_modified_to, name, priority, process_definition_id, process_instance_id, status) values
  ('1', 'assignee', 'category', null, 'description', null, null, null, null, 'task1', 'Normal', 'process_definition_id', 0, 'Completed' ),
  ('2', 'assignee', 'category', null, 'description', null, null, null, null, 'task2', 'High', 'process_definition_id', 0, 'Running' ),
  ('3', 'assignee', 'category', null, 'description', null, null, null, null, 'task3', 'Normal', 'process_definition_id', 0, 'Running' ),
  ('4', 'assignee', 'category', null, 'description', null, null, null, null, 'task4', 'High', 'process_definition_id', 1, 'Running' ),
  ('5', 'assignee', 'category', null, 'description', null, null, null, null, 'task5', 'Normal', 'process_definition_id', 1, 'Completed' );

insert into variable (id, create_time, execution_id, last_updated_time, name, process_instance_id, task_id, type, value) values
  (0, null, 'execution_id', null, 'variable1', 0, '1', 'String', 'value1'),
  (1, null, 'execution_id', null, 'variable2', 0, '1', 'String', 'value2'),
  (2, null, 'execution_id', null, 'variable3', 0, '2', 'String', 'value3'),
  (3, null, 'execution_id', null, 'variable4', 0, '2', 'String', 'value4'),
  (4, null, 'execution_id', null, 'variable5', 1, '4', 'String', 'value5'),
  (5, null, 'execution_id', null, 'variable6', 1, '4', 'String', 'value6');


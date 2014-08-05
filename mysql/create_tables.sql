create table recommendations ( user_id int, project_id int, weight float, foreign key (user_id) references users (id), foreign key (project_id) references projects(id));

create table fork_source ( project_id INT, parent_id INT, foreign key (project_id) references projects(id), foreign key (parent_id) references projects(id));

create table commit_prefs (author_id INT, project_id INT, commits INT);

alter table users add index (login);

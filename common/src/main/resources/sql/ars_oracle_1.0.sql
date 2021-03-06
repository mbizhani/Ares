-----------------------
-- CREATE AUDIT TABLES
-----------------------

CREATE TABLE a_mt_ars_prpCommand_role (
	r_num         NUMBER(10, 0) NOT NULL,
	f_prp_command NUMBER(19, 0) NOT NULL,
	f_role        NUMBER(19, 0) NOT NULL,
	r_type        NUMBER(3, 0),
	PRIMARY KEY (r_num, f_prp_command, f_role)
);

CREATE TABLE a_mt_ars_prpCommand_user (
	r_num         NUMBER(10, 0) NOT NULL,
	f_prp_command NUMBER(19, 0) NOT NULL,
	f_user        NUMBER(19, 0) NOT NULL,
	r_type        NUMBER(3, 0),
	PRIMARY KEY (r_num, f_prp_command, f_user)
);

CREATE TABLE a_mt_ars_siUser_role (
	r_num    NUMBER(10, 0) NOT NULL,
	f_siUser NUMBER(19, 0) NOT NULL,
	f_role   NUMBER(19, 0) NOT NULL,
	r_type   NUMBER(3, 0),
	PRIMARY KEY (r_num, f_siUser, f_role)
);

CREATE TABLE a_mt_ars_siUser_user (
	r_num    NUMBER(10, 0) NOT NULL,
	f_siUser NUMBER(19, 0) NOT NULL,
	f_user   NUMBER(19, 0) NOT NULL,
	r_type   NUMBER(3, 0),
	PRIMARY KEY (r_num, f_siUser, f_user)
);

CREATE TABLE a_mt_ars_srvcinst_role (
	r_num      NUMBER(10, 0) NOT NULL,
	f_srvcinst NUMBER(19, 0) NOT NULL,
	f_role     NUMBER(19, 0) NOT NULL,
	r_type     NUMBER(3, 0),
	PRIMARY KEY (r_num, f_srvcinst, f_role)
);

CREATE TABLE a_mt_ars_srvcinst_user (
	r_num      NUMBER(10, 0) NOT NULL,
	f_srvcinst NUMBER(19, 0) NOT NULL,
	f_user     NUMBER(19, 0) NOT NULL,
	r_type     NUMBER(3, 0),
	PRIMARY KEY (r_num, f_srvcinst, f_user)
);

CREATE TABLE a_t_ars_basic_data (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	e_discriminator NUMBER(10, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_ars_prp_command (
	id                 NUMBER(19, 0) NOT NULL,
	r_num              NUMBER(10, 0) NOT NULL,
	r_type             NUMBER(3, 0),
	c_code             VARCHAR2(255 CHAR),
	b_enabled          NUMBER(1, 0),
	d_modification     DATE,
	f_modifier_user    NUMBER(19, 0),
	c_name             VARCHAR2(255 CHAR),
	c_params           VARCHAR2(1000 CHAR),
	f_service_instance NUMBER(19, 0),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_ars_server (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	c_address       VARCHAR2(255 CHAR),
	n_counter       NUMBER(10, 0),
	f_hypervisor    NUMBER(19, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR),
	e_os            NUMBER(10, 0),
	c_vm_id         VARCHAR2(255 CHAR),
	f_comp          NUMBER(19, 0),
	f_env           NUMBER(19, 0),
	f_func          NUMBER(19, 0),
	f_loc           NUMBER(19, 0),
	f_owner         NUMBER(19, 0),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_ars_service (
	id                NUMBER(19, 0) NOT NULL,
	r_num             NUMBER(10, 0) NOT NULL,
	r_type            NUMBER(3, 0),
	n_admin_port      NUMBER(10, 0),
	c_conn_pattern    VARCHAR2(1000 CHAR),
	d_modification    DATE,
	f_modifier_user   NUMBER(19, 0),
	c_name            VARCHAR2(255 CHAR),
	c_ports           VARCHAR2(255 CHAR),
	c_username_reg_ex VARCHAR2(255 CHAR),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_ars_service_inst (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR),
	n_port          NUMBER(10, 0),
	f_server        NUMBER(19, 0),
	f_service       NUMBER(19, 0),
	e_mod           NUMBER(10, 0),
	PRIMARY KEY (r_num, f_server, f_service)
);

CREATE TABLE a_t_ars_service_inst_prop_val (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_value         VARCHAR2(255 CHAR),
	f_property      NUMBER(19, 0),
	f_service_inst  NUMBER(19, 0),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_ars_service_inst_user (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	b_enabled       NUMBER(1, 0),
	e_type          NUMBER(10, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_password      VARCHAR2(255 CHAR),
	e_remote_mode   NUMBER(10, 0),
	e_mod           NUMBER(10, 0),
	f_service       NUMBER(19, 0),
	f_service_inst  NUMBER(19, 0),
	c_username      VARCHAR2(255 CHAR),
	f_server        NUMBER(19, 0),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_ars_service_prop (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR),
	b_required      NUMBER(1, 0),
	c_value         VARCHAR2(255 CHAR),
	f_service       NUMBER(19, 0),
	PRIMARY KEY (id, r_num)
);

------------------------
-- CREATE MIDDLE TABLES
------------------------

CREATE TABLE mt_ars_prpCommand_role (
	f_prp_command NUMBER(19, 0) NOT NULL,
	f_role        NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_ars_prpCommand_user (
	f_prp_command NUMBER(19, 0) NOT NULL,
	f_user        NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_ars_siUser_role (
	f_siUser NUMBER(19, 0) NOT NULL,
	f_role   NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_ars_siUser_user (
	f_siUser NUMBER(19, 0) NOT NULL,
	f_user   NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_ars_srvcinst_role (
	f_srvcinst NUMBER(19, 0) NOT NULL,
	f_role     NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_ars_srvcinst_user (
	f_srvcinst NUMBER(19, 0) NOT NULL,
	f_user     NUMBER(19, 0) NOT NULL
);

CREATE TABLE t_ars_basic_data (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	e_discriminator NUMBER(10, 0)      NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR) NOT NULL,
	n_version       NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_command (
	id              NUMBER(19, 0)      NOT NULL,
	f_config        NUMBER(19, 0),
	b_confirm       NUMBER(1, 0)       NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	b_enabled       NUMBER(1, 0)       NOT NULL,
	n_exec_limit    NUMBER(10, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR) NOT NULL,
	f_service       NUMBER(19, 0),
	n_version       NUMBER(10, 0)      NOT NULL,
	e_view_mode     NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_command_log (
	id                 NUMBER(19, 0) NOT NULL,
	d_creation         DATE          NOT NULL,
	f_creator_user     NUMBER(19, 0) NOT NULL,
	n_duration         NUMBER(19, 0),
	c_error            VARCHAR2(2000 CHAR),
	c_params           VARCHAR2(1000 CHAR),
	f_prep_command     NUMBER(19, 0),
	e_result           NUMBER(10, 0) NOT NULL,
	f_command          NUMBER(19, 0) NOT NULL,
	f_service_instance NUMBER(19, 0) NOT NULL,
	c_log_file_id      VARCHAR2(255 CHAR),
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_config_lob (
	id              NUMBER(19, 0) NOT NULL,
	d_creation      DATE          NOT NULL,
	f_creator_user  NUMBER(19, 0) NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_value         CLOB          NOT NULL,
	n_version       NUMBER(10, 0) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_prp_command (
	id                 NUMBER(19, 0)      NOT NULL,
	c_code             VARCHAR2(255 CHAR) NOT NULL,
	f_command          NUMBER(19, 0)      NOT NULL,
	d_creation         DATE               NOT NULL,
	f_creator_user     NUMBER(19, 0)      NOT NULL,
	b_enabled          NUMBER(1, 0)       NOT NULL,
	d_modification     DATE,
	f_modifier_user    NUMBER(19, 0),
	c_name             VARCHAR2(255 CHAR) NOT NULL,
	c_params           VARCHAR2(1000 CHAR),
	f_service_instance NUMBER(19, 0),
	n_version          NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_server (
	id              NUMBER(19, 0)      NOT NULL,
	c_address       VARCHAR2(255 CHAR),
	n_counter       NUMBER(10, 0),
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	f_hypervisor    NUMBER(19, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR) NOT NULL,
	e_os            NUMBER(10, 0),
	n_version       NUMBER(10, 0)      NOT NULL,
	c_vm_id         VARCHAR2(255 CHAR),
	f_comp          NUMBER(19, 0),
	f_env           NUMBER(19, 0),
	f_func          NUMBER(19, 0),
	f_loc           NUMBER(19, 0),
	f_owner         NUMBER(19, 0),
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_service (
	id                NUMBER(19, 0)      NOT NULL,
	n_admin_port      NUMBER(10, 0),
	c_conn_pattern    VARCHAR2(1000 CHAR),
	d_creation        DATE               NOT NULL,
	f_creator_user    NUMBER(19, 0)      NOT NULL,
	d_modification    DATE,
	f_modifier_user   NUMBER(19, 0),
	c_name            VARCHAR2(255 CHAR) NOT NULL,
	c_ports           VARCHAR2(255 CHAR),
	c_username_reg_ex VARCHAR2(255 CHAR),
	n_version         NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_service_inst (
	id              NUMBER(19, 0) NOT NULL,
	d_creation      DATE          NOT NULL,
	f_creator_user  NUMBER(19, 0) NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR),
	n_port          NUMBER(10, 0),
	f_server        NUMBER(19, 0),
	n_version       NUMBER(10, 0) NOT NULL,
	f_service       NUMBER(19, 0) NOT NULL,
	e_mod           NUMBER(10, 0) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_service_inst_prop_val (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_value         VARCHAR2(255 CHAR) NOT NULL,
	n_version       NUMBER(10, 0)      NOT NULL,
	f_property      NUMBER(19, 0)      NOT NULL,
	f_service_inst  NUMBER(19, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_service_inst_user (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	b_enabled       NUMBER(1, 0)       NOT NULL,
	e_type          NUMBER(10, 0)      NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_password      VARCHAR2(255 CHAR) NOT NULL,
	e_remote_mode   NUMBER(10, 0)      NOT NULL,
	e_mod           NUMBER(10, 0)      NOT NULL,
	f_service       NUMBER(19, 0),
	f_service_inst  NUMBER(19, 0),
	c_username      VARCHAR2(255 CHAR) NOT NULL,
	n_version       NUMBER(10, 0)      NOT NULL,
	f_server        NUMBER(19, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_service_prop (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR) NOT NULL,
	b_required      NUMBER(1, 0)       NOT NULL,
	c_value         VARCHAR2(255 CHAR),
	n_version       NUMBER(10, 0)      NOT NULL,
	f_service       NUMBER(19, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_ars_terminal (
	id              NUMBER(19, 0) NOT NULL,
	b_active        NUMBER(1, 0)  NOT NULL,
	d_creation      DATE          NOT NULL,
	f_creator_user  NUMBER(19, 0) NOT NULL,
	d_disconnection DATE,
	f_target        NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (id)
);

-----------------------------
-- CREATE UNIQUE CONSTRAINTS
-----------------------------

ALTER TABLE t_ars_basic_data
	ADD CONSTRAINT uk_ars_basNameDis UNIQUE (c_name, e_discriminator);

ALTER TABLE t_ars_command
	ADD CONSTRAINT uk_ars_command UNIQUE (c_name, f_service);

ALTER TABLE t_ars_prp_command
	ADD CONSTRAINT uk_ars_prepCommand_code UNIQUE (c_code);

ALTER TABLE t_ars_server
	ADD CONSTRAINT uk_ars_serverName UNIQUE (c_name);

ALTER TABLE t_ars_service
	ADD CONSTRAINT uk_ars_service UNIQUE (c_name);

ALTER TABLE t_ars_service_inst
	ADD CONSTRAINT uk_ars_serviceInst UNIQUE (f_server, f_service, c_name);

ALTER TABLE t_ars_service_inst_prop_val
	ADD CONSTRAINT uk_ars_siPropVal UNIQUE (f_property, f_service_inst);

ALTER TABLE t_ars_service_inst_user
	ADD CONSTRAINT uk_ars_user_username UNIQUE (c_username, f_service_inst);

ALTER TABLE t_ars_service_prop
	ADD CONSTRAINT uk_ars_serviceProp UNIQUE (c_name, f_service);


ALTER TABLE mt_ars_prpCommand_role
	ADD CONSTRAINT uk_mtPrpCommandRole UNIQUE (f_prp_command, f_role);

ALTER TABLE mt_ars_prpCommand_user
	ADD CONSTRAINT uk_ars_mtPrpCommandUser UNIQUE (f_prp_command, f_user);

ALTER TABLE mt_ars_siUser_role
	ADD CONSTRAINT uk_mtSiUserRole UNIQUE (f_siUser, f_role);

ALTER TABLE mt_ars_siUser_user
	ADD CONSTRAINT uk_ars_mtSiUserUser UNIQUE (f_siUser, f_user);

ALTER TABLE mt_ars_srvcinst_role
	ADD CONSTRAINT uk_ars_mtSrvcInstRole UNIQUE (f_srvcinst, f_role);

ALTER TABLE mt_ars_srvcinst_user
	ADD CONSTRAINT uk_ars_mtSrvcInstUser UNIQUE (f_srvcinst, f_user);

----------------------------------
-- CREATE REFERENTIAL CONSTRAINTS
----------------------------------

ALTER TABLE a_mt_ars_prpCommand_role
	ADD CONSTRAINT FKpqn4hgiqrchxalggi4vi0chs1
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_ars_prpCommand_user
	ADD CONSTRAINT FK8c17hi4t89q44nwyyddvjaodb
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_ars_siUser_role
	ADD CONSTRAINT FKeypmey105tou1xn515dddkstw
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_ars_siUser_user
	ADD CONSTRAINT FK6jdka8lpuyp2p9w5qs03eo51l
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_ars_srvcinst_role
	ADD CONSTRAINT FK33cag762rq74p717gi1uo6usx
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_ars_srvcinst_user
	ADD CONSTRAINT FKmuv98952epkqyiiikvcw5aauq
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_basic_data
	ADD CONSTRAINT FKooy7i5d4c2lnha98hdtegniyy
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_prp_command
	ADD CONSTRAINT FKlhrimesp5tfdt51ynuaxktd8w
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_server
	ADD CONSTRAINT FKl76uxncckiaoh1arqpsxfhuva
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_service
	ADD CONSTRAINT FKqcol19nq1oljn5ph9acd7a3b9
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_service_inst
	ADD CONSTRAINT FKpm4th3iw5xbm5q0lanwpm7p6e
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_service_inst_prop_val
	ADD CONSTRAINT FKeifejou9xm5uun88xy7veeviq
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_service_inst_user
	ADD CONSTRAINT FKsbscocl3g9xp5gegxndvvbvmr
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_ars_service_prop
	ADD CONSTRAINT FKshdxi6tk2qpmwy71a2fu7gbg5
FOREIGN KEY (r_num)
REFERENCES REVINFO;

------------------------------

ALTER TABLE mt_ars_prpCommand_role
	ADD CONSTRAINT prpCommandRole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_ars_prpCommand_role
	ADD CONSTRAINT prpCommandRole2prpCommand
FOREIGN KEY (f_prp_command)
REFERENCES t_ars_prp_command;

ALTER TABLE mt_ars_prpCommand_user
	ADD CONSTRAINT prpCommandUser2user
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;

ALTER TABLE mt_ars_prpCommand_user
	ADD CONSTRAINT prpCommandUser2prpCommand
FOREIGN KEY (f_prp_command)
REFERENCES t_ars_prp_command;

ALTER TABLE mt_ars_siUser_role
	ADD CONSTRAINT siUserRole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_ars_siUser_role
	ADD CONSTRAINT siUserRole2siUser
FOREIGN KEY (f_siUser)
REFERENCES t_ars_service_inst_user;

ALTER TABLE mt_ars_siUser_user
	ADD CONSTRAINT siUserUser2user
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;

ALTER TABLE mt_ars_siUser_user
	ADD CONSTRAINT siUserUser2siUser
FOREIGN KEY (f_siUser)
REFERENCES t_ars_service_inst_user;

ALTER TABLE mt_ars_srvcinst_role
	ADD CONSTRAINT srvcinstRole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_ars_srvcinst_role
	ADD CONSTRAINT srvcinstRole2srvcinst
FOREIGN KEY (f_srvcinst)
REFERENCES t_ars_service_inst;

ALTER TABLE mt_ars_srvcinst_user
	ADD CONSTRAINT srvcinstUser2srvcinst
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;

ALTER TABLE mt_ars_srvcinst_user
	ADD CONSTRAINT srvcinstUser2user
FOREIGN KEY (f_srvcinst)
REFERENCES t_ars_service_inst;

ALTER TABLE t_ars_basic_data
	ADD CONSTRAINT basic_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_basic_data
	ADD CONSTRAINT basic_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_command
	ADD CONSTRAINT command2configLob
FOREIGN KEY (f_config)
REFERENCES t_ars_config_lob;

ALTER TABLE t_ars_command
	ADD CONSTRAINT command_crtrUsr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_command
	ADD CONSTRAINT command_mdfrUsr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_command
	ADD CONSTRAINT command2service
FOREIGN KEY (f_service)
REFERENCES t_ars_service;

ALTER TABLE t_ars_command_log
	ADD CONSTRAINT commandLog2command
FOREIGN KEY (f_command)
REFERENCES t_ars_command;

ALTER TABLE t_ars_command_log
	ADD CONSTRAINT commandLog_crtrUsr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_command_log
	ADD CONSTRAINT commandLog2prepCommand
FOREIGN KEY (f_prep_command)
REFERENCES t_ars_prp_command;

ALTER TABLE t_ars_command_log
	ADD CONSTRAINT commandLog2serviceInstance
FOREIGN KEY (f_service_instance)
REFERENCES t_ars_service_inst;

ALTER TABLE t_ars_config_lob
	ADD CONSTRAINT arsCfgLob_crtrUsr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_config_lob
	ADD CONSTRAINT arsCfgLob_mdfrUsr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_prp_command
	ADD CONSTRAINT prpCommand2command
FOREIGN KEY (f_command)
REFERENCES t_ars_command;

ALTER TABLE t_ars_prp_command
	ADD CONSTRAINT prpCommand_crtrUsr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_prp_command
	ADD CONSTRAINT prpCommand_mdfrUsr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_prp_command
	ADD CONSTRAINT prpCommand2serviceInstance
FOREIGN KEY (f_service_instance)
REFERENCES t_ars_service_inst;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_comp2basic
FOREIGN KEY (f_comp)
REFERENCES t_ars_basic_data;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_env2basic
FOREIGN KEY (f_env)
REFERENCES t_ars_basic_data;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_func2basic
FOREIGN KEY (f_func)
REFERENCES t_ars_basic_data;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_hyper2server
FOREIGN KEY (f_hypervisor)
REFERENCES t_ars_server;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_loc2basic
FOREIGN KEY (f_loc)
REFERENCES t_ars_basic_data;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_server
	ADD CONSTRAINT server_owner2user
FOREIGN KEY (f_owner)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service
	ADD CONSTRAINT service_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service
	ADD CONSTRAINT service_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst
	ADD CONSTRAINT srvcinst_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst
	ADD CONSTRAINT srvcinst_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst
	ADD CONSTRAINT srvcinst2server
FOREIGN KEY (f_server)
REFERENCES t_ars_server;

ALTER TABLE t_ars_service_inst
	ADD CONSTRAINT srvcinst2service
FOREIGN KEY (f_service)
REFERENCES t_ars_service;

ALTER TABLE t_ars_service_inst_prop_val
	ADD CONSTRAINT siPropVal_crtrUsr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst_prop_val
	ADD CONSTRAINT siPropVal_mdfrUsr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst_prop_val
	ADD CONSTRAINT siPropVal2property
FOREIGN KEY (f_property)
REFERENCES t_ars_service_prop;

ALTER TABLE t_ars_service_inst_prop_val
	ADD CONSTRAINT siPropVal2serviceInstance
FOREIGN KEY (f_service_inst)
REFERENCES t_ars_service_inst;

ALTER TABLE t_ars_service_inst_user
	ADD CONSTRAINT siUser_crtrUsr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst_user
	ADD CONSTRAINT siUser_mdfrUsr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_inst_user
	ADD CONSTRAINT siUser2server
FOREIGN KEY (f_server)
REFERENCES t_ars_server;

ALTER TABLE t_ars_service_inst_user
	ADD CONSTRAINT siUser2service
FOREIGN KEY (f_service)
REFERENCES t_ars_service;

ALTER TABLE t_ars_service_inst_user
	ADD CONSTRAINT siUser2serviceInstance
FOREIGN KEY (f_service_inst)
REFERENCES t_ars_service_inst;

ALTER TABLE t_ars_service_prop
	ADD CONSTRAINT srvcprop_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_prop
	ADD CONSTRAINT srvcprop_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_service_prop
	ADD CONSTRAINT srvcprop2service
FOREIGN KEY (f_service)
REFERENCES t_ars_service;

ALTER TABLE t_ars_terminal
	ADD CONSTRAINT trmConn2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_ars_terminal
	ADD CONSTRAINT trmConn2osiUser
FOREIGN KEY (f_target)
REFERENCES t_ars_service_inst_user;

--------------------
-- CREATE SEQUENCES
--------------------

CREATE SEQUENCE ars_basic
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_command
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_command_log
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_config_lob
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_prp_command
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_server
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_service
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_service_inst
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_service_inst_prop_val
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_service_inst_user
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_service_prop
	START WITH 1
	INCREMENT BY 1;

CREATE SEQUENCE ars_terminal
	START WITH 1
	INCREMENT BY 1;
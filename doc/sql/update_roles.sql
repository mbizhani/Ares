declare
  type array_t is varray(9) of varchar2(20);
  v_roles array_t := array_t('Linux','LinuxSI','LinuxAdmin','OracleDB','OracleDBAdmin','OracleDBSI','ESXi','ESXiAdmin','ESXiSI');
  v_root_id number;
  v_role varchar2(100);
  v_role_si varchar2(100);
  v_role_admin varchar2(100);
begin
  select id into v_root_id from t_dmt_user where c_username = 'root';

  for i in 1..v_roles.count loop
    merge into t_dmt_role dest
      using (select v_roles(i) c_name from dual) src
      on (dest.c_name = src.c_name)
      when not matched then
        insert (id, c_name, e_mod, e_role_mode, d_creation, n_version, f_creator_user)
        values (dmt_role.nextval, src.c_name, 12, 1, sysdate, 0, v_root_id);
  end loop;
  commit;

  execute immediate 'truncate table MT_ARS_SRVCINST_ROLE';
  execute immediate 'truncate table MT_ARS_SIUSER_ROLE';
  execute immediate 'truncate table MT_ARS_PRPCOMMAND_ROLE';

  -- ************************

  select id into v_role from t_dmt_role where c_name = 'Linux';
  select id into v_role_si from t_dmt_role where c_name = 'LinuxSI';
  select id into v_role_admin from t_dmt_role where c_name = 'LinuxAdmin';

  for v_si in (select si.id from t_ars_service_inst si join t_ars_service sv on si.f_service = sv.id where sv.c_name = 'Linux') loop

    merge into MT_ARS_SRVCINST_ROLE dest
      using (select v_si.id f_srvcinst, v_role_si f_role from dual) src
      on (dest.f_srvcinst = src.f_srvcinst and dest.f_role = src.f_role)
      when not matched then insert (f_srvcinst, f_role) values (src.f_srvcinst, src.f_role);

    merge into MT_ARS_SRVCINST_ROLE dest
      using (select v_si.id f_srvcinst, v_role_admin f_role from dual) src
      on (dest.f_srvcinst = src.f_srvcinst and dest.f_role = src.f_role)
      when not matched then insert (f_srvcinst, f_role) values (src.f_srvcinst, src.f_role);

  end loop;

  for v_siu in (select siu.id from t_ars_service_inst_user siu join t_ars_service sv on siu.f_service = sv.id where sv.c_name = 'Linux') loop
    merge into MT_ARS_SIUSER_ROLE dest
      using (select v_siu.id f_siuser, v_role_admin f_role from dual) src
      on (dest.f_siuser = src.f_siuser and dest.f_role = src.f_role)
      when not matched then insert (f_siuser, f_role) values (src.f_siuser, src.f_role);
  end loop;

  for v_pc in (select pc.id from t_ars_prp_command pc join t_ars_command cm on cm.id = pc.f_command join t_ars_service sv on sv.id = cm.f_service where sv.c_name = 'Linux') loop

    merge into MT_ARS_PRPCOMMAND_ROLE dest
      using (select v_pc.id f_prp_command, v_role f_role from dual) src
      on (dest.f_prp_command = src.f_prp_command and dest.f_role = src.f_role)
      when not matched then insert (f_prp_command, f_role) values (src.f_prp_command, src.f_role);

    merge into MT_ARS_PRPCOMMAND_ROLE dest
      using (select v_pc.id f_prp_command, v_role_admin f_role from dual) src
      on (dest.f_prp_command = src.f_prp_command and dest.f_role = src.f_role)
      when not matched then insert (f_prp_command, f_role) values (src.f_prp_command, src.f_role);

  end loop;

  -- ************************

  select id into v_role from t_dmt_role where c_name = 'OracleDB';
  select id into v_role_si from t_dmt_role where c_name = 'OracleDBSI';
  select id into v_role_admin from t_dmt_role where c_name = 'OracleDBAdmin';

  for v_si in (select si.id from t_ars_service_inst si join t_ars_service sv on si.f_service = sv.id where sv.c_name = 'OracleDB') loop

    merge into MT_ARS_SRVCINST_ROLE dest
      using (select v_si.id f_srvcinst, v_role_si f_role from dual) src
      on (dest.f_srvcinst = src.f_srvcinst and dest.f_role = src.f_role)
      when not matched then insert (f_srvcinst, f_role) values (src.f_srvcinst, src.f_role);

    merge into MT_ARS_SRVCINST_ROLE dest
      using (select v_si.id f_srvcinst, v_role_admin f_role from dual) src
      on (dest.f_srvcinst = src.f_srvcinst and dest.f_role = src.f_role)
      when not matched then insert (f_srvcinst, f_role) values (src.f_srvcinst, src.f_role);

  end loop;

  for v_siu in (select siu.id from t_ars_service_inst_user siu join t_ars_service sv on siu.f_service = sv.id where sv.c_name = 'OracleDB') loop
    merge into MT_ARS_SIUSER_ROLE dest
      using (select v_siu.id f_siuser, v_role_admin f_role from dual) src
      on (dest.f_siuser = src.f_siuser and dest.f_role = src.f_role)
      when not matched then insert (f_siuser, f_role) values (src.f_siuser, src.f_role);
  end loop;

  for v_pc in (select pc.id from t_ars_prp_command pc join t_ars_command cm on cm.id = pc.f_command join t_ars_service sv on sv.id = cm.f_service where sv.c_name = 'OracleDB') loop

    merge into MT_ARS_PRPCOMMAND_ROLE dest
      using (select v_pc.id f_prp_command, v_role f_role from dual) src
      on (dest.f_prp_command = src.f_prp_command and dest.f_role = src.f_role)
      when not matched then insert (f_prp_command, f_role) values (src.f_prp_command, src.f_role);

    merge into MT_ARS_PRPCOMMAND_ROLE dest
      using (select v_pc.id f_prp_command, v_role_admin f_role from dual) src
      on (dest.f_prp_command = src.f_prp_command and dest.f_role = src.f_role)
      when not matched then insert (f_prp_command, f_role) values (src.f_prp_command, src.f_role);

  end loop;

  -- ************************

  select id into v_role from t_dmt_role where c_name = 'ESXi';
  select id into v_role_si from t_dmt_role where c_name = 'ESXiSI';
  select id into v_role_admin from t_dmt_role where c_name = 'ESXiAdmin';

  for v_si in (select si.id from t_ars_service_inst si join t_ars_service sv on si.f_service = sv.id where sv.c_name = 'ESXi') loop

    merge into MT_ARS_SRVCINST_ROLE dest
      using (select v_si.id f_srvcinst, v_role_si f_role from dual) src
      on (dest.f_srvcinst = src.f_srvcinst and dest.f_role = src.f_role)
      when not matched then insert (f_srvcinst, f_role) values (src.f_srvcinst, src.f_role);

    merge into MT_ARS_SRVCINST_ROLE dest
      using (select v_si.id f_srvcinst, v_role_admin f_role from dual) src
      on (dest.f_srvcinst = src.f_srvcinst and dest.f_role = src.f_role)
      when not matched then insert (f_srvcinst, f_role) values (src.f_srvcinst, src.f_role);

  end loop;

  for v_siu in (select siu.id from t_ars_service_inst_user siu join t_ars_service sv on siu.f_service = sv.id where sv.c_name = 'ESXi') loop
    merge into MT_ARS_SIUSER_ROLE dest
      using (select v_siu.id f_siuser, v_role_admin f_role from dual) src
      on (dest.f_siuser = src.f_siuser and dest.f_role = src.f_role)
      when not matched then insert (f_siuser, f_role) values (src.f_siuser, src.f_role);
  end loop;

  for v_pc in (select pc.id from t_ars_prp_command pc join t_ars_command cm on cm.id = pc.f_command join t_ars_service sv on sv.id = cm.f_service where sv.c_name = 'ESXi') loop

    merge into MT_ARS_PRPCOMMAND_ROLE dest
      using (select v_pc.id f_prp_command, v_role f_role from dual) src
      on (dest.f_prp_command = src.f_prp_command and dest.f_role = src.f_role)
      when not matched then insert (f_prp_command, f_role) values (src.f_prp_command, src.f_role);

    merge into MT_ARS_PRPCOMMAND_ROLE dest
      using (select v_pc.id f_prp_command, v_role_admin f_role from dual) src
      on (dest.f_prp_command = src.f_prp_command and dest.f_role = src.f_role)
      when not matched then insert (f_prp_command, f_role) values (src.f_prp_command, src.f_role);

  end loop;

  commit;
end;

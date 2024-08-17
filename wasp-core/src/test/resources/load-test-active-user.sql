insert into core.users (id, login, password, first_name, last_name, created_at, updated_at, ref_auth_types,
                        ref_user_statuses)
select uuid_generate_v4(),
       'test-active-user',
       'password',
       'first',
       'last',
       now(),
       now(),
       (select id from core.auth_types where auth_type_code = 'INT_WASP'),
       (select id from core.user_statuses where user_status_code = 'ACTIVE');
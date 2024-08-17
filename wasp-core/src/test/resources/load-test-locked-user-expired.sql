insert into core.users (id, login, password, first_name, last_name, created_at, updated_at, ref_auth_types,
                        ref_user_statuses, failed_logins_counter, last_failed_login_at, locked_at)
select uuid_generate_v4(),
       'test-locked-user',
       'password',
       'first',
       'last',
       now(),
       now(),
       (select id from core.auth_types where auth_type_code = 'INT_WASP'),
       (select id from core.user_statuses where user_status_code = 'LOCKED'),
       3,
       now() - interval '1 hour',
       now() - interval '1 hour';

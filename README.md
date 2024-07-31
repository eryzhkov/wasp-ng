# WASP NG

## How to build and start?

The application supports two profiles: 'local' and 'docker'.

The 'local' profile supposes the PostgreSQL database is started locally. See the /src/main/resources/application-local.properties for information about default user, password and database name.

If the user/database does not exist yet, just create one by any suitable way for you. For instance

```
$ psql -d postgres
postgres=# create user wasp encrypted password 'wasp';
CREATE ROLE
postgres=# create database wasp with owner wasp;
CREATE DATABASE
postgres=# \q
```

Be sure, the uuid-ossp extension is installed in the database:

```
$ psql -h localhost -d wasp
...
wasp=# CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION
wasp=# \q
```

The 'docker' profile is for running the application in a docker container. All environment variables are already defined in the .env file!

### How to build and start/stop the application with the 'local' profile?

```
$ mvn clean package -P local
$ mvn spring-boot:start
...
$ mvn spring-boot:stop
```

The application is available at http://localhost:8888/wasp (by default).

### How to build and start with the 'docker' profile?

```
$ mvn clean package -P docker
$ docker compose up
```

The application is available at http://localhost:8888/wasp (by default).
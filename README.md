# WASP/NG

## How to configure local development environment?

The following tools have to be installed:

- Java SDK 17+ (use [SDKMAN](https://sdkman.io))
- Apache Maven 3.9.8 (use [SDKMAN](https://sdkman.io))
- Git
- Docker
- PostgreSQL 16 (not mandatory but recommended)
- Hashicorp Vault (optionally)
- Any suitable IDE

### Git configuration

Don't forget to set at the project level the following configuration parameters:

```
$ git config user.name "<Your name>"
$ git config user.email "<Your email>"
```

## How to configure the application before start?

At the moment, there are no any stages where the application is supposed to be deployed.
So, all sensitive information in the application.properties refers to your local environment.

It's supposed that:

- there is a database named 'wasp'
- there is a database user named 'wasp' with the password 'wasp'
- the PostgreSQL is running locally

All these values are defaulted and are already present in the application.properties.

### How to set up the database locally?

Let's suppose you have to create the database and the user. Below are commands to do that using the 'psql' utility.

```
$ psql -d postgres
postgres=# create user wasp encrypted password 'wasp';
CREATE ROLE
postgres=# create database wasp with owner wasp;
CREATE DATABASE
postgres=# \q
```

All the needed extensions and tables will be created by the application during start up.

### What about database in a docker container?

See the docker-compose.yaml in the project root. The database will be created automatically at the first start of the
container.

### What about Hashicorp Vault?

The application supports configuration via Hashicorp Vault service. Right now, it is supposed that Vault is running
locally.

You may have the Vault installed locally. It gives a possibility to store all necessary parameters permanently.
Other case is to run the Vault in Docker container (see docker/vault/vaul-docker-compose.yaml as example).
Or you may just create your own container and start/stop them manually every time you need one.
Anyway, the Vault in Docker-container works in the development mode. It means, all keys are store in the memory not in a
file.
You have to add configuration parameters every time.

The using Vault is disabled in the application.properties by default.

### What about RADIUS server?

The application implements a special authentication provider to use an external RADIUS server.
I have not found any simple RADIUS servers to be use specially for development.
So, the corresponding functionality is disabled in the application.properties by default, and you don't need the RADIUS
server for the local development.

## How to build the application?

When the application is configured it's time to build one:

```
$ mvn clean package
```

or to skip the tests:

```
$ mvn clean package -Dskip.Tests
```

## How to run the application?

### Run the application locally

Before to run the application locally be sure the PostgreSQL is running and the database is created:

```
$ mvn spring-boot:run
```

### Run the application in Docker

Use the docker-compose.yaml in the project root. The configuration describes all needed services and uses the .env file
to configure PostgreSQL and the application with default settings.

```
$ mvn clean package
$ docker compose up --build
```

The application is available at http://localhost:8888/wasp (by default).

The actuator is available at http://localhost:8889/actuator (by default)
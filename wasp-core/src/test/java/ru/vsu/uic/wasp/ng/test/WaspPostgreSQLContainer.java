package ru.vsu.uic.wasp.ng.test;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public class WaspPostgreSQLContainer extends PostgreSQLContainer<WaspPostgreSQLContainer> {

    private static final String IMAGE_VERSION = "postgres:16-alpine";
    private static WaspPostgreSQLContainer container;

    private WaspPostgreSQLContainer() {
        super(IMAGE_VERSION);
    }

    public static WaspPostgreSQLContainer getInstance() {
        if (container == null) {
            // The database name, database username and the password will be created automatically in the container.
            // The information will be passed to Spring configuration via the @ServiceConnection annotation
            // in the test classes!
            container = new WaspPostgreSQLContainer().withCopyFileToContainer(
                    MountableFile.forClasspathResource("init-db.sql"), "/docker-entrypoint-initdb.d/"
            );
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        // do nothing to not spin up a new docker container.
        // JVM handles shutdown at the end of test.
    }
}

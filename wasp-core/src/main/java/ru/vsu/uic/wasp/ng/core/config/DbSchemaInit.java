package ru.vsu.uic.wasp.ng.core.config;

import liquibase.change.DatabaseChange;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The idea of the class is to create a database schema right before Liquibase starts to process change sets.
 * See https://stackoverflow.com/questions/52517529/how-to-create-schema-in-postgres-db-before-liquibase-start-to-work
 */
@Configuration
@ConditionalOnClass({SpringLiquibase.class, DatabaseChange.class})
@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({DbSchemaInit.SpringLiquibaseDependsOnPostProcessor.class})
@Slf4j
public class DbSchemaInit {

    @Component
    @ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", matchIfMissing = true)
    public static class SchemaInitBean implements InitializingBean {

        private final DataSource dataSource;
        private final String databaseSchemaName;

        public SchemaInitBean(DataSource dataSource, @Value("${wasp.db.schema}") String databaseSchemaName) {
            this.dataSource = dataSource;
            this.databaseSchemaName = databaseSchemaName;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                log.info("Create database extension 'uuid-ossp' if needed...");
                statement.execute("create extension if not exists \"uuid-ossp\"");
                log.info("Create the database schema '{}' if needed...", databaseSchemaName);
                statement.execute("create schema if not exists %s".formatted(databaseSchemaName));
            } catch (SQLException e) {
                log.error("{}", e.getMessage(), e);
                throw new RuntimeException("Failed to create extension 'uuid-ossp' or database schema '%s'.".formatted(databaseSchemaName), e);
            }

        }
    }

    @ConditionalOnBean(SchemaInitBean.class)
    static class SpringLiquibaseDependsOnPostProcessor extends AbstractDependsOnBeanFactoryPostProcessor {

        protected SpringLiquibaseDependsOnPostProcessor() {
            super(SpringLiquibase.class, SchemaInitBean.class);
        }
    }

}

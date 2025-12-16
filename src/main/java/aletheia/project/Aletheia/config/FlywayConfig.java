package aletheia.project.Aletheia.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public CommandLineRunner runFlyway(DataSource dataSource) {
        return args -> {
            System.out.println("========================================");
            System.out.println("MANUALLY RUNNING FLYWAY MIGRATION!");
            System.out.println("========================================");
            
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .baselineDescription("Initial")
                .load();
            
            // Run migrations
            int migrationsExecuted = flyway.migrate().migrationsExecuted;
            
            System.out.println("========================================");
            System.out.println("FLYWAY COMPLETED! Migrations executed: " + migrationsExecuted);
            System.out.println("========================================");
        };
    }
}

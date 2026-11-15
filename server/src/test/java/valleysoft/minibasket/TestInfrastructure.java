package valleysoft.minibasket;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestInfrastructure {

    static final String BUCKET = "test-bucket";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:8-alpine")).withExposedPorts(6379);
    }

    @Bean
    GenericContainer<?> minioContainer() {
        return new GenericContainer<>(DockerImageName.parse("bitnamilegacy/minio:2025.5.24"))
                .withEnv("MINIO_ROOT_USER", "test-user")
                .withEnv("MINIO_ROOT_PASSWORD", "test-secret")
                .withEnv("MINIO_DEFAULT_BUCKETS", BUCKET + ":download")
                .withExposedPorts(9000)
                .waitingFor(Wait.forLogMessage(".*\\*\\* Starting MinIO \\*\\*.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(2)));
    }

    @Bean
    DynamicPropertyRegistrar storageProperties(GenericContainer<?> minioContainer) {
        return registry -> {
            String endpoint = "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000);
            registry.add("app.storage.endpoint", () -> endpoint);
            registry.add("app.storage.public-url", () -> endpoint + "/" + BUCKET);
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    GreenMail greenMail() {
        return new GreenMail(ServerSetupTest.SMTP.dynamicPort());
    }

    @Bean
    DynamicPropertyRegistrar mailProperties(GreenMail greenMail) {
        return registry -> registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
    }
}

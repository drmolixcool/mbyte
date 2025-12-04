package fr.jayblanc.mbyte.manager.store.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import fr.jayblanc.mbyte.manager.store.StoreProvider;
import fr.jayblanc.mbyte.manager.store.StoreProviderDbConfig;
import fr.jayblanc.mbyte.manager.store.StoreProviderException;
import io.quarkus.runtime.ConfigConfig;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class DockerStoreProvider implements StoreProvider {

    private static final Logger LOGGER = Logger.getLogger(DockerStoreProvider.class.getName());
    private static final String NAME = "docker";
    private static final String DB_STORE_PREFIX = "store_";
    private static final String CREATE_ROLE_QUERY = "DO $$ IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolename = '%s') THEN CREATE ROLE %s LOGIN PASSWORD '%s'; END IF; END$$;";
    private static final String CREATE_DB_QUERY = "DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_database WHERE databasename = '%s') THEN PERFORM d.datistemplate FROM pg_database d WHERE d.databasename = 'template1'; CREATE DATABASE %s OWNER %s; END IF; END$$;";
    private static final String DROP_ROLE_QUERY = "DROP DATABASE %s;";
    private static final String DROP_DB_QUERY = "DROP ROLE %s;";


    private DockerClient client;

    @Inject StoreProviderDbConfig dbConfig;
    @Inject DockerStoreProviderConfig config;

    @PostConstruct
    private void init() {
        DockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(config.server())
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(clientConfig.getDockerHost())
                .sslConfig(clientConfig.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        client = DockerClientImpl.getInstance(clientConfig, httpClient);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public List<String> listApps() {
        LOGGER.log(Level.INFO, "Listing store apps");
        return client.listContainersCmd().exec().stream().map(container -> Arrays.stream(container.getNames()).collect(Collectors.joining()) + " / " + container.getImage()).collect(Collectors.toList());
    }

    @Override
    public String createApp(String id, String owner, String name) {
        LOGGER.log(Level.INFO, "Creating new store app");
        try {
            this.createDbForStore(id, "Tagada54");
            // TODO Create the store using docker
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to create database for store", e);
        }
        return id;
    }

    @Override public void destroyApp(String id) throws StoreProviderException {
        LOGGER.log(Level.INFO, "Destroying store app");
        try {
            // TODO Stop and remove the store using docker
            this.dropDbForStore(id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to create database for store", e);
        }
    }

    private void createDbForStore(String id, String secret) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbConfig.url(), dbConfig.user(), dbConfig.password());
                Statement st = conn.createStatement()) {
            st.execute(CREATE_ROLE_QUERY.formatted(DB_STORE_PREFIX.concat(id), DB_STORE_PREFIX.concat(id), secret));
            st.execute(CREATE_DB_QUERY.formatted(DB_STORE_PREFIX.concat(id), DB_STORE_PREFIX.concat(id), DB_STORE_PREFIX.concat(id)));
        }
    }

    private void dropDbForStore(String id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbConfig.url(), dbConfig.user(), dbConfig.password());
                Statement st = conn.createStatement()) {
            st.execute(DROP_ROLE_QUERY.formatted(DB_STORE_PREFIX.concat(id)));
            st.execute(DROP_DB_QUERY.formatted(DB_STORE_PREFIX.concat(id)));
        }
    }
}

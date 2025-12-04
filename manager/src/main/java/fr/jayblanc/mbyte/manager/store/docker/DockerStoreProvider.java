package fr.jayblanc.mbyte.manager.store.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import fr.jayblanc.mbyte.manager.store.StoreProvider;
import fr.jayblanc.mbyte.manager.store.StoreProviderException;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class DockerStoreProvider implements StoreProvider {

    private static final Logger LOGGER = Logger.getLogger(DockerStoreProvider.class.getName());
    private static final String NAME = "docker";

    private DockerClient client;

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
        LOGGER.log(Level.INFO, "Existing networks: {}", client.listNetworksCmd().exec().stream().map(Network::getName).collect(Collectors.joining(", ")));
        List<Network> networks = client.listNetworksCmd().withNameFilter("mbyte_network").exec();
        if (networks.isEmpty()) {
            LOGGER.log(Level.SEVERE, "MByte network not found, cannot create store app");
            return null;
        }
        if (networks.size() > 1) {
            LOGGER.log(Level.WARNING, "Multiple existing MByte networks found, using the first one");
        }
        /*
        CreateContainerResponse container = client.createContainerCmd("prestashop:latest")
            .withName("store-" + id)
            .withEnv(
                    "DB_SERVER=" + dbConfig.host(),
                    "DB_NAME=" + DB_STORE_PREFIX + id,
                    "DB_USER=" + DB_STORE_PREFIX + id,
                    "DB_PASSWD=" + secret
            )
            .withNetworkingConfig(new NetworkingConfig()
                    .withEndpointsConfig(Map.of(network.getId(), new EndpointConfig())))
            .exec();

         */
        return id;
    }

    @Override public void destroyApp(String id) throws StoreProviderException {
        LOGGER.log(Level.INFO, "Destroying store app");
    }

}

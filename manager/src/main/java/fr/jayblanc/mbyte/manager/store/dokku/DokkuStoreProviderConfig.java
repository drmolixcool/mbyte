package fr.jayblanc.mbyte.manager.store.dokku;

import io.smallrye.config.ConfigMapping;

/**
 * @author Jerome Blanchard
 */
@ConfigMapping(prefix = "manager.store.provider.dokku")
public interface DokkuStoreProviderConfig {

    String host();

    int port();

    String image();

}

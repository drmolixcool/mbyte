package fr.jayblanc.mbyte.manager.store;

import io.smallrye.config.ConfigMapping;

/**
 * @author Jerome Blanchard
 */
@ConfigMapping(prefix = "manager.store.provider.db")
public interface StoreProviderDbConfig {

    String url();

    String user();

    String password();

}

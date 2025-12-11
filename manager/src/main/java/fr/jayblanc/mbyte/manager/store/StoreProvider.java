package fr.jayblanc.mbyte.manager.store;

import java.util.List;

public interface StoreProvider {

    String name();

    List<String> listAllStores() throws StoreProviderException;

    String createStore(String id, String owner, String name) throws StoreProviderException;

    String destroyStore(String id) throws StoreProviderException;

}

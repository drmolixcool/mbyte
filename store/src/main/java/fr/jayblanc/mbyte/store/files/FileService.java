package fr.jayblanc.mbyte.store.files;

import fr.jayblanc.mbyte.store.data.exception.DataNotFoundException;
import fr.jayblanc.mbyte.store.data.exception.DataStoreException;
import fr.jayblanc.mbyte.store.files.entity.Node;
import fr.jayblanc.mbyte.store.files.exceptions.*;
import fr.jayblanc.mbyte.store.notification.NotificationServiceException;

import java.io.InputStream;
import java.util.List;

public interface FileService {

    String ROOT_NODE_ID = "root";
    String TREE_NODE_MIMETYPE = "application/fs-folder";

    List<Node> list(String id) throws NodeNotFoundException;

    List<Node> path(String id) throws NodeNotFoundException;

    Node get(String id) throws NodeNotFoundException;

    InputStream getContent(String id) throws NodeNotFoundException, NodeTypeException, DataNotFoundException, DataStoreException;

    String add(String parent, String name) throws NodeNotFoundException, NodeAlreadyExistsException, NodeTypeException,
            NodePersistenceException, NotificationServiceException;

    String add(String parent, String name, InputStream content) throws NodeNotFoundException, NodeAlreadyExistsException, NodeTypeException, DataStoreException, DataNotFoundException, NodePersistenceException, NotificationServiceException;

    void remove(String parent, String name) throws NodeNotFoundException, NodeNotEmptyException, NodeTypeException, DataStoreException, NodePersistenceException, NotificationServiceException;

    String getFullPath(List<Node> nodesPath);

    List<Node> findAll() throws NodeNotFoundException;

}

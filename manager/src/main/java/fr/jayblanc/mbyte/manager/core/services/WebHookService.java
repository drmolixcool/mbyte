package fr.jayblanc.mbyte.manager.core.services;

import fr.jayblanc.mbyte.manager.core.AccessDeniedException;
import fr.jayblanc.mbyte.manager.core.entity.WebHook;
import fr.jayblanc.mbyte.manager.core.exceptions.WebHookNotFoundException;

import java.util.List;

public interface WebHookService {
    List<WebHook> listWebHooks(String owner) throws AccessDeniedException;
    String createWebHook(String owner, String url, List<String> events) throws AccessDeniedException;
    WebHook getWebHook(String id) throws WebHookNotFoundException, AccessDeniedException;
    WebHook updateWebHook(String id, String url, boolean active, List<String> events) throws WebHookNotFoundException, AccessDeniedException;
    void deleteWebHook(String id) throws WebHookNotFoundException, AccessDeniedException;
}
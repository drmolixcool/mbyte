package fr.jayblanc.mbyte.manager.core.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "webhooks")
public class WebHook {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "secret")
    private String secret;

    @Column(name = "active")
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhooks_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event")
    private Set<String> events;

    public WebHook() {
        this.id = UUID.randomUUID().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Set<String> getEvents() { return events; }
    public void setEvents(Set<String> events) { this.events = events; }
}
# Local environment bootstrap (mbyte)

This project uses Docker Compose with a `mbyte` network, Postgres, and Keycloak. Follow these steps to initialize a clean environment, run the SQL init script, and import the `mbyte` Keycloak realm.

## Prerequisites
- Docker and Docker Compose installed
- Sudo access to create the Postgres data directory

## Steps

1) Create the host directory for Postgres data and set permissions

```bash
sudo mkdir -p /var/local/mbyte
# UID/GID for postgres in the official image is typically 999:999
sudo chown 999:999 /var/local/mbyte
```

2) Reset the Postgres volume (optional but required if you want to re-run the init)

```bash
docker compose down
docker volume rm miage.24_postgres-data || true
```

3) Start only the database to execute the SQL init script

```bash
docker compose up -d db
docker compose logs -f db
```

The `provisioning/init.sql` script creates the `store` and `keycloak` databases and grants privileges to the Postgres user `mbyte`.

4) Verify that the databases are created and accessible

```bash
docker exec -it miage.24-db-1 psql -U mbyte -d postgres -c "\l"
```

5) Start Keycloak and import the `mbyte` realm

```bash
docker compose up -d keycloak
docker compose logs -f keycloak
```

Keycloak is configured to automatically import `provisioning/mbyte-realm.json` on startup.

6) Start other services if needed (e.g., Traefik)

```bash
docker compose up -d traefik
```

## Notes
- The SQL init script in `/docker-entrypoint-initdb.d/` only runs if the Postgres data directory is empty on first startup.
- If you already had data, remove the `miage.24_postgres-data` volume (see step 2) to re-run the init.
- The Docker network used is `mbyte` (bridge, subnet 172.25.0.0/24).
- The named volume `postgres-data` is a bind mount to `/var/local/mbyte`.
- DB variables for Keycloak: `KC_DB_USERNAME=mbyte`, database `keycloak`.

## Quick troubleshooting
- Postgres directory permissions:
```bash
ls -la /var/local/mbyte
sudo chown 999:999 /var/local/mbyte
```
- Inspect the Docker volume:
```bash
docker volume inspect miage.24_postgres-data
```
- Full redeploy:
```bash
docker compose pull
docker compose up -d
```

7) Install Dnsmasq if not already installed

```bash
sudo apt-get install dnsmasq
``` 

8) Configure Dnsmasq for local domain resolution

Create file `/etc/dnsmasq.d/mbyte.conf` with the following content:

```bash
address=/mbyte.dev.local/127.0.0.1
address=/mbyte.dev.local/::1
```

Check file `/etc/dnsmask.com` it should contain:

```bash
listen-address=127.0.0.1
bind-interfaces
``` 

Restart Dnsmasq service:

```bash
sudo systemctl restart dnsmasq
``` 

/*
 * Copyright (C) 2025 Jerome Blanchard <jayblanc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.jayblanc.mbyte.store.auth;

import fr.jayblanc.mbyte.store.auth.entity.Account;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
public class AuthenticationServiceBean implements AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    @Inject JsonWebToken accessToken;
    @Inject SecurityIdentity identity;
    @Inject AuthenticationConfig config;

    private static final Map<String, Account> profilesCache = new HashMap<>();
    static {
        profilesCache.put(UNAUTHENTIFIED_IDENTIFIER, ANONYMOUS_PROFILE);
    }

    @Override
    public boolean isAuthentified() {
        return !identity.isAnonymous();
    }

    @Override
    public String getConnectedIdentifier() {
        return isAuthentified()?identity.getPrincipal().getName():AuthenticationService.UNAUTHENTIFIED_IDENTIFIER;
    }

    @Override
    public Account getConnectedProfile() {
        LOGGER.log(Level.INFO, "Getting connected profile");
        String connectedId = getConnectedIdentifier();
        Account profile;
        if ( profilesCache.containsKey(connectedId) ) {
            profile = profilesCache.get(connectedId);
        } else {
            profile = new Account();
            profile.setId(connectedId);
            profilesCache.put(connectedId, profile);
        }
        profile.setRoles(identity.getRoles().stream().toList());
        if (accessToken.containsClaim("preferred_username")) {
            profile.setUsername(accessToken.getClaim("preferred_username"));
            if ( profile.getUsername().equals(config.owner()) ) {
                LOGGER.log(Level.INFO, "User is owner: " + profile.getUsername());
                profile.setOwner(true);
            } else {
                LOGGER.log(Level.INFO, "User is NOT owner: " + profile.getUsername());
                profile.setOwner(false);
            }
            profile.setFullname(accessToken.getClaim("given_name") + " " + accessToken.getClaim("family_name"));
            profile.setEmail(accessToken.getClaim("email"));
        } else {
            profile.setOwner(false);
            LOGGER.log(Level.INFO, "Unable to get claims from token");
        }
        return profile;
    }



}

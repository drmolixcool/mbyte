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
package fr.jayblanc.mbyte.manager.auth;

import fr.jayblanc.mbyte.manager.auth.entity.Profile;
import io.quarkus.oidc.IdToken;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
public class AuthenticationServiceBean implements AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    @Inject @IdToken JsonWebToken idToken;
    @Inject SecurityIdentity identity;
    @Inject AuthenticationConfig config;
    @Inject EntityManager em;

    @Override
    public boolean isAuthentified() {
        return !identity.isAnonymous();
    }

    @Override
    public String getConnectedIdentifier() {
        return isAuthentified()?identity.getPrincipal().getName():AuthenticationService.UNAUTHENTIFIED_IDENTIFIER;
    }

    @Override
    public boolean isConnectedIdentifierInRoleAdmin() {
        return identity.hasRole(config.adminRoleName());
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public Profile getConnectedProfile() {
        LOGGER.log(Level.FINE, "Getting connected profile");
        String connectedId = getConnectedIdentifier();
        LOGGER.log(Level.FINE, "Loading profile for connected identifier: " + connectedId);
        Profile profile = em.find(Profile.class, connectedId);
        if ( profile == null ) {
            Profile newprofile = new Profile();
            newprofile.setId(connectedId);
            if (idToken.containsClaim("preferred_username")) {
                newprofile.setUsername(idToken.getClaim("preferred_username"));
            }
            if (idToken.containsClaim("given_name") && idToken.containsClaim("family_name")) {
                newprofile.setFullname(idToken.getClaim("given_name") + " " + idToken.getClaim("family_name"));
            }
            if (idToken.containsClaim("email")) {
                newprofile.setEmail(idToken.getClaim("email"));
            }
            em.persist(newprofile);
            LOGGER.log(Level.FINE, "Profile created for identifier: " + connectedId);
            profile = newprofile;
        }
        LOGGER.log(Level.FINE, "Profile retrieved for connected identifier: " + profile);
        return profile;
    }

}

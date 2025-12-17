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
package fr.jayblanc.mbyte.store.notification;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jerome Blanchard
 */
@QuarkusTest
public class NotificationServiceTest {

    private static final Logger LOGGER = Logger.getLogger(NotificationServiceTest.class.getName());

    @Inject NotificationService notification;

    @Inject UserTransaction userTx;

    @Test
    @Transactional
    public void simpleThrowEventTest() throws NotificationServiceException {
        LOGGER.log(Level.INFO, "Starting Simple Throw Event Test");
        notification.notify("data.create", "data.id.1");
        notification.notify("data.create", "data.id.2");
        notification.notify("data.delete", "data.id.2");
    }

    @Test
    public void txThrowEvent() throws Exception {
        LOGGER.log(Level.INFO, "Starting Transactionnal Throw Event");
        userTx.begin();
        notification.notify("data.create", "data.id.1");
        notification.notify("data.create", "data.id.2");
        userTx.commit();
        Thread.sleep(1000);
        userTx.begin();
        notification.notify("data.create", "data.id.3");
        userTx.rollback();
        Thread.sleep(1000);
        userTx.begin();
        notification.notify("data.create", "data.id.4");
        notification.notify("data.create", "data.id.5");
        notification.notify("data.create", "data.id.6");
        notification.notify("data.create", "data.id.7");
        userTx.commit();
    }


}

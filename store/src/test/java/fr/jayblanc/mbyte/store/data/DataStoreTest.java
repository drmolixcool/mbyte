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
package fr.jayblanc.mbyte.store.data;

import fr.jayblanc.mbyte.store.data.exception.DataNotFoundException;
import fr.jayblanc.mbyte.store.data.exception.DataStoreException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jerome Blanchard
 */
@QuarkusTest
public class DataStoreTest {

    private static final Logger LOGGER = Logger.getLogger(DataStoreTest.class.getName());

    @Inject
    DataStore store;

    @Test
    void testExists() {

    }

    @Test
    public void simpleCreateFileTest() throws DataStoreException, DataNotFoundException, IOException {
        LOGGER.log(Level.INFO, "Starting Simple Create File Test");
        String content = "This is a test";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        String KEY = store.put(inputStream);
        LOGGER.log(Level.INFO, "File stored with key: " + KEY);
        assertNotNull(KEY);
        assertTrue(store.exists(KEY));
        assertEquals(14, store.size(KEY));
        assertEquals("text/plain", store.type(KEY, "test.txt"));
        InputStream inputStream1 = store.get(KEY);
        String retrieved = new String(IOUtils.toByteArray(inputStream1));
        assertEquals(content, retrieved);
    }

    @Test
    public void getUnexistingFileTest() throws DataStoreException, DataNotFoundException, IOException {
        assertThrows(DataNotFoundException.class, () -> {
            store.get("ID_QUI_NEXISTE_PAS");
        });
    }

    @Test
    public void deduplicatedCreateFileTest() throws DataStoreException, IOException, DataNotFoundException {
        LOGGER.log(Level.INFO, "Starting Simple Create File Test");
        String content = "This is a test";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        String KEY = store.put(inputStream);
        LOGGER.log(Level.INFO, "File stored with key: " + KEY);
        assertNotNull(KEY);
        assertTrue(store.exists(KEY));
        assertEquals(14, store.size(KEY));

        InputStream inputStream1 = store.get(KEY);
        String retrieved = new String(IOUtils.toByteArray(inputStream1));
        assertEquals(content, retrieved);

        //Putting the same data should produce the same ID
        inputStream = new ByteArrayInputStream(content.getBytes());
        String KEY2 = store.put(inputStream);
        assertEquals(KEY, KEY2);
    }

}

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

import java.io.InputStream;

/**
 * @author Jerome Blanchard
 */
public interface DataStore {

    boolean exists(String key);

    String put(InputStream is) throws DataStoreException;

    InputStream get(String key) throws DataStoreException, DataNotFoundException;

    String type(String key, String name) throws DataStoreException, DataNotFoundException;

    long size(String key) throws DataStoreException, DataNotFoundException;

    String extract(String key, String name, String type) throws DataStoreException, DataNotFoundException;

    void delete(String key) throws DataStoreException;

}

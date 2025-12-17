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
package fr.jayblanc.mbyte.store.data.hash;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author Jerome Blanchard (jerome.blanchard@fairandsmart.com)
 * @version 1.0
 */
public abstract class HashedFilterInputStream extends FilterInputStream {

	protected HashedFilterInputStream(InputStream in) {
		super(in);
	}

	public abstract String getHash();

	public static HashedFilterInputStream SHA256(InputStream is) throws NoSuchAlgorithmException {
		return new SHA256FilterInputStream(is);
	}

}

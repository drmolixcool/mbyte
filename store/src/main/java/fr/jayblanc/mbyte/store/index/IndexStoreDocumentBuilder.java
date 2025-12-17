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
package fr.jayblanc.mbyte.store.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class IndexStoreDocumentBuilder {

    public static final String TYPE_FIELD = "TYPE";
    public static final String IDENTIFIER_FIELD = "IDENTIFIER";
    public static final String SCOPE_FIELD = "SCOPE";
    public static final String CONTENT_FIELD = "CONTENT";

    public static Document buildDocument(IndexableContent object) {
        Document document = new Document();
        document.add(new Field(TYPE_FIELD, object.getType(), StringField.TYPE_STORED));
        document.add(new Field(IDENTIFIER_FIELD, object.getIdentifier(), StringField.TYPE_STORED));
        document.add(new Field(SCOPE_FIELD, object.getScope().name(), StringField.TYPE_STORED));
        document.add(new Field(CONTENT_FIELD, object.getContent(), TextField.TYPE_STORED));
        return document;
    }

}

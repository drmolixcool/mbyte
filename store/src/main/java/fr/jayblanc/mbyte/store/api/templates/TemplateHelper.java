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
package fr.jayblanc.mbyte.store.api.templates;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class TemplateHelper {

    public static String toIconName(String mimetype) {
        String icon = "fa-file";
        if ( mimetype.startsWith("image") ) icon =  "fa-file-image";
        if ( mimetype.startsWith("audio") ) icon =  "fa-file-audio";
        if ( mimetype.startsWith("video") ) icon =  "fa-file-video";
        if ( mimetype.equals("application/fs-folder") ) icon =  "fa-folder";
        if ( mimetype.equals("application/pdf") ) icon =  "fa-file-pdf";
        if ( mimetype.equals("text/plain") ) icon =  "fa-file-text";
        if ( mimetype.equals("text/html") ) icon =  "fa-file-code";
        if ( mimetype.equals("application/json") ) icon =  "fa-file-code";
        if ( mimetype.equals("application/gzip") ) icon =  "fa-file-archive";
        if ( mimetype.equals("application/zip") ) icon =  "fa-file-archive";
        if ( mimetype.equals("application/msword") ) icon =  "fa-file-word";
        if ( mimetype.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ) icon =  "fa-file-word";
        if ( mimetype.equals("application/vnd.ms-excel") ) icon =  "fa-file-excel";
        if ( mimetype.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ) icon =  "fa-file-excel";
        if ( mimetype.equals("application/vnd.ms-powerpoint") ) icon =  "fa-file-powerpoint";
        if ( mimetype.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ) icon =  "fa-file-powerpoint";
        return icon;
    }

    public static String toHumanRead(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp-1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}

/*
 * @(#)DownloadUtil.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.presentationTier;


import javax.servlet.http.HttpServletRequest;

import pt.ist.fenixframework.DomainObject;


/**
 * 
 * @author Sérgio Silva
 * @author Luis Cruz
 * 
 */
public class DownloadUtil {

    public static String getDownloadUrl(final HttpServletRequest request, final DomainObject domainObject) {
        return getDownloadUrl(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(),
                domainObject);
    }

    static String getDownloadUrl(final String scheme, final String servername, final int serverPort,
            final String contextPath, final DomainObject domainObject) {
        final StringBuilder url = new StringBuilder();
        url.append(scheme);
        url.append("://");
        url.append(servername);
        if (serverPort > 0 && serverPort != 80 && serverPort != 443) {
            url.append(":");
            url.append(serverPort);
        }
        url.append(contextPath);
        url.append("/download/");
        url.append(domainObject.getExternalId());
        return url.toString();
    }
}

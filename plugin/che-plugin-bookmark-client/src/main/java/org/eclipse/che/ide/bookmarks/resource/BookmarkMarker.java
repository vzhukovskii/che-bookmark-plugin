/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.bookmarks.resource;

import org.eclipse.che.ide.api.resources.marker.Marker;

/**
 * Marker indicates that specific resource is marked as a bookmark.
 *
 * @author Vlad Zhukovskiy
 */
public class BookmarkMarker implements Marker {

    public static final String ID = "bookmarkMarker";

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return ID;
    }
}

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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.bookmarks.storage.BookmarksStorage;

/**
 * Intercepts given resource and mark it with {@link BookmarkMarker} if it found in {@link BookmarksStorage}.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class BookmarksInterceptor implements ResourceInterceptor {

    private final BookmarksStorage bmStorage;

    @Inject
    public BookmarksInterceptor(BookmarksStorage bmStorage) {
        this.bmStorage = bmStorage;
    }

    /** {@inheritDoc} */
    @Override
    public void intercept(Resource resource) {
        if (bmStorage.contains(resource.getLocation()) && !resource.getMarker(BookmarkMarker.ID).isPresent()) {
            resource.addMarker(new BookmarkMarker());
        }
    }
}

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
package org.eclipse.che.ide.bookmarks.storage;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.resource.Path;

/**
 * Bookmarks storage. Manages bookmarks by adding and removing the las ones.
 * <p/>
 * There is only the implementation which stores bookmarks in the user's profile.
 *
 * @author Vlad Zhukovskiy
 * @see BookmarksStorageImpl
 */
@ImplementedBy(BookmarksStorageImpl.class)
public interface BookmarksStorage {

    /**
     * Adds the new path to the storage.
     *
     * @param path
     *         the path to mark as bookmark
     * @return true if the path has added, otherwise false
     * @see Path
     */
    boolean add(Path path);

    /**
     * Removes the given path from the storage.
     *
     * @param path
     *         the path to remove from the bookmarks
     * @return true if path has removed, otherwise false
     * @see Path
     */
    boolean remove(Path path);

    /**
     * Returns all stored paths in the storage.
     *
     * @return the path array
     * @see Path
     */
    Path[] getAll();

    /**
     * Checks whether given path is stored in the bookmarks storage.
     *
     * @param path
     *         the path to check
     * @return true if path was stored in the bookmarks storage
     */
    boolean contains(Path path);
}

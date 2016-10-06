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
package org.eclipse.che.ide.bookmarks.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Bookmarks updated event describes general event when bookmarks storage has been updated.
 *
 * @author Vlad Zhukovskiy
 */
public class BookmarksUpdatedEvent extends GwtEvent<BookmarksUpdatedEvent.BookmarksUpdatedHandler> {

    /**
     * A listener is notified of changes in bookmarks storage. These changes arise from manipulation of bookmarks storage.
     */
    public interface BookmarksUpdatedHandler extends EventHandler {

        /**
         * Notifies the listener that bookmarks storage has been updated.
         *
         * @param event
         *         instance of {@link BookmarksUpdatedEvent}
         */
        void onBookmarksUpdated(BookmarksUpdatedEvent event);
    }

    private static Type<BookmarksUpdatedHandler> TYPE;

    public static Type<BookmarksUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<BookmarksUpdatedHandler>();
        }
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    public Type<BookmarksUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(BookmarksUpdatedHandler handler) {
        handler.onBookmarksUpdated(this);
    }
}

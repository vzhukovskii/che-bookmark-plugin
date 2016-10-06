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
package org.eclipse.che.ide.bookmarks;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants.
 *
 * @author Vlad Zhukovskiy
 */
public interface BookmarksLocalizationConstant extends Messages {

    @DefaultMessage("{0} items ")
    String itemsMarked(int count);

    @DefaultMessage("Bookmarks")
    String bookmarks();

    @DefaultMessage("Show Bookmarks")
    String showBookmarksAction();

    @DefaultMessage("Managing your saved bookmarks")
    String showBookmarksActionDescription();

    @DefaultMessage("Toggle Bookmark")
    String toggleBookmarkAction();

    @DefaultMessage("Toggle bookmark on current selected resource")
    String toggleBookmarkActionDescription();

    @DefaultMessage("Managing you saved bookmarks")
    String bookmarksTitle();
}

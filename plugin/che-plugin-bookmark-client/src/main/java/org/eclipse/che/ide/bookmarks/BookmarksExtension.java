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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.bookmarks.actions.ToggleBookmarkAction;
import org.eclipse.che.ide.bookmarks.actions.ShowBookmarksAction;
import org.eclipse.che.ide.util.input.KeyCodeMap;

/**
 * Extension entry point.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
@Extension(title = "Bookmarks", version = "1.0")
public class BookmarksExtension {

    public static final String PREF_KEY = "bookmarks";

    @Inject
    public BookmarksExtension(ActionManager actionManager,
                              ToggleBookmarkAction toggleBookmarkAction,
                              ShowBookmarksAction showBookmarksAction,
                              KeyBindingAgent keyBindingAgent) {

        //register actions
        actionManager.registerAction(ToggleBookmarkAction.ID, toggleBookmarkAction);
        actionManager.registerAction(ShowBookmarksAction.ID, showBookmarksAction);

        DefaultActionGroup bookmarksGroup = new DefaultActionGroup("Bookmarks", true, actionManager);
        bookmarksGroup.add(toggleBookmarkAction);
        bookmarksGroup.add(showBookmarksAction);

        DefaultActionGroup editGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDIT);
        editGroup.add(bookmarksGroup, Constraints.LAST);

        keyBindingAgent.getGlobal().addKey(new KeyBuilder().action().charCode(KeyCodeMap.F11).build(), ToggleBookmarkAction.ID);
        keyBindingAgent.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F11).build(), ShowBookmarksAction.ID);
    }
}

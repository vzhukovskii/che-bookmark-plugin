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
package org.eclipse.che.ide.bookmarks.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.bookmarks.BookmarksLocalizationConstant;
import org.eclipse.che.ide.bookmarks.resource.BookmarkMarker;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Toggle Bookmark on current resource.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ToggleBookmarkAction extends AbstractPerspectiveAction {

    public static final String ID = "toggleBookmark";

    private final AppContext appContext;

    @Inject
    public ToggleBookmarkAction(AppContext appContext, BookmarksLocalizationConstant locale) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), locale.toggleBookmarkAction(), locale.toggleBookmarkActionDescription(), null, null);
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);

        final Resource[] resources = appContext.getResources();

        event.getPresentation().setEnabled(resources != null && resources.length == 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource[] resources = appContext.getResources();

        checkState(resources != null && resources.length == 1);

        final boolean present = resources[0].getMarker(BookmarkMarker.ID).isPresent();

        if (present) {
            resources[0].deleteMarker(BookmarkMarker.ID);
        } else {
            resources[0].addMarker(new BookmarkMarker());
        }
    }
}

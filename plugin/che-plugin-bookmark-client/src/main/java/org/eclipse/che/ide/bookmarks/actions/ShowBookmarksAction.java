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
import org.eclipse.che.ide.bookmarks.BookmarksLocalizationConstant;
import org.eclipse.che.ide.bookmarks.manager.BookmarksPresenter;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Show Bookmarks action.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ShowBookmarksAction extends AbstractPerspectiveAction {

    public static final String ID = "showBookmarks";

    private final BookmarksPresenter presenter;

    @Inject
    public ShowBookmarksAction(BookmarksPresenter presenter, BookmarksLocalizationConstant locale) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), locale.showBookmarksAction(), locale.showBookmarksActionDescription(), null, null);
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.show();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(true);
    }
}

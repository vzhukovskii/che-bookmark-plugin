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
package org.eclipse.che.ide.bookmarks.manager;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.bookmarks.BookmarksLocalizationConstant;
import org.eclipse.che.ide.bookmarks.event.BookmarksUpdatedEvent;
import org.eclipse.che.ide.bookmarks.event.BookmarksUpdatedEvent.BookmarksUpdatedHandler;
import org.eclipse.che.ide.bookmarks.manager.BookmarksView.ActionDelegate;
import org.eclipse.che.ide.bookmarks.storage.BookmarksStorage;
import org.eclipse.che.ide.bookmarks.tree.BookmarkGroupNode.NodeFactory;
import org.eclipse.che.ide.resource.Path;

import java.util.Collections;
import java.util.List;

/**
 * Manage bookmarks panel presenter. Displays current saved bookmarks.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class BookmarksPresenter extends BasePresenter implements ActionDelegate, BookmarksUpdatedHandler {

    private final BookmarksView                 view;
    private final BookmarksStorage              bmStorage;
    private final NodeFactory                   nodeFactory;
    private final SettingsProvider              settingsProvider;
    private final WorkspaceAgent                workspaceAgent;
    private final BookmarksLocalizationConstant locale;

    @Inject
    public BookmarksPresenter(BookmarksView view,
                              BookmarksStorage bmStorage,
                              EventBus eventBus,
                              SettingsProvider settingsProvider,
                              NodeFactory nodeFactory,
                              WorkspaceAgent workspaceAgent,
                              BookmarksLocalizationConstant locale) {
        this.view = view;
        this.bmStorage = bmStorage;
        this.nodeFactory = nodeFactory;
        this.settingsProvider = settingsProvider;
        this.workspaceAgent = workspaceAgent;
        this.locale = locale;
        this.view.setDelegate(this);

        workspaceAgent.getPartStack(PartStackType.TOOLING).addPart(this);

        eventBus.addHandler(BookmarksUpdatedEvent.getType(), this);
    }

    /**
     * Shows panel if it hasn't displayed yet or activate the las one if it isn't in focus.
     */
    public void show() {
        final PartPresenter activePart = partStack.getActivePart();

        if (activePart != null && activePart.equals(this)) {
            workspaceAgent.hidePart(this);
            return;
        }

        refreshView();
        workspaceAgent.setActivePart(this, PartStackType.TOOLING);
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return locale.bookmarks();
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return locale.bookmarksTitle();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onSelectionChanged(List<Node> nodes) {
        setSelection(new Selection<>(nodes));
    }

    /** {@inheritDoc} */
    @Override
    public void onBookmarksUpdated(BookmarksUpdatedEvent event) {
        refreshView();
    }

    /**
     * Refresh the bookmarks list in the view.
     */
    private void refreshView() {
        final Path[] paths = bmStorage.getAll();
        final NodeSettings settings = settingsProvider.getSettings();

        view.setBookmarks(Collections.<Node>singletonList(nodeFactory.newBookmarkGroupNode(paths, settings)));
    }
}

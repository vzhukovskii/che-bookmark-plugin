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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.bookmarks.BookmarksLocalizationConstant;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;

import java.util.List;

/**
 * Implementation of the {@link BookmarksView}.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class BookmarksViewImpl extends BaseView<BookmarksView.ActionDelegate> implements BookmarksView {

    private Tree tree;

    @Inject
    public BookmarksViewImpl(PartStackUIResources resources, BookmarksLocalizationConstant locale) {
        super(resources);

        setTitle(locale.bookmarks());

        tree = new Tree(new NodeStorage(), new NodeLoader());
        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            public void onSelectionChanged(SelectionChangedEvent event) {
                delegate.onSelectionChanged(event.getSelection());
            }
        });
        tree.setAutoSelect(true);

        setContentWidget(tree);
    }

    /** {@inheritDoc} */
    @Override
    public void setBookmarks(List<Node> nodes) {
        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(nodes);
        tree.expandAll();
    }
}

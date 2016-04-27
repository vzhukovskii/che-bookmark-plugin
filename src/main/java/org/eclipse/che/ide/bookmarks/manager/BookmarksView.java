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

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import java.util.List;

/**
 * View interface for the {@link BookmarksViewImpl}.
 *
 * @author Vlad Zhukovskiy
 */
@ImplementedBy(BookmarksViewImpl.class)
public interface BookmarksView extends View<BookmarksView.ActionDelegate> {

    /**
     * Sets the bookmarks list in the tree widget.
     *
     * @param nodes
     *         bookmark nodes
     */
    void setBookmarks(List<Node> nodes);

    /**
     * Sets the visibility status of current view.
     *
     * @param visible
     *         true if vew should be visible, otherwise false
     */
    void setVisible(boolean visible);

    /**
     * Interface for delegating events and signals from the view to the bound presenter.
     */
    interface ActionDelegate extends BaseActionDelegate {

        /**
         * Performs any operations when selection has changed.
         *
         * @param nodes
         *         nodes which has provided in new selection
         */
        void onSelectionChanged(List<Node> nodes);
    }

}

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
package org.eclipse.che.ide.bookmarks.tree;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;

/**
 * Node that represents bookmarked resource.
 *
 * @author Vlad Zhukovskiy
 */
public class BookmarkNode extends ResourceNode<Resource> implements HasAction {

    private final EventBus eventBus;
    private final PromiseProvider promises;

    @Inject
    protected BookmarkNode(@Assisted Resource resource,
                           @Assisted NodeSettings nodeSettings,
                           NodesResources nodesResources,
                           NodeFactory nodeFactory,
                           EventBus eventBus,
                           Set<NodeIconProvider> nodeIconProviders,
                           PromiseProvider promises) {
        super(resource, nodeSettings, nodesResources, nodeFactory, eventBus, nodeIconProviders);
        this.eventBus = eventBus;
        this.promises = promises;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return promises.resolve(Collections.<Node>emptyList());
    }

    /** {@inheritDoc} */
    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        super.updatePresentation(presentation);

        presentation.setInfoText(getData().getLocation().parent().toString());
        presentation.setInfoTextWrapper(Pair.of("(", ")"));
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed() {
        if (getData().getResourceType() == Resource.FILE) {
            eventBus.fireEvent(new FileEvent((File)getData(), OPEN));
        } else {
            eventBus.fireEvent(new RevealResourceEvent(getData()));
        }
    }
}

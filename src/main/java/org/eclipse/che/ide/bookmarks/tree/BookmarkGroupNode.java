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

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.workspace.Workspace;
import org.eclipse.che.ide.bookmarks.BookmarksLocalizationConstant;
import org.eclipse.che.ide.bookmarks.BookmarksResources;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

/**
 * Node that represent group of bookmarks.
 *
 * @author Vlad Zhukovskiy
 */
public class BookmarkGroupNode extends SyntheticNode<Void> {

    private final Path[]                        paths;
    private final PromiseProvider               promises;
    private final NodeFactory                   nodeFactory;
    private final Workspace                     workspace;
    private final BookmarksLocalizationConstant locale;
    private final BookmarksResources resources;

    @Inject
    public BookmarkGroupNode(@Assisted Path[] paths,
                             @Assisted NodeSettings nodeSettings,
                             PromiseProvider promises,
                             NodeFactory nodeFactory,
                             Workspace workspace,
                             BookmarksLocalizationConstant locale,
                             BookmarksResources resources) {
        super(null, nodeSettings);
        this.paths = paths;
        this.promises = promises;
        this.nodeFactory = nodeFactory;
        this.workspace = workspace;
        this.locale = locale;
        this.resources = resources;
    }

    /** {@inheritDoc} */
    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        if (paths == null || paths.length == 0) {
            return promises.resolve(Collections.<Node>emptyList());
        }

        int maxDepth = paths[0].segmentCount();

        for (int i = 1; i < paths.length; i++) {
            if (maxDepth < paths[i].segmentCount()) {
                maxDepth = paths[i].segmentCount();
            }
        }

        return workspace.getWorkspaceRoot().getTree(maxDepth).then(new Function<Resource[], List<Node>>() {
            @Override
            public List<Node> apply(Resource[] resources) throws FunctionException {
                final List<Node> nodes = newArrayListWithCapacity(paths.length);

                for (Path path : paths) {
                    for (Resource resource : resources) {
                        if (resource.getLocation().equals(path)) {
                            nodes.add(nodeFactory.newBookmarkNode(resource, getSettings()));
                        }
                    }
                }

                return nodes;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(locale.itemsMarked(paths != null ? paths.length : 0));
        presentation.setPresentableIcon(resources.bookmarksIcon());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return locale.bookmarks();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    public interface NodeFactory {
        BookmarkGroupNode newBookmarkGroupNode(Path[] paths, NodeSettings nodeSettings);

        BookmarkNode newBookmarkNode(Resource resource, NodeSettings nodeSettings);
    }
}

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

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.bookmarks.resource.BookmarksInterceptor;
import org.eclipse.che.ide.bookmarks.tree.BookmarkGroupNode.NodeFactory;

/**
 * Extension Gin module.
 *
 * @author Vlad Zhukovskiy
 */
@ExtensionGinModule
public class BookmarksGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(NodeFactory.class));
        GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(BookmarksInterceptor.class);
    }
}

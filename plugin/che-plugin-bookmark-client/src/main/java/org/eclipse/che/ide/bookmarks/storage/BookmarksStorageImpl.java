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
package org.eclipse.che.ide.bookmarks.storage;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent.MarkerChangedHandler;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent.WorkspaceReadyHandler;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.bookmarks.BookmarksExtension;
import org.eclipse.che.ide.bookmarks.event.BookmarksUpdatedEvent;
import org.eclipse.che.ide.bookmarks.resource.BookmarkMarker;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.ide.util.loging.Log;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.copyOf;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.bookmarks.BookmarksExtension.PREF_KEY;

/**
 * Default implementation of the {@link BookmarksStorage}.
 *
 * Stores bookmarked paths in the user's profile.
 *
 * @author Vlad Zhukovskiy
 * @see BookmarksStorage
 */
@Singleton
public class BookmarksStorageImpl implements BookmarksStorage,
                                             ResourceChangedHandler,
                                             MarkerChangedHandler,
                                             WorkspaceReadyHandler,
                                             WorkspaceStoppedEvent.Handler {

    private final PreferencesManager preferencesManager;
    private final EventBus           eventBus;

    private Path[] bookmarks;

    private static final Path[] EMPTY_BOOKMARKS = new Path[0];
    private static final char   DIVIDER         = '|';

    private Promise<Void> flushPromise;

    @Inject
    public BookmarksStorageImpl(PreferencesManager preferencesManager,
                                EventBus eventBus,
                                PromiseProvider promises) {
        this.preferencesManager = preferencesManager;
        this.eventBus = eventBus;

        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        eventBus.addHandler(MarkerChangedEvent.getType(), this);

        eventBus.addHandler(WorkspaceReadyEvent.getType(), this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);

        flushPromise = promises.resolve(null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(Path path) {
        checkState(bookmarks != null);

        if (Arrays.contains(bookmarks, path)) {
            return false;
        }

        bookmarks = Arrays.add(bookmarks, path);

        fireUpdatedEvent();
        flushStorage();

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Path path) {
        checkState(bookmarks != null);

        if (!Arrays.contains(bookmarks, path)) {
            return false;
        }

        bookmarks = Arrays.remove(bookmarks, path);

        fireUpdatedEvent();
        flushStorage();

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Path[] getAll() {
        checkState(bookmarks != null);

        return bookmarks;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Path path) {
        return bookmarks != null && Arrays.contains(bookmarks, path);
    }

    /** {@inheritDoc} */
    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();

        if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {
            onResourceMoved(delta);
        } else if (delta.getKind() == REMOVED) {
            onResourceRemoved(delta);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onMarkerChanged(MarkerChangedEvent event) {
        if (event.getMarker().getType().equals(BookmarkMarker.ID)) {
            onMarkerChanged(event.getStatus(), event.getResource());
        }
    }

    protected void onResourceMoved(ResourceDelta delta) {
        if (Arrays.contains(bookmarks, delta.getFromPath())) {

            bookmarks = Arrays.remove(bookmarks, delta.getFromPath());
            bookmarks = Arrays.add(bookmarks, delta.getToPath());

            fireUpdatedEvent();
            flushStorage();
        }
    }

    protected void onResourceRemoved(ResourceDelta delta) {
        final Resource resource = delta.getResource();

        if (contains(resource.getLocation())) {
            remove(resource.getLocation());
        }
    }

    protected void onMarkerChanged(int status, Resource resource) {
        switch (status) {
            case Marker.CREATED:
                add(resource.getLocation());
                break;
            case Marker.REMOVED:
                remove(resource.getLocation());
                break;
            case Marker.UPDATED:
                fireUpdatedEvent();
                break;
        }
    }

    protected void initStorage() {
        final String rawString = preferencesManager.getValue(PREF_KEY);

        if (isNullOrEmpty(rawString)) {
            bookmarks = EMPTY_BOOKMARKS;
        }

        bookmarks = decode(rawString);

        fireUpdatedEvent();
    }

    protected String encode(Path[] resources) {
        StringBuilder rawString = new StringBuilder();

        for (int i = 0; i < resources.length; i++) {
            rawString.append(resources[i].toString());

            if (i != resources.length - 1) {
                rawString.append(DIVIDER);
            }
        }

        return rawString.toString();
    }

    protected Path[] decode(String rawString) {
        Path[] paths = new Path[0];

        Iterable<String> strings = Splitter.on(DIVIDER).omitEmptyStrings().split(rawString);

        for (String s : strings) {
            if (Path.isValidPath(s)) {
                final int index = paths.length;
                paths = copyOf(paths, index + 1);
                paths[index] = Path.valueOf(s);
            } else {
                Log.info(this.getClass(), "Failed to deserialize path '" + s + "'.");
            }
        }

        return paths;
    }

    protected void flushStorage() {
        checkState(bookmarks != null);

        flushPromise.thenPromise(new Function<Void, Promise<Void>>() {
            public Promise<Void> apply(Void arg) throws FunctionException {
                preferencesManager.setValue(BookmarksExtension.PREF_KEY, encode(bookmarks));

                return preferencesManager.flushPreferences();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onWorkspaceReady(WorkspaceReadyEvent event) {
        initStorage();
    }

    /** {@inheritDoc} */
    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        bookmarks = null;
    }

    protected void fireUpdatedEvent() {
        eventBus.fireEvent(new BookmarksUpdatedEvent());
    }
}

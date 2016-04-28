# Developing the bookmark plugin in Eclipse Che

#### Introduction

In this article we will consider the aspects of developing your own client side plugin for managing bookmarks in Eclipse Che.

Bookmark - is the favorite reference for any object. In our case it will be a reference for file or folder for later retrieval in text editor or project explorer. There are many advantages of this feature. One of them is that if you have a big project with thousands of files you may loose focus on main files you worked on, so you can add files into the bookmarks and then easily access them.

While developing, any IDE may be used, but we will use Eclipse Che.

So, let's do that together.

Before starting to develop the new plugin, you should understand, that architecturally Eclipse Che consists of two parts, client and server side.
Client side - is a GWT application written in java, but then compiled into javascript. In this case you should know, that not all java functions are available. If you are not sure that some functionality correctly emulates in GWT, than you should check official documentation ([JRE Emulation Reference](http://www.gwtproject.org/doc/latest/RefJreEmulation.html))

#### Plugin Structure

To find out how plugins are arranged in the Eclipse Che you can visit official repository ([plugins](https://github.com/eclipse/che/tree/master/plugins)) and see how they were made. There are a few predefined plugins.

But we will start from the scratch. For this we will create an empty maven project and define parent and dependency management in the `pom.xml`

```
   <parent>
       <artifactId>maven-depmgt-pom</artifactId>
       <groupId>org.eclipse.che.depmgt</groupId>
       <version>4.2.0-RC1-SNAPSHOT</version>
   </parent>
```

and

```
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>org.eclipse.che</groupId>
               <artifactId>che-parent</artifactId>
               <version>${che.version}</version>
               <type>pom</type>
               <scope>import</scope>
           </dependency>
       </dependencies>
   </dependencyManagement>
```

Pay attention that we used `4.2.0-RC1-SNAPSHOT` version, so this plugin will be available in this version. This is due the upgrades in the client side api which works with new resource management mechanism (operating with files/folders/projects).

As the Eclipse Che uses GWT library version of 2.7 the client side can be written uses only java 7. Otherwise the code will not compile.

Here you can see the `pom.xml` content:

```
<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2012-2016 Codenvy, S.A.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
      Codenvy, S.A. - initial API and implementation

-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <artifactId>maven-depmgt-pom</artifactId>
        <groupId>org.eclipse.che.depmgt</groupId>
        <version>4.2.0-RC1-SNAPSHOT</version>
    </parent>
    <groupId>org.eclipse.che</groupId>
    <artifactId>che-bookmark-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>Che Plugin :: Bookmarks :: Parent</name>
    <developers>
        <developer>
            <name>Vlad Zhukovskyi</name>
            <email>vzhukovskyi@codenvy.com</email>
            <url>http://codenvy.com/</url>
            <roles>
                <role>developer</role>
            </roles>
            <timezone>Europe/Kiev</timezone>
        </developer>
    </developers>
    <scm>
        <connection>scm:git:http://github.com/vzhukovskii/che-bookmark-plugin</connection>
        <developerConnection>scm:git:https://github.com/vzhukovskii/che-bookmark-plugin</developerConnection>
        <tag>HEAD</tag>
        <url>http://github.com/vzhukovskii/che-bookmark-plugin</url>
    </scm>
    <properties>
        <che.version>4.2.0-RC1-SNAPSHOT</che.version>
        <maven.compiler.source>1.7</maven.compiler.source>
        <maven.compiler.target>1.7</maven.compiler.target>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.eclipse.che</groupId>
                <artifactId>che-parent</artifactId>
                <version>${che.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.gwt</groupId>
            <artifactId>gwt-user</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.gwt.inject</groupId>
            <artifactId>gin</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.inject</groupId>
            <artifactId>guice</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.inject.extensions</groupId>
            <artifactId>guice-assistedinject</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-client-gwt-machine</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-commons-gwt</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-ide-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-ide-app</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.che.core</groupId>
            <artifactId>che-core-ide-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>org.vectomatic</groupId>
            <artifactId>lib-gwt-svg</artifactId>
        </dependency>
    </dependencies>
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
            </resource>
        </resources>
    </build>
</project>
```

In `src/main/java` we will create two base classes:

`org.eclipse.che.ide.bookmarks.BookmarksExtension`:
```
@Singleton
@Extension(title = "Bookmarks", version = "1.0")
public class BookmarksExtension {
    //...
}
```

this class is responsible for the registering extension in the runtime. _Note, that each extension in the Eclipse Che should be marked with [Extension](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/extension/Extension.java) annotation._

And `org.eclipse.che.ide.bookmarks.BookmarksGinModule`:
```
@ExtensionGinModule
public class BookmarksGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        //...
    }
}
```

This class is responsible for the registering base components in dependency management framework (Gin). _Note, that each class which registers components to use in dependency management should be annotated with [ExtensionGinModule](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/extension/ExtensionGinModule.java) annotation._

Than, in `src/main/resources` we will create the following file:

`org/eclipse/che/ide/bookmarks/Bookmarks.gwt.xml`
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Google Inc.//DTD Google Web Toolkit 2.7.0//EN" "http://gwtproject.org/doctype/2.7.0/gwt-module.dtd">
<module>
    <inherits name="com.google.gwt.user.User"/>
    <inherits name="com.google.gwt.http.HTTP"/>
    <inherits name="com.google.gwt.i18n.I18N"/>
    <inherits name="com.google.gwt.json.JSON"/>
    <inherits name='org.eclipse.che.ide.Api'/>
    <inherits name="com.google.gwt.inject.Inject"/>
    <inherits name="org.eclipse.che.ide.Core"/>
    <inherits name="org.eclipse.che.ide.ui.CodenvyUI"/>

    <source path=""/>
</module>
```

Current file is included into GWT compilation to allow link client side code with core application.

Than we should clone the sources of Eclipse Che ([link](https://github.com/eclipse/che)) and register our plugin into compilation phase by adding maven dependency in `/che/assembly/assembly-ide-war/pom.xml` and registering our `Bookmarks.gwt.xml` in GWT compilation by adding:
```
...
<inherits name='org.eclipse.che.ide.bookmarks.Bookmarks'/>
...
```

in `/che/assembly/assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml`.

This is the minimum needed to create an empty plugin in Eclipse Che.

#### Developing the plugin

Lets create a few additional classes which will manage bookmarks.

![asd](https://files.slack.com/files-pri/T02G3VAG4-F14628Y2U/project_structure.png?pub_secret=6ffb8c9399)

Here are the descriptions of each subpackage:

* `org.eclipse.che.ide.bookmarks.actions` contains classes which display actions on the UI. _(Toggle Bookmark and Show Bookmarks)_
* `org.eclipse.che.ide.bookmarks.event` contains class of event that fires when bookmarks has been updated.
* `org.eclipse.che.ide.bookmarks.manager` contains classes of presenter that displays the list of stored bookmarks. As the Eclipse Che's client built using MVP pattern there is the presenter, the view and the implementation of view.
* `org.eclipse.che.ide.bookmarks.resource` contains the resource marker and interceptor. Marker is the special object entity that provides additional information to the specific resource (file, folder or project). Interceptor is the some kind of filter which modifies the resource on the loading stage.
* `org.eclipse.che.ide.bookmarks.storage` contains the implementation of manager that operates with bookmarks.
* `org.eclipse.che.ide.bookmarks.tree` contains the nodes which displays in bookmarks list.

#### Detail view

##### Actions

Each plugin can customize Eclipse Che by adding new items to the menu and toolbars. The Eclipse Che provides the class [Action](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/action/Action.java). Extending this class you can override method `actionPerformed` and perform any actions when user activates action from the UI.
To create custom actions you should perform two steps:

1. Define an action in plugin.
2. Register the action.

Example of action:

```
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
```

By overriding `updateInPerspective` method, action can supply specific rules when it should be showed.
By overriding `actionPerformed` method, action performs developer instructions. In this case we are showing the bookmark panel on the UI.

To register action class [ActionManager](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/action/ActionManager.java) should be used.

Example of registering actions and placing them into the system menu. In `org.eclipse.che.ide.bookmarks.BookmarksExtension` we will add the following code:
```
@Singleton
public class BookmarksExtension {
    @Inject
    public BookmarksExtension(ActionManager actionManager,
                              ToggleBookmarkAction toggleBookmarkAction,
                              ShowBookmarksAction showBookmarksAction,
                              KeyBindingAgent keyBindingAgent) {

        //register actions
        actionManager.registerAction(ToggleBookmarkAction.ID, toggleBookmarkAction);
        actionManager.registerAction(ShowBookmarksAction.ID, showBookmarksAction);

        //registering Bookmarks popup group
        DefaultActionGroup bookmarksGroup = new DefaultActionGroup("Bookmarks", true, actionManager);
        bookmarksGroup.add(toggleBookmarkAction);
        bookmarksGroup.add(showBookmarksAction);

        DefaultActionGroup editGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDIT);
        editGroup.add(bookmarksGroup, Constraints.LAST);

        //creating keybindings for the above actions
        keyBindingAgent.getGlobal().addKey(new KeyBuilder().action().charCode(KeyCodeMap.F11).build(), ToggleBookmarkAction.ID);
        keyBindingAgent.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F11).build(), ShowBookmarksAction.ID);
    }
}
```

As the result you will see your registered actions on the UI:

![Actions](https://files.slack.com/files-pri/T02G3VAG4-F14E9LSJ1/actions.png?pub_secret=741c0f8408)

##### Events

Event can send the signal that specific component has changed own state or some action has performed. To create own event class [GwtEvent](https://github.com/gwtproject/gwt/blob/2.7.0/user/src/com/google/gwt/event/shared/GwtEvent.java) should be extended, simultaneously, with the event there is should be an event handler. Handlers should extend [EventHandler](https://github.com/gwtproject/gwt/blob/2.7.0/user/src/com/google/gwt/event/shared/EventHandler.java).

Example of GWT event base on `org.eclipse.che.ide.bookmarks.event.BookmarksUpdatedEvent`:
```
public class BookmarksUpdatedEvent extends GwtEvent<BookmarksUpdatedEvent.BookmarksUpdatedHandler> {

    /**
     * A listener is notified of changes in bookmarks storage. These changes arise from manipulation of bookmarks storage.
     */
    public interface BookmarksUpdatedHandler extends EventHandler {

        /**
         * Notifies the listener that bookmarks storage has been updated.
         *
         * @param event
         *         instance of {@link BookmarksUpdatedEvent}
         */
        void onBookmarksUpdated(BookmarksUpdatedEvent event);
    }

    private static Type<BookmarksUpdatedHandler> TYPE;

    public static Type<BookmarksUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<BookmarksUpdatedHandler>();
        }
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    public Type<BookmarksUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(BookmarksUpdatedHandler handler) {
        handler.onBookmarksUpdated(this);
    }
}

```

To handle specific events method [EventBus#addHandler](https://github.com/gwtproject/gwt/blob/2.7.0/user/src/com/google/web/bindery/event/shared/EventBus.java#L66) should be called. _Note, that EventBus should be injected to be able to subscribe listening to the events._

Example of handling event:
```
public class Foo implements BookmarksUpdatedHandler {
    @Inject
    public Foo(EventBus eventBus) {
        eventBus.addHandler(BookmarksUpdatedEvent.getType(), this);
    }
    
    @Override
    public void onBookmarksUpdated(BookmarksUpdatedEvent event) {
        //do something on event
    }
}
```

#### Storage

As we need some kind of storage, we will create an interface with necessary methods and it's implementation. This storage will be injected into other components which want to operate with the bookmarks.

Example of `org.eclipse.che.ide.bookmarks.storage.BookmarksStorage`:
```
public interface BookmarksStorage {

    /**
     * Adds the new path to the storage.
     *
     * @param path
     *         the path to mark as bookmark
     * @return true if the path has added, otherwise false
     * @see Path
     */
    boolean add(Path path);

    /**
     * Removes the given path from the storage.
     *
     * @param path
     *         the path to remove from the bookmarks
     * @return true if path has removed, otherwise false
     * @see Path
     */
    boolean remove(Path path);

    /**
     * Returns all stored paths in the storage.
     *
     * @return the path array
     * @see Path
     */
    Path[] getAll();

    /**
     * Checks whether given path is stored in the bookmarks storage.
     *
     * @param path
     *         the path to check
     * @return true if path was stored in the bookmarks storage
     */
    boolean contains(Path path);
}
```
See the javadoc of [Path](https://github.com/eclipse/che/blob/master/core/commons/che-core-commons-gwt/src/main/java/org/eclipse/che/ide/resource/Path.java).

Example of implementation above interface, `org.eclipse.che.ide.bookmarks.storage.BookmarksStorageImpl`:
```
@Singleton
public class BookmarksStorageImpl implements BookmarksStorage,
                                             ResourceChangedHandler,
                                             MarkerChangedHandler,
                                             WorkspaceReadyHandler,
                                             WsAgentStateHandler {

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
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);

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
    public void onWsAgentStarted(WsAgentStateEvent event) {
        //do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        bookmarks = null;
    }

    protected void fireUpdatedEvent() {
        eventBus.fireEvent(new BookmarksUpdatedEvent());
    }
}
```
When path adds or removes from the storage, implementation sends a request to the [PreferencesManager](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/preferences/PreferencesManager.java) asynchronously to save bookmarks and fires event `org.eclipse.che.ide.bookmarks.event.BookmarksUpdatedEvent`.

Finally, we will mark `org.eclipse.che.ide.bookmarks.storage.BookmarksStorage` with following annotation:
```
@ImplementedBy(BookmarksStorageImpl.class)
```
because we have the only one implementation.

#### Parts

In Eclipse Che each panel which is docked to specific region _(Tooling, Information, Editor, Navigation)_ calls part. In our case the part will be responsible for displaying bookmarks and will be located in the right side of IDE, in tooling segment.

As Eclipse Che built using MVP architecture, we need to create Presenter and View. Model will be our bookmark storage which was described in above section.

Example of `org.eclipse.che.ide.bookmarks.manager.BookmarksPresenter`:
```
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
    protected void refreshView() {
        final Path[] paths = bmStorage.getAll();
        final NodeSettings settings = settingsProvider.getSettings();

        view.setBookmarks(Collections.<Node>singletonList(nodeFactory.newBookmarkGroupNode(paths, settings)));
    }
}
```

Each part that should be displayed in the Eclipse Che should extends [BasePresenter](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/parts/base/BasePresenter.java).

Example of `org.eclipse.che.ide.bookmarks.manager.BookmarksView`:
```
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
```

Views should extend [View](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/mvp/View.java).
Action delegate classes should extends [BaseActionDelegate](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/parts/base/BaseActionDelegate.java).

Example of `org.eclipse.che.ide.bookmarks.manager.BookmarksViewImpl`:
```
@Singleton
public class BookmarksViewImpl extends BaseView<BookmarksView.ActionDelegate> implements BookmarksView {

    private Tree tree;

    @Inject
    public BookmarksViewImpl(PartStackUIResources resources, BookmarksLocalizationConstant locale) {
        super(resources);

        setTitle(locale.bookmarks());

        tree = new Tree(new NodeStorage(), new NodeLoader(Collections.<NodeInterceptor>emptySet()));
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

```

View implementations should extends [BaseView](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/parts/base/BaseView.java).

The result at this step you will see on below screenshot:

![Empty part](https://files.slack.com/files-pri/T02G3VAG4-F14EKEXUJ/part.png?pub_secret=3d446c9af7)

#### Nodes

To display the results of stored bookmarks there are two types of nodes used. Group node and Bookmark node.

Example of `org.eclipse.che.ide.bookmarks.tree.BookmarkGroupNode`:
```
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
```

Example of `org.eclipse.che.ide.bookmarks.tree.BookmarkNode`:
```
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
```

Finally we will modify the `org.eclipse.che.ide.bookmarks.BookmarksGinModule` by binding components:
```
@Override
protected void configure() {
    install(new GinFactoryModuleBuilder().build(NodeFactory.class));
    GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(BookmarksInterceptor.class);
}
```

Rebuild Eclipse Che with the plugin and see the result:

![Result](https://files.slack.com/files-pri/T02G3VAG4-F14E4V5EY/result.png?pub_secret=0c05365b23)

### Conclusion 

So, you can see that creating custom plugins to extend Eclipse Che functionality is not difficult as it looks like. In one hour we created a custom plugin that manages bookmarks for the files and folders to help the user fast retrieve them in huge project.

Whole code of the given plugin you can find [there](https://github.com/vzhukovskii/che-bookmark-plugin).

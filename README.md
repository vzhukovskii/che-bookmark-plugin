# Developing the bookmark plugin in Eclipse Che

#### Introduction

In this article we will consider the aspects of developing an own client side plugin for managing bookmarks in Eclipse Che.

Bookmark - is the favorite reference for any object. In our case it will be a reference for file or folder for later retrieval in text editor or project explorer. There are much advantages of this feature. One of them is that if you have a big project with thousands of files you may loose focus on main files you worked on, so you can add files into the bookmarks and then easily find them.

While developing, any IDE may be used, but we will use Eclipse Che.

So, lets do it.

Before starting to develop the new plugin, you should understand, that architecturally Eclipse Che consists of two parts, client and server side.
Client side - is a GWT application written in java, but then compiled into javascript. In this case you should know note, that not all java functions are available. If you are not sure that some functionality correctly emulates in GWT, than you should check official documentation ([JRE Emulation Reference](http://www.gwtproject.org/doc/latest/RefJreEmulation.html))

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

`org.eclipse.che.ide.bookmarks.BookmarksExtension`
```
@Singleton
@Extension(title = "Bookmarks", version = "1.0")
public class BookmarksExtension {
    //...
}
```

this class is responsible for the registering extension in the runtime. _Note, that each extension in the Eclipse Che marks with `@Extension` annotation._

and

`org.eclipse.che.ide.bookmarks.BookmarksGinModule`
```
@ExtensionGinModule
public class BookmarksGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        //...
    }
}
```

this class is responsible for the registering base components in dependency management framework (Gin). _Note, that each class which is registers components to use in dependency management should be annotated with `@ExtensionGinModule` annotation._

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

current file is includes into GWT compilation to allow link client side code with core application.

Than we should clone the sources of Eclipse Che ([link](https://github.com/eclipse/che)) and register our plugin into compilation phase by adding maven dependency in `/che/assembly/assembly-ide-war/pom.xml` and registering our `Bookmarks.gwt.xml` in GWT compilation by adding:
```
...
<inherits name='org.eclipse.che.ide.bookmarks.Bookmarks'/>
...
```

in `/che/assembly/assembly-ide-war/src/main/resources/org/eclipse/che/ide/IDE.gwt.xml`.

This is the minimum needed to create an empty plugin in Eclipse Che.

#### Developing the plugin

Lets create the few additional classes which will manage bookmarks.

![asd](https://files.slack.com/files-pri/T02G3VAG4-F14628Y2U/project_structure.png?pub_secret=6ffb8c9399)

Here are the descriptions of each subpackage:

* `org.eclipse.che.ide.bookmarks.actions` contains classes which display actions on the UI. _(Toggle Bookmark and Show Bookmarks)_
* `org.eclipse.che.ide.bookmarks.event` contains class of event that fires when bookmarks has been updated.
* `org.eclipse.che.ide.bookmarks.manager` contains classes of presenter that displays the list of stored bookmarks. As the Eclipse Che's client built using MVP pattern there is the presenter, the view and the implementation of view.
* `org.eclipse.che.ide.bookmarks.resource` contains the resource marker and interceptor. Marker is the special object entity that provides additional information to the specific resource (file, folder or project). Interceptor is the some kind of filter which modifies the resource on the loading stage.
* `org.eclipse.che.ide.bookmarks.storage` contains the implementation of manager that operates with bookmarks.
* `org.eclipse.che.ide.bookmarks.tree` contains the nodes which displays in bookmarks list.

The full code is available in the [repository](https://github.com/vzhukovskii/che-bookmark-plugin).

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

By overriding `updateInPerspective` action can supply specific rules when it should be showed.
By overriding `actionPerformed` action performs developer instructions. In this case we are showing the bookmark panel on the UI.

To register action class [ActionManager](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/action/ActionManager.java) should be used.

Example of registering actions and placing them in the system menus. In `org.eclipse.che.ide.bookmarks.BookmarksExtension` we will add the following code:
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

##### Events

Event can send the signal that specific component has changed own state or some action has performed. To create own event class [GwtEvent](https://github.com/gwtproject/gwt/blob/2.7.0/user/src/com/google/gwt/event/shared/GwtEvent.java) should be extended, simultaneously, with event there is should be a event handler. Handler should extends [EventHandler](https://github.com/gwtproject/gwt/blob/2.7.0/user/src/com/google/gwt/event/shared/EventHandler.java).

Example of GWT event base on `BookmarksUpdatedEvent`:
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




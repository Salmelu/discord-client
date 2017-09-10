# Discord Modules

Discord Modules is a library for Discord bots, written in Java. It's purpose is to make writing small bots for your personal Discord servers easier.

## How does it work?

The library's core term is a **module**. A module is a single isolated unit (usually a class), which is written by you and contains your code. The compiled module (its *.class* file) is then loaded into the framework and executed when its conditions fulfill.

### Compiling from sources

The default compilation is simple. All you need to do is executing `mvn package`. This compiles all the sources, packages the library *.jar*, gets all the dependencies and configuration files and puts everything in `dist/` directory.

### How to make a simple module

```Java
import cz.salmelu.discord.listeners.MessageListener;

public class MyModule implements MessageListener {
  @Override
  public boolean matchMessage(Message message) {
    return message.getText().startsWith("/");
  }

  @Override
  public void onMessage(Message message) {
    message.reply("Message was accepted.");
  }

  @Override
  public String getName() {
    return "acceptor"
  }
}
```
The class above is a simple example of a module. After writing such a module, compile it into its class file and put it into the *modules* directory of your library (don't forget that if you put it in a package, you need to conserve directory structure). The last step is adding your module name to *config/modules* text file.

Then if you run the library, it will automatically load your module and run its code when applied. That means, on every message starting with a slash (**/**), it replies *"Message was accepted."*.

### Configure the library

Configuration files are located in the *config/* directory. You can find three configuration files there:

+ *discord.properties* - main library configuration; The most important field is **token**, which must be set to your application Discord token for the library to work. See [Discord developers documentation](https://discordapp.com/developers/applications/me) to get a token for your bot.
+ *log4j.properties* - settings for logging.
+ *modulelist* - contains a list of loaded modules. Add your module names here so the library loads them.

### Starting it up

Run the main jar for its directory: `java -jar DiscordModules-version.jar`.

## Library functions

### Listeners

Every module should implement at least one of the supplies listeners. When the event in the listener is triggered, the library will call your overloaded method. Most of the methods in those interfaces are set as default, therefore you can only implement those that matter and leave rest unimplemented.

The listeners are contained in `cz.salmelu.discord.listeners` package. There are four available listeners:

+ `Initializer` - its methods are triggered when the library successfully connects to Discord and when any Discord community server becomes available or unavailable.
+ `MessageListener` - contains methods to react to all message related events, such as someone posting a message, editing it, or adding a reaction.
+ `ServerListener` - contains methods to react to community server changes, such as editing channels, roles, or adding new members.
+ `UserActionListener` - contains methods to react to user changes, such as their private channels, typing, or presence changes.

Feel free to implement any of those interfaces in your modules and any of their methods to achieve your desired behavior.

### Rate limits

Discord official API implements rate limits. Those are application-based limits on certain resources, such as channels, or community servers. The Application can only use the API a limited amount of times, to prevent abuse. These limits are set by Discord developers and are completely dynamic, so they can change anytime.

To prevent going over limits, this library implements a client-side rate limiter. This class attempts to watch the replies received from Discord and parse the rate limit information from them. If it detects that the next API call would be breach the limit, it will automatically delay the call.

Since the limits may change anytime, this may not work everytime. In the case when the call is not successful because of rate limits, the call throws `DiscordRequestException`.

### Asynchronous calls

A significant part of the API methods send a one-way request to Discord servers. In some cases, your module may not need to wait for the response, or it may need to process some data before the response arrives.

For those cases, all such requests are done asynchronously and the method call returns a future. If you do not care about the result, you can simply ignore the returned value. However, in case you need to make sure the request was successful, call the `get()` method on received future to await the completion of the request.

### Context and other features

The library offers many additional features not directly connected with Discord API. To get access to them, your module must have a *Context constructor*.

All following classes are found in `cz.salmelu.discord` package. See the classes documentation for more details.

#### Context constructor

Every module is initialized using its default public constructor (the constructor taking no arguments, such as `public MyModule() {}`). However, when you need to access the *Context*, implement another constructor, which takes a context as its only argument:

```Java
public MyModule(Context c) {
  // store the context instance for later use
  this.context = c;
}
```

If such constructor is present, the library will call it instead of the default constructor.

#### NotifyManager

NotifyManager is a special class, which can be used to call your functions at a certain time. It allows you to dynamically set up channel broadcasts, timed clean actions and many more. For full documentation, see the Javadoc of its class, `cz.salmelu.discord.NotifyManager`.

A quick simple example of its usage:

```Java
context.getNotifyManager()
    .addNotification(new String("The event starts now!"),
        object -> myChannel.sendMessage(object);,
        OffsetDateTime.now().plusHours(1));
```

This sends a message to a channel (the one stored in `myChannel`) an exactly one hour later after this code is executed.

#### Storage

The framework also allows you to access a persistent storage. A storage is a simple map of key value pairs, where a value is any `Serializable` object. The map is automatically saved to disk so it persists client restarts.

Every module can have multiple storages, and they are accessed with `context.getStorage(storageName)` method.

#### SubscriptionManager

Let's say you organise an event and want to occasionally send a message to a bunch of members who subscribed for the event.

This is where `SubscriptionManager` will help you. You can use it to subscribe and unsubscribe members to your module by calling its methods. When the event message is being posted, method `getMentions()` will return a string to attach to the message, which will mention all subscribed members.

#### PermissionGuard

Some modules should not be accessible to everyone, yet the calls should be seen by everyone so the members know what the admins are doing.

This is where `PermissionGuard` comes. It allows you to give your commands a minimum level of required permissions, and then assign your members another level of permissions. Then a simple call `isAllowed()` checks, if the user has permissions to use the command or not.

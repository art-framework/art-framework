# Developer Documentation

The [ART-Framework](https://github.com/silthus/art-framework) is designed to be modular and very easy to use for both developers and [server admins](../admin/README.md).

All of these code examples can also be found inside the [art-example](../../art-example/src/main/java/net/silthus/examples/art/) project.

* [Dependencies](#dependencies)
  * [Gradle](#gradle)
  * [Maven](#maven)
* [Creating Actions](#creating-actions)
* [Register your **A**ctions **R**equirements **T**rigger](#register-your-actions-requirements-trigger)
  * [Using the ART static class](#using-the-art-static-class)
  * [Using the Bukkit ServiceManager](#using-the-bukkit-servicemanager)
* [Using Actions](#using-actions)
  * [Using ConfigLib to load the config](#using-configlib-to-load-the-config)

## Dependencies

You only need to depend on the `net.silthus.art:art-core` maven packge and shade it into your plugin or require the admin to provide the corresponding plugin to be present.

### Gradle

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation group: 'net.silthus.art', name: 'art-core', version: '1.0.0-beta.1'
}
```

Shade ART into your plugin if you don't want to depend on the ART-Plugin being present on the server.

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version '5.2.0'
}

...

shadowJar {
    classifier = ''
    dependencies {
        include(dependency('net.silthus.art:art-core:'))
    }
    relocate 'net.silthus.art', "${YOUR_PACKAGE_NAME}.art"
}
```

### Maven

```xml
<project>
  ...
  <dependencies>
    <dependency>
      <groupId>net.silthus.art</groupId>
      <artifactId>art-core</artifactId>
      <version>1.0.0-beta.1</version>
    </dependency>
  </dependencies>
  ...
</project>
```

Shade ART into your plugin if you don't want to depend on the ART-Plugin being present on the server.

```xml
<project>
  ...
  <plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.0.0</version>
        <executions>
            <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <relocations>
                <relocation>
                    <pattern>net.silthus.art</pattern>
                    <shadedPattern>your.package.name.art</shadedPattern>
                </relocation>
                </relocations>
            </configuration>
            </execution>
        </executions>
    </plugin>
  </plugins>
  ...
</project>
```

## Creating Actions

You can provide `actions`, `requirements` and `trigger` from any of your plugins. These will be useable by [Server Admins](../admin/README.md) inside configs used by other plugins.

Providing an `Action` is as simple as implementing the `Action<TTarget, TConfig>` interface and registering it `onLoad()` with `ART.register(...)`.

First create your action and define a config (optional). In this example a `PlayerDamageAction` with its own config class.

```java
/**
 * Every action needs a unique name across all plugins.
 * It is recommended to prefix it with your plugin name to make sure it is unique.
 *
 * The @Name annotation is required on all actions or else the registration will fail.
 *
 * You can optionally provide a @Config that will be used to describe the parameter your action takes.
 */
@Name("art-example:player.damage")
@Config(PlayerDamageAction.ActionConfig.class)
public class PlayerDamageAction implements Action<Player, PlayerDamageAction.ActionConfig> {

    /**
     * This method will be called everytime your action is executed.
     *
     * @param player the player or other target object your action is executed against
     * @param context context of this action.
     *                Use the {@link ActionContext} to retrieve the config
     */
    @Override
    public void execute(Player player, ActionContext<Player, ActionConfig> context) {
        context.getConfig().ifPresent(config -> {
            double damage;
            double health = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            if (config.percentage) {
                if (config.fromCurrent) {
                    damage = health * config.amount;
                } else {
                    damage = maxHealth * config.amount;
                }
            } else {
                damage = config.amount;
            }

            player.damage(damage);
        });
    }

    /**
     * You should annotate all of your config parameters with a @Description.
     * This will make it easier for the admins to decide what to configure.
     *
     * You can also tag config fields with a @Required flag.
     * The action caller will get an error if the parameter is not defined inside the config.
     *
     * Additionally to that you have to option to mark your parameters with the @Position position.
     * Start in an indexed manner at 0 and count upwards. This is optional.
     *
     * This means your action can be called like this: !art-example:player.damage 10
     * instead of: !art-example:player.damage amount=10
     */
    public static class ActionConfig {

        @Required
        @Position(0)
        @Description("Damage amount in percent or health points. Use a value between 0 and 1 if percentage=true.")
        private double amount;

        @Description("Set to true if you want the player to be damaged based on his maximum life")
        private boolean percentage = false;

        @Description("Set to true if you want to damage the player based on his current health. Only makes sense in combination with percentage=true.")
        private boolean fromCurrent = false;
    }
}
```

## Register your **A**ctions **R**equirements **T**rigger

You need to register your actions, requirements and trigger when your plugin is enabled. Before you can do that, you need to make sure ART is loaded and enabled.

### Using the ART static class

You can use the static `ART` class to register your actions, requirements and trigger. However you need to make sure ART is loaded before calling it, to avoid `ClassNotFoundExceptions`.

```java
public class ExampleARTPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        // register your actions, requirements and trigger when enabling your plugin
        registerART();
    }

    private void registerART() {

        if (!isARTLoaded()) {
            getLogger().warning("ART plugin not found. Not registering ART.");
            return;
        }

        ART.register(getName(), artBuilder -> {
            artBuilder.target(Player.class)
                    .action(new PlayerDamageAction());
        });
    }

    private boolean isARTLoaded() {
        return Bukkit.getPluginManager().getPlugin("ART") != null;
    }
}
```

### Using the Bukkit ServiceManager

As an alternative you can use the Bukkit `ServiceManager` to get an instance of the `ARTManager` and register your actions, requirements and trigger with it.

```java
public class ExampleARTPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        // register your actions, requirements and trigger when enabling your plugin
        // this needs to be done before loading configs
        registerART();
    }

    private void registerART() {

        if (!isARTLoaded() || getARTManager().isEmpty()) {
            getLogger().warning("ART plugin not found. Not registering ART.");
            return;
        }

        getARTManager().get().register(getName(), artBuilder -> {
            artBuilder.target(Player.class)
                    .action(new PlayerDamageAction());
        });
    }

    private boolean isARTLoaded() {
        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("ART");
        if (plugin == null) {
            return false;
        }

        RegisteredServiceProvider<ARTManager> registration = Bukkit.getServicesManager().getRegistration(ARTManager.class);
        if (registration == null) {
            return false;
        }

        return true;
    }

    private Optional<ARTManager> getARTManager() {

        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("ART");
        if (plugin == null) {
            return Optional.empty();
        }

        RegisteredServiceProvider<ARTManager> registration = Bukkit.getServicesManager().getRegistration(ARTManager.class);
        if (registration == null) {
            return Optional.empty();
        }

        return Optional.of(registration.getProvider());
    }
}
```

## Using Actions

One powerfull feature ob the [ART-Framework](https://github.com/silthus/art-framework) is the reuseability of actions, requirements and trigger accross multiple plugins without knowing the implementation and config of those.

All you need to do to use actions inside your plugin is to provide a reference to the loaded `ARTConfig`. How you load this config is up to you. However to make your life simple ART provides some helper methods for Bukkits `ConfigurationSection` (*coming soon*) and implements [ConfigLib](https://github.com/Silthus/ConfigLib) for some easy config loading.

> Make sure you load your ARTConfig after all plugins are loaded and enabled.  
> To do this you can use this handy method: `Bukkit.getScheduler().runTaskLater(this, () -> {...}, 1L);`  
> This will execute after all plugins are loaded and enabled.

The following example references an `example.yml` config which could have this content. For more details see the [admin documentation](../admin/README.md).

```yaml
actions:
  art:
    - '!art-example:player.damage 10'
```

### Using [ConfigLib](https://github.com/Silthus/ConfigLib) to load the config

```java
public class ExampleARTPlugin extends JavaPlugin implements Listener {

    @Getter
    private final List<Action<Player, ?>> actions = new ArrayList<>();

    @Override
    public void onEnable() {

        // this will load all art configs after all plugins are loaded and enabled
        // this is a must to avoid loading conflicts
        Bukkit.getScheduler().runTaskLater(this, this::loadARTConfig, 1L);

        // register your standard event stuff
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // this will execute all configured actions on every player move
    // dont try this at home ;)
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        this.actions.forEach(action -> action.execute(event.getPlayer()));
    }

    private void loadARTConfig() {

        if (!isARTLoaded()) {
            getLogger().warning("ART plugin not found. Not loading ART configs.");
            return;
        }

        // this will load the config using ConfigLib
        // see https://github.com/Silthus/ConfigLib/ for more details
        Config config = new Config(new File(getDataFolder(), "example.yml"));
        config.loadAndSave();

        List<ActionContext<Player, ?>> actions = ART.actions().create(Player.class, config.getActions());

        this.actions.addAll(actions);
    }

    private boolean isARTLoaded() {
        return Bukkit.getPluginManager().getPlugin("ART") != null;
    }

    @Getter
    @Setter
    public static class Config extends YamlConfiguration {

        private ARTConfig actions = new ARTConfig();

        protected Config(File file) {
            super(file.toPath());
        }
    }
```
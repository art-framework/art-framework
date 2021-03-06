This installation guide is for Minecraft server owners that want to use the `art-framework`. Developers can look at the [quickstart guide](/developer/) or the [maven/gradle](dev-setup.md) page.

To use the art-framework on your server, you have to do three things:

1. [Download the latest release](https://github.com/art-framework/art-core/releases/latest) and install the ART plugin for your Minecraft distribution.  
   *e.g.: `art-bukkit` if your are running `spigot`, `paper` or any other bukkit based distribution.*
2. [Learn the syntax](syntax.md) for configuring [Actions](actions.md), [Requirements](requirements.md) and [Trigger](trigger.md).
3. Install a plugin that supports loading ART configs and follow the instructions of the plugin where you should put the [ART section](#art-config-syntax).

!> If you want to persist cooldowns of actions and triggers across server reboots you need to install the [ebean-wrapper](https://github.com/silthus/ebean-wrapper) plugin as well.

## Installing Modules

To install a module simply drop it into the `plugins/art-framework/modules/` directory and restart your server.

*A command to hot reload modules will be available soon. ([#38](https://github.com/art-framework/art-framework/issues/38))*

package com.github.hornta.versus;

import com.github.hornta.commando.CarbonArgument;
import com.github.hornta.commando.CarbonCommand;
import com.github.hornta.commando.Commando;
import com.github.hornta.commando.ICarbonArgument;
import com.github.hornta.commando.ValidationResult;
import com.github.hornta.commando.completers.IArgumentHandler;
import com.github.hornta.messenger.MessageManager;
import com.github.hornta.messenger.MessagesBuilder;
import com.github.hornta.messenger.MessengerException;
import com.github.hornta.messenger.Translation;
import com.github.hornta.messenger.Translations;
import com.github.hornta.versioned_config.Configuration;
import com.github.hornta.versioned_config.ConfigurationBuilder;
import com.github.hornta.versioned_config.ConfigurationException;
import com.github.hornta.versioned_config.Migration;
import com.github.hornta.versioned_config.Patch;
import com.github.hornta.versioned_config.Type;
import com.github.hornta.versus.commands.AddPlayerCommand;
import com.github.hornta.versus.commands.CommandReload;
import com.github.hornta.versus.commands.RemovePlayerCommand;
import com.github.hornta.versus.versus.Versus;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VersusPlugin extends JavaPlugin {
  private static VersusPlugin instance;
  private Commando commando;
  private Translations translations;
  private Configuration<ConfigKey> configuration;
  private Versus versus;

  @Override
  public void onEnable() {
    instance = this;
    new Metrics(this, 7465);

    try {
      setupConfig();
    } catch (ConfigurationException e) {
      getLogger().log(Level.SEVERE, "Failed to setup configuration", e);
      setEnabled(false);
      return;
    }

    try {
      setupMessages();
    } catch (MessengerException e) {
      getLogger().log(Level.SEVERE, "Failed to setup messages", e);
      setEnabled(false);
      return;
    }

    versus = new Versus();
    Bukkit.getPluginManager().registerEvents(versus, this);

    setupCommands();
  }

  @Override
  public void onDisable() {
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      Disguise disguise = DisguiseAPI.getDisguise(onlinePlayer);
      if (disguise != null) {
        disguise.stopDisguise();
      }
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return commando.handleCommand(sender, command, args);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return commando.handleAutoComplete(sender, command, args);
  }

  private void setupConfig() throws ConfigurationException {
    File cfgFile = new File(getDataFolder(), "config.yml");
    ConfigurationBuilder<ConfigKey> cb = new ConfigurationBuilder<>(cfgFile);
    cb.addMigration(new Migration<>(1, () -> {
      Patch<ConfigKey> patch = new Patch<>();
      patch.set(ConfigKey.LANGUAGE, "language", "english", Type.STRING);
      return patch;
    }));
    configuration = cb.create();
  }

  private void setupMessages() throws MessengerException {
    MessageManager messageManager = new MessagesBuilder()
      .add(MessageKey.NO_PERMISSION_COMMAND, "no_permission_command")
      .add(MessageKey.MISSING_ARGUMENTS_COMMAND, "missing_arguments_command")
      .add(MessageKey.COMMAND_NOT_FOUND, "command_not_found")
      .add(MessageKey.CONTROL_MOBS, "control_mobs")
      .add(MessageKey.CONTROL_INSTRUCTION, "control_instruction")
      .add(MessageKey.NO_CONTROL_MOBS, "no_control_mobs")
      .add(MessageKey.RELOAD_CONFIG_FAILED, "reload_config_failed")
      .add(MessageKey.RELOAD_MESSAGES_FAILED, "reload_messages_failed")
      .add(MessageKey.RELOADED, "reloaded")
      .add(MessageKey.NOT_ONLINE, "not_online")
      .add(MessageKey.ALREADY_CONTROLLING, "already_controlling")
      .add(MessageKey.NOT_CONTROLLING, "not_controlling")
      .add(MessageKey.NOW_CONTROLLING, "now_controlling")
      .add(MessageKey.NOT_DISGUISED, "not_disguised")
      .add(MessageKey.NOT_FIND_MOBS, "not_find_mobs")
      .add(MessageKey.NOT_FIND_MOBS_TYPE, "not_find_mobs_type")
      .add(MessageKey.COOLDOWN, "cooldown")
      .add(MessageKey.TIME_UNIT_SECOND, "timeunit.second")
      .add(MessageKey.TIME_UNIT_SECONDS, "timeunit.seconds")
      .add(MessageKey.TIME_UNIT_MINUTE, "timeunit.minute")
      .add(MessageKey.TIME_UNIT_MINUTES, "timeunit.minutes")
      .add(MessageKey.TIME_UNIT_HOUR, "timeunit.hour")
      .add(MessageKey.TIME_UNIT_HOURS, "timeunit.hours")
      .add(MessageKey.TIME_UNIT_DAY, "timeunit.day")
      .add(MessageKey.TIME_UNIT_DAYS, "timeunit.days")
      .build();

    translations = new Translations(this, messageManager);
    String language = configuration.get(ConfigKey.LANGUAGE);
    Translation translation = translations.createTranslation(language);
    messageManager.setTranslation(translation);
  }

  private void setupCommands() {
    commando = new Commando();
    commando.setNoPermissionHandler((CommandSender commandSender, CarbonCommand command) -> MessageManager.sendMessage(commandSender, MessageKey.NO_PERMISSION_COMMAND));

    commando.setMissingArgumentHandler((CommandSender commandSender, CarbonCommand command) -> {
      MessageManager.setValue("usage", command.getHelpText());
      MessageManager.sendMessage(commandSender, MessageKey.MISSING_ARGUMENTS_COMMAND);
    });

    commando.setMissingCommandHandler((CommandSender sender, List<CarbonCommand> suggestions) -> {
      MessageManager.setValue("suggestions", suggestions.stream()
        .map(CarbonCommand::getHelpText)
        .collect(Collectors.joining("\n")));
      MessageManager.sendMessage(sender, MessageKey.COMMAND_NOT_FOUND);
    });

    ICarbonArgument addPlayerArg = new CarbonArgument.Builder("player")
      .setHandler(new IArgumentHandler() {
        @Override
        public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
          return Bukkit.getOnlinePlayers()
            .stream()
            .filter((Player p) -> !versus.isControlling(p))
            .map(Player::getName)
            .collect(Collectors.toSet());
        }

        @Override
        public boolean test(Set<String> items, String argument) {
          return items.contains(argument);
        }

        @Override
        public void whenInvalid(ValidationResult validationResult) {
          String playerName = validationResult.getValue();
          Player player = Bukkit.getPlayer(playerName);
          MessageManager.setValue("player_name", playerName);
          if(player == null) {
            MessageManager.sendMessage(validationResult.getCommandSender(), MessageKey.NOT_ONLINE);
          } else {
            MessageManager.sendMessage(validationResult.getCommandSender(), MessageKey.ALREADY_CONTROLLING);
          }
        }
      })
      .create();

    commando
      .addCommand("versus add")
      .withHandler(new AddPlayerCommand())
      .withArgument(addPlayerArg)
      .requiresPermission("versus.add");

    ICarbonArgument removePlayerArg = new CarbonArgument.Builder("player")
      .setHandler(new IArgumentHandler() {
        @Override
        public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
          return Bukkit.getOnlinePlayers()
            .stream()
            .filter((Player p) -> versus.isControlling(p))
            .map(Player::getName)
            .collect(Collectors.toSet());
        }

        @Override
        public boolean test(Set<String> items, String argument) {
          return items.contains(argument);
        }

        @Override
        public void whenInvalid(ValidationResult validationResult) {
          String playerName = validationResult.getValue();
          MessageManager.setValue("player_name", playerName);
          MessageManager.sendMessage(validationResult.getCommandSender(), MessageKey.NOT_CONTROLLING);
        }
      })
      .create();

    commando
      .addCommand("versus remove")
      .withHandler(new RemovePlayerCommand())
      .withArgument(removePlayerArg)
      .requiresPermission("versus.remove");

    commando
      .addCommand("versus reload")
      .withHandler(new CommandReload())
      .requiresPermission("versus.reload");
  }

  public static Configuration<ConfigKey> getConfiguration() {
    return instance.configuration;
  }

  public static Translations getTranslations() {
    return instance.translations;
  }

  public static VersusPlugin getInstance() {
    return instance;
  }
}

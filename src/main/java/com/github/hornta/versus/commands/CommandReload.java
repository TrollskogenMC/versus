package com.github.hornta.versus.commands;

import com.github.hornta.commando.ICommandHandler;
import com.github.hornta.messenger.MessageManager;
import com.github.hornta.messenger.MessengerException;
import com.github.hornta.messenger.Translation;
import com.github.hornta.versioned_config.ConfigurationException;
import com.github.hornta.versus.ConfigKey;
import com.github.hornta.versus.MessageKey;
import com.github.hornta.versus.VersusPlugin;
import org.bukkit.command.CommandSender;

public class CommandReload implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] strings, int i) {
    try {
      VersusPlugin.getConfiguration().reload();
    } catch (ConfigurationException e) {
      MessageManager.setValue("reason", e.getMessage());
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_CONFIG_FAILED);
      return;
    }

    Translation translation;
    try {
      translation = VersusPlugin.getTranslations().createTranslation(VersusPlugin.getConfiguration().get(ConfigKey.LANGUAGE));
    } catch (MessengerException e) {
      MessageManager.setValue("reason", e.getMessage());
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_MESSAGES_FAILED);
      return;
    }
    MessageManager.getInstance().setTranslation(translation);
    MessageManager.sendMessage(commandSender, MessageKey.RELOADED);
  }
}

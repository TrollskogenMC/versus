package com.github.hornta.versus.commands;

import com.github.hornta.commando.ICommandHandler;
import com.github.hornta.versus.versus.Versus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemovePlayerCommand implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args, int i) {
    Player player = Bukkit.getPlayer(args[0]);
    Versus.removePlayer(player);
  }
}

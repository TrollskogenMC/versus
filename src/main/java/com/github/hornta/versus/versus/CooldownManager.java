package com.github.hornta.versus.versus;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class CooldownManager {
  private final HashMap<UUID, HashSet<Cooldown>> playerCooldowns;

  CooldownManager() {
    playerCooldowns = new HashMap<>();
  }

  public void addCooldown(Player player, long duration, CooldownType cooldownType) {
    HashSet<Cooldown> cooldowns = playerCooldowns.containsKey(player.getUniqueId()) ? playerCooldowns.get(player.getUniqueId()) : new HashSet<>();
    Cooldown cooldown = new Cooldown(duration, cooldownType);
    cooldowns.add(cooldown);
    playerCooldowns.put(player.getUniqueId(), cooldowns);
  }

  public Cooldown getCooldown(Player player, CooldownType cooldownType) {
    if (!playerCooldowns.containsKey(player.getUniqueId())) {
      return null;
    }
    return playerCooldowns.get(player.getUniqueId()).stream().filter(cooldown -> cooldown.getType() == cooldownType).findFirst().orElse(null);
  }

  public void removeCooldown(Player player, CooldownType cooldownType) {
    if (!playerCooldowns.containsKey(player.getUniqueId())) {
      return;
    }
    playerCooldowns.get(player.getUniqueId()).removeIf(cooldown -> cooldown.getType() == cooldownType);
  }

  public boolean isOnCooldown(Player player, CooldownType type) {
    Cooldown cooldown = getCooldown(player, type);
    if (cooldown == null) {
      return false;
    }

    if(cooldown.isActive()) {
      return true;
    } else {
      removeCooldown(player, type);
    }
    return false;
  }
}


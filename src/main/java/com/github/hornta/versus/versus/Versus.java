package com.github.hornta.versus.versus;

import com.github.hornta.messenger.MessageManager;
import com.github.hornta.versus.MessageKey;
import com.github.hornta.versus.VersusPlugin;
import io.papermc.lib.PaperLib;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EndermanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GhastWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Versus implements Listener {
  private static Versus instance;
  private final Set<UUID> controllers;
  private final CooldownManager cooldownManager;
  private final BlockFace[] blockFaces;
  private final HashMap<UUID, Integer> vineEvents;
  private final Random random;

  public Versus() {
    instance = this;
    controllers = new HashSet<>();
    cooldownManager = new CooldownManager();
    blockFaces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    vineEvents = new HashMap<>();
    random = new Random();

    new BukkitRunnable() {
      public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
          Disguise disguise = DisguiseAPI.getDisguise(player);
          if (disguise != null) {
            if (disguise.getType() == DisguiseType.ENDER_DRAGON) {
              for (Entity nearbyEntity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
                if (nearbyEntity instanceof Player) {
                  nearbyEntity.setVelocity(player.getLocation().getDirection().multiply(5).add(new Vector(0, 4, 0)));
                  ((Player)nearbyEntity).damage(2.0);
                }
              }
            } else if (disguise.getType() == DisguiseType.ENDERMAN) {
              if (player.getLocation().getBlock().getType() != Material.WATER) {
                continue;
              }
              player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
              Location loc = player.getWorld().getHighestBlockAt(player.getLocation().clone().add((random.nextDouble() - 0.5) * 64.0, (double)(random.nextInt(64) - 32), (random.nextDouble() - 0.5) * 64.0)).getLocation();
              PaperLib.teleportAsync(player, loc);
              player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            } else if (disguise.getType() == DisguiseType.COD || disguise.getType() == DisguiseType.DOLPHIN || disguise.getType() == DisguiseType.PUFFERFISH || disguise.getType() == DisguiseType.SALMON || disguise.getType() == DisguiseType.SQUID || disguise.getType() == DisguiseType.TROPICAL_FISH) {
              if (player.getLocation().getBlock().getType() == Material.WATER) {
                continue;
              }
              if(cooldownManager.isOnCooldown(player, CooldownType.DOLPHIN_LEAP)) {
                return;
              }
              player.damage(2.0);
            } else {
              if (
                disguise.getType() != DisguiseType.SKELETON &&
                disguise.getType() != DisguiseType.ZOMBIE &&
                disguise.getType() != DisguiseType.PHANTOM ||
                player.getWorld().getTime() < 23500L &&
                (
                  player.getWorld().getTime() > 12500L ||
                  player.getLocation().getBlock().getType() == Material.WATER
                ) ||
                player.getWorld().getHighestBlockAt(player.getEyeLocation()).getY() > player.getEyeLocation().getY()
              ) {
                continue;
              }
              player.setFireTicks(60);
            }
          }
        }
      }
    }.runTaskTimer(VersusPlugin.getInstance(), 0L, 20L);
  }

  private void sendCooldownMessage(Player player, CooldownType type) {
    Util.setTimeLeftPlaceholder(cooldownManager.getCooldown(player, CooldownType.HIT).getSecondsLeft());
    MessageManager.sendMessage(player, MessageKey.COOLDOWN);
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void arrowNerf(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Arrow && ((Arrow)event.getDamager()).getShooter() instanceof Player) {
      Player player = (Player)((Arrow)event.getDamager()).getShooter();
      Disguise disguise = DisguiseAPI.getDisguise(player);
      if (
        disguise != null &&
        (
          disguise.getType() == DisguiseType.SKELETON ||
          disguise.getType() == DisguiseType.STRAY ||
          disguise.getType() == DisguiseType.PILLAGER
        )
      ) {
        event.setDamage(2.0);
      }
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onEntityDamageEvent(EntityDamageEvent event) {
    Entity entity = event.getEntity();
    if (entity instanceof Player && isControlling((Player)entity)) {
      Disguise disguise = DisguiseAPI.getDisguise(entity);
      if (disguise == null) {
        event.setCancelled(true);
        return;
      }
      if (((Player)entity).getHealth() - event.getFinalDamage() <= 0.0) {
        event.setDamage(0.0);
        entity.setFireTicks(0);
        if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)event).getDamager() instanceof Player) {
          Location location = entity.getLocation();
          new BukkitRunnable() {
            public void run() {
              ExperienceOrb orb = (ExperienceOrb)location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB);
              orb.setExperience(10);
              entity.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, entity.getLocation(), 10, 0.0, 0.0, 0.0, 0.0);
            }
          }.runTaskLater(VersusPlugin.getInstance(), 10L);
        }
        try {
          Sound sound = Sound.valueOf("ENTITY_" + disguise.getType().getEntityType().name() + "_DEATH");
          entity.getWorld().playSound(entity.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {}
        Entity tempEntity = entity.getWorld().spawnEntity(new Location(entity.getWorld(), 0.0, -100.0, 0.0), disguise.getType().getEntityType());
        tempEntity.setFireTicks(entity.getFireTicks());
        if (tempEntity instanceof Lootable) {
          LootTable lootTable = ((Lootable)tempEntity).getLootTable();
          if (lootTable == null) {
            return;
          }
          Player killer = (Player)(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)event).getDamager() instanceof Player ? ((EntityDamageByEntityEvent)event).getDamager() : entity);
          LootContext.Builder contextBuilder = new LootContext.Builder(entity.getLocation()).lootedEntity(tempEntity).killer(killer);
          AttributeInstance attributeLuck = killer.getAttribute(Attribute.GENERIC_LUCK);
          if (attributeLuck != null) {
            contextBuilder.luck((float)attributeLuck.getValue());
          }
          contextBuilder.lootingModifier(killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));
          LootContext context = contextBuilder.build();
          Collection<ItemStack> itemStacks = lootTable.populateLoot(random, context);
          for (ItemStack loot : itemStacks) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), loot);
          }
        }
        tempEntity.remove();
        undisguise((Player)entity, false, false, true);
      }
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
    Entity entity = event.getEntity();
    Entity damager = event.getDamager();
    if (damager instanceof Player && isControlling((Player)damager)) {
      if (entity instanceof Player) {
        Disguise disguise = DisguiseAPI.getDisguise(damager);
        if (disguise != null) {
          Difficulty difficulty = damager.getWorld().getDifficulty();
          switch (disguise.getType()) {
            case CAVE_SPIDER:
            case SPIDER:
            case DROWNED:
            case ENDERMITE:
            case PILLAGER:
            case HUSK:
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case SLIME:
              event.setDamage(difficulty == Difficulty.EASY ? 2.0 : difficulty == Difficulty.NORMAL ? 3.0 : difficulty == Difficulty.HARD ? 4.0 : 2.0);
              break;
            case ENDERMAN:
            case BLAZE:
            case EVOKER:
            case GUARDIAN:
            case MAGMA_CUBE:
            case IRON_GOLEM:
            case PHANTOM:
              event.setDamage(difficulty == Difficulty.EASY ? 4.0 : difficulty == Difficulty.NORMAL ? 6.0 : difficulty == Difficulty.HARD ? 6.0 : 4.0);
              break;
            case PIG_ZOMBIE:
            case ELDER_GUARDIAN:
            case WITHER_SKELETON:
            case VEX:
              event.setDamage(difficulty == Difficulty.EASY ? 5.0 : difficulty == Difficulty.NORMAL ? 8.0 : difficulty == Difficulty.HARD ? 12.0 : 5.0);
              break;
            case ENDER_DRAGON:
              event.setDamage(difficulty == Difficulty.EASY ? 6.0 : difficulty == Difficulty.NORMAL ? 10.0 : difficulty == Difficulty.HARD ? 15.0 : 6.0);
              break;
            case RAVAGER:
            case VINDICATOR:
              event.setDamage(difficulty == Difficulty.EASY ? 7.0 : difficulty == Difficulty.NORMAL ? 12.0 : difficulty == Difficulty.HARD ? 18.0 : 7.0);
              break;
            default:
              event.setDamage(1.0);
              break;
          }
        }
        else {
          event.setCancelled(true);
        }
      }
      else {
        event.setCancelled(true);
      }
    }
    if (damager instanceof Player && isControlling((Player)damager)) {
      Player player = (Player)damager;
      Disguise disguise = DisguiseAPI.getDisguise(player);
      if (
        disguise != null &&
        disguise.getType() != DisguiseType.BLAZE &&
        disguise.getType() != DisguiseType.CAVE_SPIDER &&
        disguise.getType() != DisguiseType.ENDERMAN &&
        disguise.getType() != DisguiseType.ENDERMITE &&
        disguise.getType() != DisguiseType.EVOKER &&
        disguise.getType() != DisguiseType.HUSK &&
        disguise.getType() != DisguiseType.IRON_GOLEM &&
        disguise.getType() != DisguiseType.MAGMA_CUBE &&
        disguise.getType() != DisguiseType.PHANTOM &&
        disguise.getType() != DisguiseType.PIG_ZOMBIE &&
        disguise.getType() != DisguiseType.POLAR_BEAR &&
        disguise.getType() != DisguiseType.RAVAGER &&
        disguise.getType() != DisguiseType.SILVERFISH &&
        disguise.getType() != DisguiseType.SLIME &&
        disguise.getType() != DisguiseType.SPIDER &&
        disguise.getType() != DisguiseType.VEX &&
        disguise.getType() != DisguiseType.VINDICATOR &&
        disguise.getType() != DisguiseType.WITHER_SKELETON &&
        disguise.getType() != DisguiseType.WITHER &&
        disguise.getType() != DisguiseType.ZOMBIE &&
        disguise.getType() != DisguiseType.ZOMBIE_VILLAGER
      ) {
        event.setCancelled(true);
        return;
      }

      if(cooldownManager.isOnCooldown(player, CooldownType.HIT)) {
        sendCooldownMessage(player, CooldownType.HIT);
        event.setCancelled(true);
      } else {
        cooldownManager.addCooldown(player, 1250L, CooldownType.HIT);
      }
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!isControlling(player)) {
      return;
    }
    if (event.getItem() != null) {
      ItemStack item = event.getItem();
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (item.isSimilar(Items.getUndisguiser())) {
          if (DisguiseAPI.getDisguise(player) != null) {
            undisguise(player, false, true, true);
            MessageManager.sendMessage(player, MessageKey.NOT_DISGUISED);
          }
          event.setCancelled(true);
        } else if (item.isSimilar(Items.getCreeperExploder())) {
          event.setCancelled(true);
          if(cooldownManager.isOnCooldown(player, CooldownType.CREEPER_EXPLODE)) {
            sendCooldownMessage(player, CooldownType.CREEPER_EXPLODE);
            return;
          }
          cooldownManager.addCooldown(player, 2500L, CooldownType.CREEPER_EXPLODE);
          ((CreeperWatcher)DisguiseAPI.getDisguise(player).getWatcher()).setIgnited(true);
          player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
          new BukkitRunnable() {
            public void run() {
              undisguise(player, false, false, true);
              player.getWorld().createExplosion(player.getLocation(), 3.0f);
            }
          }.runTaskLater(VersusPlugin.getInstance(), 30L);
        }
        else if (item.isSimilar(Items.getGhastItem())) {
          event.setCancelled(true);
          if(cooldownManager.isOnCooldown(player, CooldownType.GHAST_BALL)) {
            sendCooldownMessage(player, CooldownType.GHAST_BALL);
            return;
          }
          cooldownManager.addCooldown(player, 2500L, CooldownType.GHAST_BALL);
          player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_WARN, 3.0f, 1.0f);
          player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.FIREBALL);
          ((GhastWatcher)DisguiseAPI.getDisguise(player).getWatcher()).setAggressive(true);
          new BukkitRunnable() {
            public void run() {
              Disguise disguise = DisguiseAPI.getDisguise(player);
              if (disguise != null && disguise.getType() == DisguiseType.GHAST) {
                ((GhastWatcher)DisguiseAPI.getDisguise(player).getWatcher()).setAggressive(false);
              }
            }
          }.runTaskLater(VersusPlugin.getInstance(), 10L);
        } else if (item.isSimilar(Items.getBlazeItem())) {
          event.setCancelled(true);
          if(cooldownManager.isOnCooldown(player, CooldownType.BLAZE_BALL)) {
            sendCooldownMessage(player, CooldownType.BLAZE_BALL);
            return;
          }
          cooldownManager.addCooldown(player, 2500L, CooldownType.BLAZE_BALL);
          new BukkitRunnable() {
            int counter = 3;

            public void run() {
              if (counter == 0) {
                cancel();
                return;
              }
              player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 3.0f, 1.0f);
              player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.SMALL_FIREBALL);
              --counter;
            }
          }.runTaskTimer(VersusPlugin.getInstance(), 0L, 7L);
        } else if (item.isSimilar(Items.getDragonItem())) {
          event.setCancelled(true);
          if(cooldownManager.isOnCooldown(player, CooldownType.ENDER_DRAGON_BALL)) {
            sendCooldownMessage(player, CooldownType.ENDER_DRAGON_BALL);
            return;
          }
          cooldownManager.addCooldown(player, 250L, CooldownType.ENDER_DRAGON_BALL);
          player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 3.0f, 1.0f);
          player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.DRAGON_FIREBALL);
          event.setCancelled(true);
        } else if (item.isSimilar(Items.getEndermanItem())) {
          event.setCancelled(true);
          if(cooldownManager.isOnCooldown(player, CooldownType.ENDERMAN_TP)) {
            sendCooldownMessage(player, CooldownType.ENDERMAN_TP);
            return;
          }
          Block targetBlockExact = player.getTargetBlockExact(60);
          if (targetBlockExact == null) {
            return;
          }
          cooldownManager.addCooldown(player, 5000L, CooldownType.ENDERMAN_TP);
          player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
          PaperLib.teleportAsync(player, targetBlockExact.getLocation());
          player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else if (item.isSimilar(Items.getDolphinItem())) {
          event.setCancelled(true);
          if(cooldownManager.isOnCooldown(player, CooldownType.DOLPHIN_LEAP)) {
            sendCooldownMessage(player, CooldownType.DOLPHIN_LEAP);
            return;
          }
          cooldownManager.addCooldown(player, 2000L, CooldownType.DOLPHIN_LEAP);
          player.setVelocity(player.getLocation().getDirection().multiply(2.3));
        }
      }

      if (item.isSimilar(Items.getNearest())) {
        event.setCancelled(true);
        Player toCheck = player;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
          Player nearestEntity = getNearestEntity(player, Player.class, player, false);
          if (nearestEntity != null) {
            toCheck = nearestEntity;
          }
        }
        LivingEntity nearestEntity = getNearestEntity(toCheck, LivingEntity.class, null, true);
        if (nearestEntity == null) {
          MessageManager.sendMessage(player, MessageKey.NOT_FIND_MOBS);
          return;
        }
        disguise(player, nearestEntity);
      } else {
        for (Map.Entry<EntityType, ItemStack> entry : Items.getEntityTypeItemStackMap().entrySet()) {
          if (item.isSimilar(entry.getValue())) {
            event.setCancelled(true);
            Player toCheck2 = player;
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
              Player nearestEntity3 = getNearestEntity(player, Player.class, player, false);
              if (nearestEntity3 != null) {
                toCheck2 = nearestEntity3;
              }
            }

            LivingEntity nearest = getNearestEntity(toCheck2, (Class<LivingEntity>)entry.getKey().getEntityClass(), null, false);
            if (nearest == null) {
              MessageManager.setValue("entity_type", entry.getKey().name().replaceAll("_", " "));
              MessageManager.sendMessage(player, MessageKey.NOT_FIND_MOBS_TYPE);
              return;
            }
            disguise(player, nearest);
          }
        }
      }
    } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
      BlockIterator iterator = new BlockIterator(player.getEyeLocation(), 0.0, 60);
      while (iterator.hasNext()) {
        Block block = iterator.next();
        if (block.getType().isOccluding()) {
          break;
        }
        boolean entityFound = false;
        for (Entity entity : player.getWorld().getEntitiesByClasses(LivingEntity.class)) {
          if (entity instanceof Player) {
            continue;
          }
          if (entity.getLocation().distanceSquared(block.getLocation()) < 2.0) {
            disguise(player, (LivingEntity)entity);
            event.setCancelled(true);
            entityFound = true;
            break;
          }
        }
        if (entityFound) {
          break;
        }
      }
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onCompassTrack(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (
      isControlling(player) &&
      event.getItem() != null &&
      event.getItem().getType() == Material.COMPASS &&
      (
        event.getAction() == Action.RIGHT_CLICK_BLOCK ||
        event.getAction() == Action.RIGHT_CLICK_AIR)
    ) {
      Player nearest = null;
      double distance = Double.MAX_VALUE;
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (!onlinePlayer.equals(player) && onlinePlayer.getWorld().equals(player.getWorld())) {
          if (isControlling(onlinePlayer)) {
            continue;
          }
          double distanceSquared = onlinePlayer.getLocation().distanceSquared(player.getLocation());
          if (distanceSquared >= distance) {
            continue;
          }
          distance = distanceSquared;
          nearest = onlinePlayer;
        }
      }
      if (nearest == null) {
        MessageManager.sendMessage(player, MessageKey.NO_PLAYER_TRACK);
        return;
      }
      player.setCompassTarget(nearest.getLocation());
      MessageManager.setValue("player_name", nearest.getName());
      MessageManager.sendMessage(player, MessageKey.TRACK);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    event.getPlayer().setGlowing(true);
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
    Player player = event.getPlayer();
    if (isControlling(player)) {
      undisguise(player, false, false, true);
      giveHotbar(player);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player && isControlling((Player)event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if(isControlling(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
    if (
      event.getEntity() instanceof Player &&
      isControlling((Player)event.getEntity()) &&
      event.getFoodLevel() < ((Player)event.getEntity()).getFoodLevel()
    ) {
      event.setCancelled(true);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onPlayerQuitEvent(PlayerQuitEvent event) {
    undisguise(event.getPlayer(), false, false, false);
    controllers.remove(event.getPlayer().getUniqueId());
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onPlayerKickEvent(PlayerKickEvent event) {
    Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer());
    if (
      disguise != null &&
      disguise.getType() == DisguiseType.SPIDER &&
      event.getReason().toLowerCase().contains("flying")
    ) {
      event.setCancelled(false);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler(ignoreCancelled = true)
  public void onPlayerMoveEvent(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (
      event.getFrom().getX() == event.getTo().getX() &&
      event.getFrom().getY() == event.getTo().getY() &&
      event.getFrom().getZ() == event.getTo().getZ()
    ) {
      return;
    }
    Disguise disguise = DisguiseAPI.getDisguise(player);
    if (disguise == null) {
      return;
    }
    if (disguise.getType() == DisguiseType.ENDERMAN) {
      if (event.getTo().getBlock().getType() == Material.WATER) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        Location loc = player.getWorld().getHighestBlockAt(player.getLocation().clone().add((random.nextDouble() - 0.5) * 64.0, random.nextInt(64) - 32, (random.nextDouble() - 0.5) * 64.0)).getLocation();
        PaperLib.teleportAsync(player, loc);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
      }
    }
    else if (disguise.getType() == DisguiseType.SPIDER) {
      if (!vineEvents.containsKey(player.getUniqueId())) {
        vineEvents.put(player.getUniqueId(), 1);
      }
      if (vineEvents.get(player.getUniqueId()) != 10) {
        vineEvents.put(player.getUniqueId(), vineEvents.get(player.getUniqueId()) + 1);
        return;
      }
      vineEvents.remove(player.getUniqueId());
      for (int x = -2; x <= 2; ++x) {
        for (int y = -2; y <= 2; ++y) {
          for (int z = -2; z <= 2; ++z) {
            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX() + x, player.getLocation().getBlockY() + y, player.getLocation().getBlockZ() + z);
            if (block.getType() == Material.AIR || block.getType() == Material.VOID_AIR || block.getType() == Material.CAVE_AIR) {
              if (block.getY() > 0) {
                if (block.getRelative(BlockFace.UP).getType() != Material.VINE) {
                  BlockData blockData = Material.VINE.createBlockData();
                  MultipleFacing vine = (MultipleFacing)blockData;
                  boolean found = false;
                  for (BlockFace blockFace : blockFaces) {
                    Block currentBlock = block.getRelative(blockFace);
                    Material type = currentBlock.getType();
                    if (
                      type != Material.AIR &&
                      type != Material.VINE &&
                      !currentBlock.isLiquid() &&
                      type.isSolid() &&
                      type != Material.SNOW &&
                      type != Material.LILY_PAD &&
                      type != Material.FIRE &&
                      type != Material.TORCH &&
                      type != Material.REDSTONE_WIRE &&
                      type != Material.REDSTONE_TORCH &&
                      !Tag.SIGNS.isTagged(type) &&
                      !Tag.STANDING_SIGNS.isTagged(type)
                    ) {
                      if (!Tag.WALL_SIGNS.isTagged(type)) {
                        found = true;
                        vine.setFace(blockFace, true);
                      }
                    }
                  }
                  if (found) {
                    player.sendBlockChange(block.getLocation(), vine);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onEntityTargetEvent(EntityTargetEvent event) {
    if (event.getTarget() instanceof Player && this.isControlling((Player)event.getTarget())) {
      event.setCancelled(true);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
    if (event.getEntity() instanceof Player && isControlling((Player)event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @SuppressWarnings("unused")
  @EventHandler
  public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
    if (event.getEntity() instanceof ExperienceOrb && event.getTarget() instanceof Player && isControlling((Player)event.getTarget())) {
      event.setCancelled(true);
    }
  }

  private <E extends LivingEntity> E getNearestEntity(Player player, Class<E> entity, Player ignore, boolean ignorePlayers) {
    E nearest = null;
    double bestDistance = Double.MAX_VALUE;
    for (E livingEntity : player.getWorld().getEntitiesByClass(entity)) {
      if (!livingEntity.isDead() && livingEntity.isValid()) {
        if (livingEntity.getHealth() <= 0.0) {
          continue;
        }
        if (livingEntity.equals(ignore)) {
          continue;
        }
        if (ignorePlayers && livingEntity instanceof Player) {
          continue;
        }
        double distance = livingEntity.getLocation().distanceSquared(player.getLocation());
        if (distance >= bestDistance) {
          continue;
        }
        nearest = livingEntity;
        bestDistance = distance;
      }
    }
    return nearest;
  }

  private void giveHotbar(Player player) {
    player.getInventory().clear();
    switch (player.getWorld().getEnvironment()) {
      case NORMAL:
        player.getInventory().setItem(2, Items.getNearest());
        player.getInventory().setItem(3, Items.getItem(EntityType.CREEPER));
        player.getInventory().setItem(4, Items.getItem(EntityType.SKELETON));
        player.getInventory().setItem(5, Items.getItem(EntityType.ENDERMAN));
        player.getInventory().setItem(6, Items.getItem(EntityType.ZOMBIE));
        player.getInventory().setItem(7, Items.getItem(EntityType.SPIDER));
        break;
      case NETHER:
        player.getInventory().setItem(2, Items.getNearest());
        player.getInventory().setItem(3, Items.getItem(EntityType.SKELETON));
        player.getInventory().setItem(4, Items.getItem(EntityType.ENDERMAN));
        player.getInventory().setItem(5, Items.getItem(EntityType.GHAST));
        player.getInventory().setItem(6, Items.getItem(EntityType.BLAZE));
        player.getInventory().setItem(7, Items.getItem(EntityType.WITHER_SKELETON));
        break;
      case THE_END:
        player.getInventory().setItem(5, Items.getNearest());
        player.getInventory().setItem(6, Items.getItem(EntityType.ENDERMAN));
        player.getInventory().setItem(7, Items.getItem(EntityType.ENDER_DRAGON));
        break;
    }
    player.getInventory().setItem(1, new ItemStack(Material.COMPASS));
    player.getInventory().setItem(8, Items.getUndisguiser());
  }

  private void toggleInvisibility(Player player, boolean invisible) {
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      if (onlinePlayer.equals(player)) {
        continue;
      }
      if (invisible) {
        onlinePlayer.hidePlayer(VersusPlugin.getInstance(), player);
      } else {
        onlinePlayer.showPlayer(VersusPlugin.getInstance(), player);
      }
    }
  }

  private void undisguise(Player player, boolean toNew, boolean spawn, boolean giveHotbar) {
    toggleInvisibility(player, true);
    Disguise disguise = DisguiseAPI.getDisguise(player);
    if (disguise != null) {
      for (PotionEffect activePotionEffect : player.getActivePotionEffects()) {
        player.removePotionEffect(activePotionEffect.getType());
      }
      if (spawn) {
        LivingEntity entity = (LivingEntity)player.getWorld().spawnEntity(player.getLocation(), disguise.getType().getEntityType());
        if (entity instanceof Ageable) {
          ((Ageable)entity).setAdult();
        }
        if (entity instanceof Horse) {
          Horse horse = (Horse)entity;
          EntityEquipment equipment = horse.getEquipment();
          if (equipment != null) {
            equipment.setArmorContents(disguise.getWatcher().getEquipment().getArmorContents());
          }
          if (((HorseWatcher)disguise.getWatcher()).isSaddled()) {
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
          }
          horse.setColor(((HorseWatcher)disguise.getWatcher()).getColor());
          horse.setStyle(((HorseWatcher)disguise.getWatcher()).getStyle());
        }
        entity.setHealth(player.getHealth());
      }
      if (!toNew) {
        if (player.getGameMode() != GameMode.CREATIVE) {
          player.setFlying(false);
          player.setAllowFlight(false);
        }
        player
          .getAttribute(Attribute.GENERIC_MAX_HEALTH)
          .setBaseValue(
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue()
          );
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
      }
      disguise.stopDisguise();
      player.getInventory().clear();
      if (giveHotbar) {
        giveHotbar(player);
      }
    }
  }

  private void disguise(Player player, LivingEntity entity) {
    undisguise(player, true, true, true);
    toggleInvisibility(player, false);
    PaperLib.teleportAsync(player, entity.getLocation());
    player
      .getAttribute(Attribute.GENERIC_MAX_HEALTH)
      .setBaseValue(
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
          .getBaseValue()
      );
    player.setHealth(entity.getHealth());

    MobDisguise disguise = new MobDisguise(DisguiseType.getType(entity), !(entity instanceof Ageable) || ((Ageable)entity).isAdult());
    disguise.setHideArmorFromSelf(true);
    disguise.setHideHeldItemFromSelf(true);
    disguise.setViewSelfDisguise(false);
    disguise.getWatcher().setCustomNameVisible(false);
    if (entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.STRAY) {
      disguise.getWatcher().setItemInMainHand(new ItemStack(Material.BOW));
      ItemStack itemStack = new ItemStack(Material.BOW);
      itemStack.addEnchantment(Enchantment.ARROW_INFINITE, 1);
      itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
      player.getInventory().setItem(0, itemStack);
      player.getInventory().setItem(9, new ItemStack(Material.ARROW, 64));
    } else if (entity.getType() == EntityType.PILLAGER) {
      disguise.getWatcher().setItemInMainHand(new ItemStack(Material.CROSSBOW));
      ItemStack itemStack = new ItemStack(Material.CROSSBOW);
      itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
      player.getInventory().setItem(0, itemStack);
      player.getInventory().setItem(9, new ItemStack(Material.ARROW, 64));
    } else if (entity.getType() == EntityType.CREEPER) {
      player.getInventory().setItem(0, Items.getCreeperExploder());
    } else if (entity.getType() == EntityType.GHAST) {
      player.getInventory().setItem(0, Items.getGhastItem());
    } else if (entity.getType() == EntityType.BLAZE) {
      player.getInventory().setItem(0, Items.getBlazeItem());
    } else if (entity.getType() == EntityType.ENDER_DRAGON) {
      player.getInventory().setItem(0, Items.getDragonItem());
    } else if (entity.getType() == EntityType.DOLPHIN) {
      player.getInventory().setItem(0, Items.getDolphinItem());
      player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false, false));
    } else if (entity.getType() == EntityType.ENDERMAN) {
      player.getInventory().setItem(0, Items.getEndermanItem());
      ((EndermanWatcher)disguise.getWatcher()).setAggressive(true);
    } else if (entity.getType() == EntityType.HORSE) {
      disguise.getWatcher().setArmor(entity.getEquipment().getArmorContents());
      ((HorseWatcher)disguise.getWatcher()).setSaddled(((Horse)entity).getInventory().getSaddle() != null);
      ((HorseWatcher)disguise.getWatcher()).setColor(((Horse)entity).getColor());
      ((HorseWatcher)disguise.getWatcher()).setStyle(((Horse)entity).getStyle());
    }

    if (entity instanceof WaterMob) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false, false));
    }

    if (entity.getType() == EntityType.BAT || entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.GHAST || entity.getType() == EntityType.PHANTOM || entity.getType() == EntityType.VEX || entity.getType() == EntityType.WITHER || entity.getType() == EntityType.SHULKER) {
      if (player.getGameMode() != GameMode.CREATIVE) {
        player.setAllowFlight(true);
        player.setFlying(true);
      }
    } else if (player.getGameMode() != GameMode.CREATIVE) {
      player.setFlying(false);
      player.setAllowFlight(false);
    }

    disguise.setEntity(player);
    disguise.startDisguise();
    entity.remove();
    MessageManager.setValue("entity_type", entity.getType().name().replaceAll("_", " "));
    MessageManager.sendMessage(player, MessageKey.NOW_CONTROLLING);
  }

  private void addPlayerInternal(Player player) {
    controllers.add(player.getUniqueId());
    player.setGlowing(false);
    giveHotbar(player);
    toggleInvisibility(player, true);
    MessageManager.setValue("player_name", player.getName());
    MessageManager.broadcast(MessageKey.CONTROL_MOBS);
    MessageManager.sendMessage(player, MessageKey.CONTROL_INSTRUCTION);
  }

  private void removePlayerInternal(Player player) {
    controllers.remove(player.getUniqueId());
    player.setGlowing(true);
    player.getInventory().clear();
    undisguise(player, false, true, false);
    toggleInvisibility(player, false);
    MessageManager.setValue("player_name", player.getName());
    MessageManager.broadcast(MessageKey.NO_CONTROL_MOBS);
  }

  public boolean isControlling(Player player) {
    return controllers.contains(player.getUniqueId());
  }

  public static void addPlayer(Player player) {
    instance.addPlayerInternal(player);
  }

  public static void removePlayer(Player player) {
    instance.removePlayerInternal(player);
  }
}

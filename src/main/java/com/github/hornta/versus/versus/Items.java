package com.github.hornta.versus.versus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Items {
  private static Map<EntityType, ItemStack> entityTypeItemStackMap;

  public static Map<EntityType, ItemStack> getEntityTypeItemStackMap() {
    return entityTypeItemStackMap;
  }

  public static ItemStack getItem(EntityType entityType) {
    return entityTypeItemStackMap.get(entityType);
  }

  public static ItemStack getCreeperExploder() {
    ItemStack itemStack = new ItemStack(Material.TNT);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Explode");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to explode"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getGhastItem() {
    ItemStack itemStack = new ItemStack(Material.FIRE_CHARGE);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Fireball");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to shoot"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getBlazeItem() {
    ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Fireball");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to shoot"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getDragonItem() {
    ItemStack itemStack = new ItemStack(Material.DRAGON_BREATH);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Dragon Fireball");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to shoot"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getEndermanItem() {
    ItemStack itemStack = new ItemStack(Material.ENDER_PEARL);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Teleport");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to teleport"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getUndisguiser() {
    ItemStack itemStack = new ItemStack(Material.BARRIER);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Undisguise");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to undisguise"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getNearest() {
    ItemStack itemStack = new ItemStack(Material.DIAMOND);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Nearest Mob");
    itemMeta.setLore(Arrays.asList(ChatColor.WHITE + "Left click to teleport", ChatColor.WHITE + "to the nearest mob (nearest player)", ChatColor.WHITE + "Right click to teleport", ChatColor.WHITE + "to the nearest mob (you)"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack getDolphinItem() {
    ItemStack itemStack = new ItemStack(Material.DOLPHIN_SPAWN_EGG);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Dolphin Leap");
    itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Right click to leap"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  private static ItemStack makeItem(Material material) {
    String name = material.name().replace("_SPAWN_EGG", "").toLowerCase().replace("_", " ");
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD + "Teleport to nearest " + name);
    itemMeta.setLore(Arrays.asList(ChatColor.WHITE + "Left click to teleport", ChatColor.WHITE + "to the nearest " + name + " (nearest player)", ChatColor.WHITE + "Right click to teleport", ChatColor.WHITE + "to the nearest " + name + " (you)"));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  static {
    entityTypeItemStackMap = new HashMap<>();
    entityTypeItemStackMap.put(EntityType.CREEPER, makeItem(Material.CREEPER_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.SKELETON, makeItem(Material.SKELETON_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.ENDERMAN, makeItem(Material.ENDERMAN_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.ZOMBIE, makeItem(Material.ZOMBIE_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.SPIDER, makeItem(Material.SPIDER_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.GHAST, makeItem(Material.GHAST_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.BLAZE, makeItem(Material.BLAZE_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.WITHER_SKELETON, makeItem(Material.WITHER_SKELETON_SPAWN_EGG));
    entityTypeItemStackMap.put(EntityType.ENDER_DRAGON, makeItem(Material.DRAGON_HEAD));
  }
}
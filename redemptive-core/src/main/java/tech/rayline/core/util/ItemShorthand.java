package tech.rayline.core.util;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter public final class ItemShorthand {
    private final Material material;
    private String name;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;
    private short dataValue;
    private int quantity;

    public ItemShorthand(Material m) {
        this.material = m;
    }

    public static ItemShorthand setMaterial(Material material) {
        return new ItemShorthand(material);
    }

    public static ItemShorthand withMaterial(Material material) {
        return setMaterial(material);
    }

    public ItemShorthand setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public ItemShorthand withQuantity(int quantity) {
        return setQuantity(quantity);
    }

    public ItemShorthand setName(String name) {
        this.name = name;
        return this;
    }

    public ItemShorthand withName(String name) {
        return setName(name);
    }

    public ItemShorthand setLore(String l) {
        checkLore();
        lore.add(ChatColor.translateAlternateColorCodes('&', l));
        return this;
    }

    public ItemShorthand withLore(String lore) {
        return setLore(lore);
    }

    public ItemShorthand setDataValue(short dataValue) {
        this.dataValue = dataValue;
        return this;
    }

    public ItemShorthand withDataValue(short dataValue) {
        return setDataValue(dataValue);
    }

    public ItemShorthand setEnchantment(Enchantment enchantment, int level) {
        checkEnchantments();
        enchantments.put(enchantment, level);
        return this;
    }

    public ItemShorthand withEnchantment(Enchantment enchantment, int level) {
        return setEnchantment(enchantment, level);
    }

    public ItemShorthand withLore(List<String> lore) {
        checkLore();
        this.lore.clear();
        this.lore.addAll(lore);
        return this;
    }

    private void checkLore() {
        if (lore == null) lore = new ArrayList<>();
    }

    private void checkEnchantments() {
        if (enchantments == null) enchantments = new HashMap<>();
    }

    public ItemStack get() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        if (quantity > 1) item.setAmount(quantity);
        if (dataValue > 0) item.setDurability(dataValue);
        if (enchantments != null) item.addUnsafeEnchantments(enchantments);
        return item;
    }
}

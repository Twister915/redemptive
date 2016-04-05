package tech.rayline.core;

import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemStackTypeAdapter extends TypeAdapter<ItemStack> {
    private final static String
            MATERIAL = "m",
            DATA_VALUE = "d",
            AMOUNT = "a",
            LORE = "l",
            NAME = "n",
            ENCHANTS = "e";

    @Override
    public void write(JsonWriter out, ItemStack value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name(MATERIAL);
        out.value(value.getType().name());
        short durability = value.getDurability();
        if (durability != 0) {
            out.name(DATA_VALUE);
            out.value(durability);
        }
        int amount = value.getAmount();
        if (amount != 1) {
            out.name(AMOUNT);
            out.value(amount);
        }
        ItemMeta itemMeta = value.getItemMeta();
        if (itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore != null) {
                out.name(LORE);
                out.beginArray();
                for (String s : lore)
                    out.value(s);
                out.endArray();
            }
            String name = itemMeta.getDisplayName();
            if (name != null) {
                out.name(NAME);
                out.value(name);
            }
        }
        Map<Enchantment, Integer> enchantments = value.getEnchantments();
        if (enchantments != null && !enchantments.isEmpty()) {
            out.name(ENCHANTS);
            out.beginObject();
            for (Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : enchantments.entrySet()) {
                out.name(enchantmentIntegerEntry.getKey().getName());
                out.value(enchantmentIntegerEntry.getValue());
            }
            out.endObject();
        }
        out.endObject();
    }

    @Override
    public ItemStack read(JsonReader in) throws IOException {
        Material material = null;
        Integer amount = null;
        Short dataValue = null;
        String name = null;
        List<String> lore = new ArrayList<>();
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return null;
        }

        in.beginObject();

        while (in.hasNext())
            switch (in.nextName()) {
                case MATERIAL:
                    material = Material.getMaterial(in.nextString());
                    break;
                case DATA_VALUE:
                    dataValue = (short) in.nextInt();
                    break;
                case AMOUNT:
                    amount = in.nextInt();
                    break;
                case LORE:
                    in.beginArray();
                    while (in.hasNext()) {
                        lore.add(in.nextString());
                    }
                    in.endArray();
                    break;
                case NAME:
                    name = in.nextString();
                    break;
                case ENCHANTS:
                    in.beginObject();
                    while (in.hasNext())
                        enchantments.put(Enchantment.getByName(in.nextName()), in.nextInt());
                    in.endObject();
                    break;
        }

        if (material == null)
            throw new JsonParseException("An ItemStack must have a material field!");

        ItemStack stack = new ItemStack(material);
        if (amount != null) stack.setAmount(amount);
        if (dataValue != null) stack.setDurability(dataValue);
        if (name != null || !lore.isEmpty()) {
            ItemMeta itemMeta = stack.getItemMeta();
            if (name != null) itemMeta.setDisplayName(name);
            if (!lore.isEmpty()) itemMeta.setLore(lore);
            stack.setItemMeta(itemMeta);
        }
        if (!enchantments.isEmpty())
            for (Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : enchantments.entrySet()) {
                stack.addUnsafeEnchantment(enchantmentIntegerEntry.getKey(), enchantmentIntegerEntry.getValue());
            }
        in.endObject();

        return stack;
    }
}

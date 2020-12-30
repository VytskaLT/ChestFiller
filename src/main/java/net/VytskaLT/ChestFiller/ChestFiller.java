package net.VytskaLT.ChestFiller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Library for filling chests with specified loot.
 *
 * @author VytskaLT
 * @version 1.0
 */
@UtilityClass
public class ChestFiller {

    private static JsonParser jsonParser;
    private static SplittableRandom random;

    /**
     * If the random isn't initialised, initialises it.
     */
    public void initRandom() {
        if (random == null)
            random = new SplittableRandom();
    }

    /**
     * Parses entries from JSON.
     *
     * @param json JSON string to parse.
     * @return parsed entries.
     */
    public Entry[] parse(String json) {
        if (jsonParser == null)
            jsonParser = new JsonParser();
        try {
            return parse(jsonParser.parse(json));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses entries from JSON.
     *
     * @param element JSON element to parse.
     * @return parsed entries.
     */
    public Entry[] parse(JsonElement element) {
        return element == null ? null : element.isJsonArray() ? parse((JsonArray) element) : null;
    }

    /**
     * Parses entries from JSON.
     *
     * @param array JSON array to parse.
     * @return parsed entries.
     */
    public Entry[] parse(JsonArray array) {
        Entry[] entries = new Entry[array.size()];
        byte i = 0;
        for (JsonElement element : array) {
            JsonObject object;

            try {
                object = (JsonObject) element;
            } catch (ClassCastException e) {
                return null;
            }

            byte min = 0, max = 1;
            {
                try {
                    min = object.get("min").getAsByte();
                } catch (Exception ignored) {
                }
                try {

                    max = object.get("max").getAsByte();
                } catch (Exception ignored) {
                }
            }

            JsonArray itemsArray;
            try {
                itemsArray = (JsonArray) object.get("items");
            } catch (Exception e) {
                return null;
            }

            Item[] itemEntries = new Item[itemsArray.size()];
            try {
                byte x = 0;
                for (JsonElement items : itemsArray) {
                    if (items.isJsonObject()) {
                        JsonObject obj = (JsonObject) items;
                        Material material;
                        short data;
                        Object2ByteOpenHashMap<Enchantment> enchantments = null;

                        material = Material.valueOf(obj.get("id").getAsString());
                        try {
                            data = obj.get("data").getAsShort();
                        } catch (Exception ignored) {
                            data = 0;
                        }
                        try {
                            JsonObject enchantmentsObject = obj.get("enchantments").getAsJsonObject();
                            enchantments = new Object2ByteOpenHashMap<>();
                            for (Map.Entry<String, JsonElement> entry : enchantmentsObject.entrySet())
                                enchantments.put(Enchantment.getByName(entry.getKey()), entry.getValue().getAsByte());
                        } catch (Exception ignored) {
                        }

                        itemEntries[x] = new Item(material, data, enchantments);
                    } else itemEntries[x] = new Item(Material.getMaterial(items.getAsString()), (short) 0, null);

                    x++;
                }
            } catch (Exception e) {
                return null;
            }

            if (min > max)
                return null;

            if (max < 0)
                return null;

            entries[i] = new Entry(min, max, itemEntries);
            i++;
        }
        return entries;
    }

    /**
     * Fills an array of chests with specified entries.
     *
     * @param entries entries to fill chests with.
     * @param chests  chests to fill.
     * @return if the fill was successful.
     */
    public boolean fill(Entry[] entries, Chest[] chests) {
        if (chests == null)
            return false;
        if (chests.length == 0)
            return true;
        return fill0(entries, new ObjectArrayList<>(chests));
    }

    /**
     * Fills a list of chests with specified entries.
     *
     * @param entries entries to fill chests with.
     * @param chests  chests to fill.
     * @return if the fill was successful.
     */
    public boolean fill(Entry[] entries, List<Chest> chests) {
        if (chests == null)
            return false;
        return fill0(entries, chests);
    }

    private boolean fill0(Entry[] entries, List<Chest> chests) {
        if (entries == null)
            return false;
        for (Chest chest : chests)
            chest.getBlockInventory().clear();
        if (entries.length == 0)
            return true;
        initRandom();

        List<ItemStack> items = new ObjectArrayList<>();

        for (Entry entry : entries) {
            byte amount = (byte) ((byte) random.nextInt((entry.max - entry.min) + 1) + entry.min);
            if (amount <= 0)
                continue;

            do {
                Item randomItem = entry.items[random.nextInt(entry.items.length)];
                byte itemAmount = (byte) (random.nextInt(Math.max(randomItem.material.getMaxStackSize() / 2, 1) + 1) + (randomItem.material.getMaxStackSize() / 2));
                itemAmount = itemAmount > amount ? amount : itemAmount;
                if (itemAmount == 0)
                    continue;
                amount -= itemAmount;
                items.add(randomItem.create(itemAmount));
            } while (amount != 0);
        }

        if (items.isEmpty())
            return true;

        Collections.shuffle(items);

        main:
        for (ItemStack item : items) {
            Chest chest = chests.get(random.nextInt(chests.size()));

            byte index;
            do {
                index = (byte) random.nextInt(chest.getBlockInventory().getSize());
            } while (chest.getBlockInventory().getItem(index) != null);

            chest.getBlockInventory().setItem(index, item);

            for (byte i = 0; i < 27; i++)
                if (chest.getBlockInventory().getItem(i) == null)
                    continue main;

            chests.remove(chest);
            if (chests.isEmpty())
                return false;
        }

        return true;
    }

    @Value
    public class Entry {
        byte min, max;
        Item[] items;
    }

    @Value
    public class Item {
        Material material;
        short data;
        Object2ByteOpenHashMap<Enchantment> enchantments;

        public ItemStack create(byte amount) {
            ItemStack item = new ItemStack(material, amount, data);
            if (enchantments != null)
                enchantments.forEach(item::addUnsafeEnchantment);
            return item;
        }
    }
}

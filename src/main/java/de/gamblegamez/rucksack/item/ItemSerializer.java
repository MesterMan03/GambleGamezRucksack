package de.gamblegamez.rucksack.item;

import com.github.luben.zstd.Zstd;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemSerializer {
    /**
     * Serializes a collection of {@link ItemStack} objects to a compressed byte array.
     *
     * <p>The format is as follows:</p>
     * <ul>
     *   <li>int (4 bytes): size of serialized data</li>
     *   <li>int (4 bytes): size of compressed data</li>
     *   <li>N bytes: compressed data</li>
     * </ul>
     *
     * <p>The uncompressed data is a list of the following:</p>
     * <ul>
     *   <li>byte: slot index</li>
     *   <li>int (4 bytes): size of item data</li>
     *   <li>N bytes: item serialized using built-in Paper APIs</li>
     * </ul>
     *
     * @param items the collection of items to serialize and compress
     * @return a byte array of the compressed serialized item data
     */
    public static byte[] serializeItems(Collection<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteStream)) {

            int slot = 0;
            boolean hasItems = false;

            for (ItemStack itemStack : items) {
                if (itemStack == null || itemStack.isEmpty()) {
                    slot++;
                    continue;
                }

                // serialize the item using Paper's built-in method
                byte[] serializedData = itemStack.serializeAsBytes();
                if (serializedData == null || serializedData.length == 0) {
                    slot++;
                    continue;
                }

                hasItems = true;

                dataOut.writeByte(slot);                 // slot index
                dataOut.writeInt(serializedData.length); // size of serialized data
                dataOut.write(serializedData);           // serialized item data

                slot++;
            }

            if (!hasItems) {
                return new byte[0];
            }

            dataOut.flush();
            byte[] uncompressed = byteStream.toByteArray();

            // compress the serialized data
            byte[] compressed = Zstd.compress(uncompressed, 22);

            // prepend the two ints for total and compressed size
            ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + compressed.length);
            buffer.putInt(uncompressed.length);
            buffer.putInt(compressed.length);
            buffer.put(compressed);

            return buffer.array();

        } catch (IOException e) {
            // should not happen with ByteArrayOutputStream
            throw new RuntimeException("Failed to serialize items", e);
        }
    }

    public static Collection<ItemStack> deserializeItems(byte[] data) {
        if (data == null || data.length == 0) {
            return List.of();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        int serializedSize = buffer.getInt();
        int compressedSize = buffer.getInt();

        if (serializedSize <= 0 || compressedSize <= 0) {
            return List.of();
        }

        byte[] compressed = new byte[compressedSize];
        buffer.get(compressed);

        // decompress
        byte[] decompressed = Zstd.decompress(compressed, serializedSize);

        List<ItemStack> items = new ArrayList<>();

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(decompressed);
             DataInputStream input = new DataInputStream(byteStream)) {

            while (input.available() > 0) {
                int slot = input.readByte() & 0xFF; // preserve unsigned behavior
                int dataSize = input.readInt();

                byte[] itemBytes = new byte[dataSize];
                input.readFully(itemBytes);

                // deserialize using Paper API
                ItemStack item = ItemStack.deserializeBytes(itemBytes);

                // fill missing slots with empty items
                while (items.size() < slot) {
                    items.add(ItemStack.empty());
                }

                items.add(item);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize items", e);
        }

        return items;
    }
}

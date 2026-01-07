package fr.nexus.api.var.types;

import fr.nexus.Core;
import fr.nexus.api.var.types.parents.normal.adventure.ComponentType;
import fr.nexus.api.var.types.parents.normal.big.BigDecimalType;
import fr.nexus.api.var.types.parents.normal.big.BigIntegerType;
import fr.nexus.api.var.types.parents.normal.bukkit.*;
import fr.nexus.api.var.types.parents.normal.bukkit.InventoryType;
import fr.nexus.api.var.types.parents.normal.bukkit.ItemStackType;
import fr.nexus.api.var.types.parents.normal.bukkit.WorldType;
import fr.nexus.api.var.types.parents.normal.java.*;
import fr.nexus.api.var.types.parents.normal.java.date.*;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface VarTypes{
    static void generateFileTypes() throws IOException {
        Map<String, String> typesMap = new LinkedHashMap<>();
        typesMap.put("StringType", "String");
        typesMap.put("UUIDType", "UUID");
        typesMap.put("IntegerType", "Integer");
        typesMap.put("IntArrayType", "int[]");
        typesMap.put("LongType", "Long");
        typesMap.put("LongArrayType", "long[]");
        typesMap.put("ShortType", "Short");
        typesMap.put("ShortArrayType", "short[]");
        typesMap.put("FloatType", "Float");
        typesMap.put("FloatArrayType", "float[]");
        typesMap.put("DoubleType", "Double");
        typesMap.put("DoubleArrayType", "double[]");
        typesMap.put("CharacterType", "Character");
        typesMap.put("CharArrayType", "char[]");
        typesMap.put("ByteType", "Byte");
        typesMap.put("ByteArrayType", "byte[]");
        typesMap.put("PathType", "Path");
        typesMap.put("BooleanType", "Boolean");
        typesMap.put("BooleanArrayType", "boolean[]");
        typesMap.put("DateType", "Date");
        typesMap.put("InstantType", "Instant");
        typesMap.put("LocalDateType", "LocalDate");
        typesMap.put("LocalDateTimeType", "LocalDateTime");
        typesMap.put("PeriodType", "Period");
        typesMap.put("DurationType", "Duration");
        typesMap.put("ComponentType", "Component");
        typesMap.put("BigIntegerType", "BigInteger");
        typesMap.put("BigDecimalType", "BigDecimal");
        typesMap.put("WorldType", "World");
        typesMap.put("BlockDataType", "BlockData");
        typesMap.put("ChunkType", "Chunk");
        typesMap.put("DeprecatedMaterialType", "Material");
        typesMap.put("MaterialType", "Material");
        typesMap.put("LocationType", "Location");
        typesMap.put("ItemStackType", "ItemStack");
        typesMap.put("GameModeType", "GameMode");
        typesMap.put("InventoryType", "Inventory");
        typesMap.put("VectorType", "Vector");
        typesMap.put("BoundingBoxType", "BoundingBox");

        File genFolder = new File(Core.getInstance().getDataFolder(), "generated");
        if (!genFolder.exists()) genFolder.mkdirs();

        File outputFile = new File(genFolder, "VarTypes.java");

        try (FileWriter writer = new FileWriter(outputFile)) {
            for (Map.Entry<String, String> entry : typesMap.entrySet()) {
                String typeClass = entry.getKey();   // Ex: UUIDType
                String javaType = entry.getValue(); // Ex: UUID
                String varName = typeClass.replace("Type", "").toUpperCase();

                writer.write("    // " + varName + "\n");
                writer.write("    @NotNull " + typeClass + " " + varName + " = new " + typeClass + "();\n");

                // Utilisation de VarSubType avec le bon Type Générique
                writeLine(writer, varName, "CONCURRENT_SKIP_LIST_SET", "ConcurrentSkipListSet<" + javaType + ">", "concurrent_skip_list_sets()");
                writeLine(writer, varName, "CONCURRENT_SET", "ConcurrentHashMap.KeySetView<" + javaType + ", Boolean>", "concurrent_sets()");
                writeLine(writer, varName, "LINKED_SET", "LinkedHashSet<" + javaType + ">", "linked_sets()");
                writeLine(writer, varName, "TREE_SET", "TreeSet<" + javaType + ">", "tree_sets()");
                writeLine(writer, varName, "SET", "Set<" + javaType + ">", "sets()");
                writeLine(writer, varName, "LIST", "List<" + javaType + ">", "lists()");

                writeLine(writer, varName, "PRIORITY_BLOCKING_QUEUE", "PriorityBlockingQueue<" + javaType + ">", "priority_blocking_queues()");
                writeLine(writer, varName, "PRIORITY_QUEUE", "PriorityQueue<" + javaType + ">", "priority_queues()");
                writeLine(writer, varName, "ARRAY_DEQUE", "ArrayDeque<" + javaType + ">", "array_deques()");
                writeLine(writer, varName, "STACK", "Stack<" + javaType + ">", "stacks()");
                writer.write("    @NotNull " + typeClass + ".ArrayType " + varName + "_ARRAY = " + varName + ".arrays();\n");
                writer.write("\n");
            }
        }
    }

    // Méthode helper pour éviter les répétitions dans le générateur
    private static void writeLine(FileWriter w, String varName, String suffix, String generic, String method) throws IOException {
        w.write("    @NotNull VarSubType<" + generic + "> " + varName + "_" + suffix + " = " + varName + "." + method + ";\n");
    }

    // STRING
    @NotNull StringType STRING = new StringType();
    @NotNull VarSubType<ConcurrentSkipListSet<String>> STRING_CONCURRENT_SKIP_LIST_SET = STRING.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<String, Boolean>> STRING_CONCURRENT_SET = STRING.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<String>> STRING_LINKED_SET = STRING.linked_sets();
    @NotNull VarSubType<TreeSet<String>> STRING_TREE_SET = STRING.tree_sets();
    @NotNull VarSubType<Set<String>> STRING_SET = STRING.sets();
    @NotNull VarSubType<List<String>> STRING_LIST = STRING.lists();
    @NotNull VarSubType<PriorityBlockingQueue<String>> STRING_PRIORITY_BLOCKING_QUEUE = STRING.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<String>> STRING_PRIORITY_QUEUE = STRING.priority_queues();
    @NotNull VarSubType<ArrayDeque<String>> STRING_ARRAY_DEQUE = STRING.array_deques();
    @NotNull VarSubType<Stack<String>> STRING_STACK = STRING.stacks();
    @NotNull StringType.ArrayType STRING_ARRAY = STRING.arrays();

    // UUID
    @NotNull UUIDType UUID = new UUIDType();
    @NotNull VarSubType<ConcurrentSkipListSet<UUID>> UUID_CONCURRENT_SKIP_LIST_SET = UUID.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<UUID, Boolean>> UUID_CONCURRENT_SET = UUID.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<UUID>> UUID_LINKED_SET = UUID.linked_sets();
    @NotNull VarSubType<TreeSet<UUID>> UUID_TREE_SET = UUID.tree_sets();
    @NotNull VarSubType<Set<UUID>> UUID_SET = UUID.sets();
    @NotNull VarSubType<List<UUID>> UUID_LIST = UUID.lists();
    @NotNull VarSubType<PriorityBlockingQueue<UUID>> UUID_PRIORITY_BLOCKING_QUEUE = UUID.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<UUID>> UUID_PRIORITY_QUEUE = UUID.priority_queues();
    @NotNull VarSubType<ArrayDeque<UUID>> UUID_ARRAY_DEQUE = UUID.array_deques();
    @NotNull VarSubType<Stack<UUID>> UUID_STACK = UUID.stacks();
    @NotNull UUIDType.ArrayType UUID_ARRAY = UUID.arrays();

    // INTEGER
    @NotNull IntegerType INTEGER = new IntegerType();
    @NotNull VarSubType<ConcurrentSkipListSet<Integer>> INTEGER_CONCURRENT_SKIP_LIST_SET = INTEGER.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Integer, Boolean>> INTEGER_CONCURRENT_SET = INTEGER.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Integer>> INTEGER_LINKED_SET = INTEGER.linked_sets();
    @NotNull VarSubType<TreeSet<Integer>> INTEGER_TREE_SET = INTEGER.tree_sets();
    @NotNull VarSubType<Set<Integer>> INTEGER_SET = INTEGER.sets();
    @NotNull VarSubType<List<Integer>> INTEGER_LIST = INTEGER.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Integer>> INTEGER_PRIORITY_BLOCKING_QUEUE = INTEGER.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Integer>> INTEGER_PRIORITY_QUEUE = INTEGER.priority_queues();
    @NotNull VarSubType<ArrayDeque<Integer>> INTEGER_ARRAY_DEQUE = INTEGER.array_deques();
    @NotNull VarSubType<Stack<Integer>> INTEGER_STACK = INTEGER.stacks();
    @NotNull IntegerType.ArrayType INTEGER_ARRAY = INTEGER.arrays();

    // INTARRAY
    @NotNull IntArrayType INTARRAY = new IntArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<int[]>> INTARRAY_CONCURRENT_SKIP_LIST_SET = INTARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<int[], Boolean>> INTARRAY_CONCURRENT_SET = INTARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<int[]>> INTARRAY_LINKED_SET = INTARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<int[]>> INTARRAY_TREE_SET = INTARRAY.tree_sets();
    @NotNull VarSubType<Set<int[]>> INTARRAY_SET = INTARRAY.sets();
    @NotNull VarSubType<List<int[]>> INTARRAY_LIST = INTARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<int[]>> INTARRAY_PRIORITY_BLOCKING_QUEUE = INTARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<int[]>> INTARRAY_PRIORITY_QUEUE = INTARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<int[]>> INTARRAY_ARRAY_DEQUE = INTARRAY.array_deques();
    @NotNull VarSubType<Stack<int[]>> INTARRAY_STACK = INTARRAY.stacks();
    @NotNull IntArrayType.ArrayType INTARRAY_ARRAY = INTARRAY.arrays();

    // LONG
    @NotNull LongType LONG = new LongType();
    @NotNull VarSubType<ConcurrentSkipListSet<Long>> LONG_CONCURRENT_SKIP_LIST_SET = LONG.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Long, Boolean>> LONG_CONCURRENT_SET = LONG.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Long>> LONG_LINKED_SET = LONG.linked_sets();
    @NotNull VarSubType<TreeSet<Long>> LONG_TREE_SET = LONG.tree_sets();
    @NotNull VarSubType<Set<Long>> LONG_SET = LONG.sets();
    @NotNull VarSubType<List<Long>> LONG_LIST = LONG.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Long>> LONG_PRIORITY_BLOCKING_QUEUE = LONG.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Long>> LONG_PRIORITY_QUEUE = LONG.priority_queues();
    @NotNull VarSubType<ArrayDeque<Long>> LONG_ARRAY_DEQUE = LONG.array_deques();
    @NotNull VarSubType<Stack<Long>> LONG_STACK = LONG.stacks();
    @NotNull LongType.ArrayType LONG_ARRAY = LONG.arrays();

    // LONGARRAY
    @NotNull LongArrayType LONGARRAY = new LongArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<long[]>> LONGARRAY_CONCURRENT_SKIP_LIST_SET = LONGARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<long[], Boolean>> LONGARRAY_CONCURRENT_SET = LONGARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<long[]>> LONGARRAY_LINKED_SET = LONGARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<long[]>> LONGARRAY_TREE_SET = LONGARRAY.tree_sets();
    @NotNull VarSubType<Set<long[]>> LONGARRAY_SET = LONGARRAY.sets();
    @NotNull VarSubType<List<long[]>> LONGARRAY_LIST = LONGARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<long[]>> LONGARRAY_PRIORITY_BLOCKING_QUEUE = LONGARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<long[]>> LONGARRAY_PRIORITY_QUEUE = LONGARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<long[]>> LONGARRAY_ARRAY_DEQUE = LONGARRAY.array_deques();
    @NotNull VarSubType<Stack<long[]>> LONGARRAY_STACK = LONGARRAY.stacks();
    @NotNull LongArrayType.ArrayType LONGARRAY_ARRAY = LONGARRAY.arrays();

    // SHORT
    @NotNull ShortType SHORT = new ShortType();
    @NotNull VarSubType<ConcurrentSkipListSet<Short>> SHORT_CONCURRENT_SKIP_LIST_SET = SHORT.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Short, Boolean>> SHORT_CONCURRENT_SET = SHORT.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Short>> SHORT_LINKED_SET = SHORT.linked_sets();
    @NotNull VarSubType<TreeSet<Short>> SHORT_TREE_SET = SHORT.tree_sets();
    @NotNull VarSubType<Set<Short>> SHORT_SET = SHORT.sets();
    @NotNull VarSubType<List<Short>> SHORT_LIST = SHORT.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Short>> SHORT_PRIORITY_BLOCKING_QUEUE = SHORT.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Short>> SHORT_PRIORITY_QUEUE = SHORT.priority_queues();
    @NotNull VarSubType<ArrayDeque<Short>> SHORT_ARRAY_DEQUE = SHORT.array_deques();
    @NotNull VarSubType<Stack<Short>> SHORT_STACK = SHORT.stacks();
    @NotNull ShortType.ArrayType SHORT_ARRAY = SHORT.arrays();

    // SHORTARRAY
    @NotNull ShortArrayType SHORTARRAY = new ShortArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<short[]>> SHORTARRAY_CONCURRENT_SKIP_LIST_SET = SHORTARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<short[], Boolean>> SHORTARRAY_CONCURRENT_SET = SHORTARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<short[]>> SHORTARRAY_LINKED_SET = SHORTARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<short[]>> SHORTARRAY_TREE_SET = SHORTARRAY.tree_sets();
    @NotNull VarSubType<Set<short[]>> SHORTARRAY_SET = SHORTARRAY.sets();
    @NotNull VarSubType<List<short[]>> SHORTARRAY_LIST = SHORTARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<short[]>> SHORTARRAY_PRIORITY_BLOCKING_QUEUE = SHORTARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<short[]>> SHORTARRAY_PRIORITY_QUEUE = SHORTARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<short[]>> SHORTARRAY_ARRAY_DEQUE = SHORTARRAY.array_deques();
    @NotNull VarSubType<Stack<short[]>> SHORTARRAY_STACK = SHORTARRAY.stacks();
    @NotNull ShortArrayType.ArrayType SHORTARRAY_ARRAY = SHORTARRAY.arrays();

    // FLOAT
    @NotNull FloatType FLOAT = new FloatType();
    @NotNull VarSubType<ConcurrentSkipListSet<Float>> FLOAT_CONCURRENT_SKIP_LIST_SET = FLOAT.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Float, Boolean>> FLOAT_CONCURRENT_SET = FLOAT.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Float>> FLOAT_LINKED_SET = FLOAT.linked_sets();
    @NotNull VarSubType<TreeSet<Float>> FLOAT_TREE_SET = FLOAT.tree_sets();
    @NotNull VarSubType<Set<Float>> FLOAT_SET = FLOAT.sets();
    @NotNull VarSubType<List<Float>> FLOAT_LIST = FLOAT.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Float>> FLOAT_PRIORITY_BLOCKING_QUEUE = FLOAT.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Float>> FLOAT_PRIORITY_QUEUE = FLOAT.priority_queues();
    @NotNull VarSubType<ArrayDeque<Float>> FLOAT_ARRAY_DEQUE = FLOAT.array_deques();
    @NotNull VarSubType<Stack<Float>> FLOAT_STACK = FLOAT.stacks();
    @NotNull FloatType.ArrayType FLOAT_ARRAY = FLOAT.arrays();

    // FLOATARRAY
    @NotNull FloatArrayType FLOATARRAY = new FloatArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<float[]>> FLOATARRAY_CONCURRENT_SKIP_LIST_SET = FLOATARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<float[], Boolean>> FLOATARRAY_CONCURRENT_SET = FLOATARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<float[]>> FLOATARRAY_LINKED_SET = FLOATARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<float[]>> FLOATARRAY_TREE_SET = FLOATARRAY.tree_sets();
    @NotNull VarSubType<Set<float[]>> FLOATARRAY_SET = FLOATARRAY.sets();
    @NotNull VarSubType<List<float[]>> FLOATARRAY_LIST = FLOATARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<float[]>> FLOATARRAY_PRIORITY_BLOCKING_QUEUE = FLOATARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<float[]>> FLOATARRAY_PRIORITY_QUEUE = FLOATARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<float[]>> FLOATARRAY_ARRAY_DEQUE = FLOATARRAY.array_deques();
    @NotNull VarSubType<Stack<float[]>> FLOATARRAY_STACK = FLOATARRAY.stacks();
    @NotNull FloatArrayType.ArrayType FLOATARRAY_ARRAY = FLOATARRAY.arrays();

    // DOUBLE
    @NotNull DoubleType DOUBLE = new DoubleType();
    @NotNull VarSubType<ConcurrentSkipListSet<Double>> DOUBLE_CONCURRENT_SKIP_LIST_SET = DOUBLE.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Double, Boolean>> DOUBLE_CONCURRENT_SET = DOUBLE.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Double>> DOUBLE_LINKED_SET = DOUBLE.linked_sets();
    @NotNull VarSubType<TreeSet<Double>> DOUBLE_TREE_SET = DOUBLE.tree_sets();
    @NotNull VarSubType<Set<Double>> DOUBLE_SET = DOUBLE.sets();
    @NotNull VarSubType<List<Double>> DOUBLE_LIST = DOUBLE.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Double>> DOUBLE_PRIORITY_BLOCKING_QUEUE = DOUBLE.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Double>> DOUBLE_PRIORITY_QUEUE = DOUBLE.priority_queues();
    @NotNull VarSubType<ArrayDeque<Double>> DOUBLE_ARRAY_DEQUE = DOUBLE.array_deques();
    @NotNull VarSubType<Stack<Double>> DOUBLE_STACK = DOUBLE.stacks();
    @NotNull DoubleType.ArrayType DOUBLE_ARRAY = DOUBLE.arrays();

    // DOUBLEARRAY
    @NotNull DoubleArrayType DOUBLEARRAY = new DoubleArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<double[]>> DOUBLEARRAY_CONCURRENT_SKIP_LIST_SET = DOUBLEARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<double[], Boolean>> DOUBLEARRAY_CONCURRENT_SET = DOUBLEARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<double[]>> DOUBLEARRAY_LINKED_SET = DOUBLEARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<double[]>> DOUBLEARRAY_TREE_SET = DOUBLEARRAY.tree_sets();
    @NotNull VarSubType<Set<double[]>> DOUBLEARRAY_SET = DOUBLEARRAY.sets();
    @NotNull VarSubType<List<double[]>> DOUBLEARRAY_LIST = DOUBLEARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<double[]>> DOUBLEARRAY_PRIORITY_BLOCKING_QUEUE = DOUBLEARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<double[]>> DOUBLEARRAY_PRIORITY_QUEUE = DOUBLEARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<double[]>> DOUBLEARRAY_ARRAY_DEQUE = DOUBLEARRAY.array_deques();
    @NotNull VarSubType<Stack<double[]>> DOUBLEARRAY_STACK = DOUBLEARRAY.stacks();
    @NotNull DoubleArrayType.ArrayType DOUBLEARRAY_ARRAY = DOUBLEARRAY.arrays();

    // CHARACTER
    @NotNull CharacterType CHARACTER = new CharacterType();
    @NotNull VarSubType<ConcurrentSkipListSet<Character>> CHARACTER_CONCURRENT_SKIP_LIST_SET = CHARACTER.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Character, Boolean>> CHARACTER_CONCURRENT_SET = CHARACTER.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Character>> CHARACTER_LINKED_SET = CHARACTER.linked_sets();
    @NotNull VarSubType<TreeSet<Character>> CHARACTER_TREE_SET = CHARACTER.tree_sets();
    @NotNull VarSubType<Set<Character>> CHARACTER_SET = CHARACTER.sets();
    @NotNull VarSubType<List<Character>> CHARACTER_LIST = CHARACTER.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Character>> CHARACTER_PRIORITY_BLOCKING_QUEUE = CHARACTER.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Character>> CHARACTER_PRIORITY_QUEUE = CHARACTER.priority_queues();
    @NotNull VarSubType<ArrayDeque<Character>> CHARACTER_ARRAY_DEQUE = CHARACTER.array_deques();
    @NotNull VarSubType<Stack<Character>> CHARACTER_STACK = CHARACTER.stacks();
    @NotNull CharacterType.ArrayType CHARACTER_ARRAY = CHARACTER.arrays();

    // CHARARRAY
    @NotNull CharArrayType CHARARRAY = new CharArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<char[]>> CHARARRAY_CONCURRENT_SKIP_LIST_SET = CHARARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<char[], Boolean>> CHARARRAY_CONCURRENT_SET = CHARARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<char[]>> CHARARRAY_LINKED_SET = CHARARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<char[]>> CHARARRAY_TREE_SET = CHARARRAY.tree_sets();
    @NotNull VarSubType<Set<char[]>> CHARARRAY_SET = CHARARRAY.sets();
    @NotNull VarSubType<List<char[]>> CHARARRAY_LIST = CHARARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<char[]>> CHARARRAY_PRIORITY_BLOCKING_QUEUE = CHARARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<char[]>> CHARARRAY_PRIORITY_QUEUE = CHARARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<char[]>> CHARARRAY_ARRAY_DEQUE = CHARARRAY.array_deques();
    @NotNull VarSubType<Stack<char[]>> CHARARRAY_STACK = CHARARRAY.stacks();
    @NotNull CharArrayType.ArrayType CHARARRAY_ARRAY = CHARARRAY.arrays();

    // BYTE
    @NotNull ByteType BYTE = new ByteType();
    @NotNull VarSubType<ConcurrentSkipListSet<Byte>> BYTE_CONCURRENT_SKIP_LIST_SET = BYTE.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Byte, Boolean>> BYTE_CONCURRENT_SET = BYTE.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Byte>> BYTE_LINKED_SET = BYTE.linked_sets();
    @NotNull VarSubType<TreeSet<Byte>> BYTE_TREE_SET = BYTE.tree_sets();
    @NotNull VarSubType<Set<Byte>> BYTE_SET = BYTE.sets();
    @NotNull VarSubType<List<Byte>> BYTE_LIST = BYTE.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Byte>> BYTE_PRIORITY_BLOCKING_QUEUE = BYTE.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Byte>> BYTE_PRIORITY_QUEUE = BYTE.priority_queues();
    @NotNull VarSubType<ArrayDeque<Byte>> BYTE_ARRAY_DEQUE = BYTE.array_deques();
    @NotNull VarSubType<Stack<Byte>> BYTE_STACK = BYTE.stacks();
    @NotNull ByteType.ArrayType BYTE_ARRAY = BYTE.arrays();

    // BYTEARRAY
    @NotNull ByteArrayType BYTEARRAY = new ByteArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<byte[]>> BYTEARRAY_CONCURRENT_SKIP_LIST_SET = BYTEARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<byte[], Boolean>> BYTEARRAY_CONCURRENT_SET = BYTEARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<byte[]>> BYTEARRAY_LINKED_SET = BYTEARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<byte[]>> BYTEARRAY_TREE_SET = BYTEARRAY.tree_sets();
    @NotNull VarSubType<Set<byte[]>> BYTEARRAY_SET = BYTEARRAY.sets();
    @NotNull VarSubType<List<byte[]>> BYTEARRAY_LIST = BYTEARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<byte[]>> BYTEARRAY_PRIORITY_BLOCKING_QUEUE = BYTEARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<byte[]>> BYTEARRAY_PRIORITY_QUEUE = BYTEARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<byte[]>> BYTEARRAY_ARRAY_DEQUE = BYTEARRAY.array_deques();
    @NotNull VarSubType<Stack<byte[]>> BYTEARRAY_STACK = BYTEARRAY.stacks();
    @NotNull ByteArrayType.ArrayType BYTEARRAY_ARRAY = BYTEARRAY.arrays();

    // PATH
    @NotNull PathType PATH = new PathType();
    @NotNull VarSubType<ConcurrentSkipListSet<Path>> PATH_CONCURRENT_SKIP_LIST_SET = PATH.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Path, Boolean>> PATH_CONCURRENT_SET = PATH.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Path>> PATH_LINKED_SET = PATH.linked_sets();
    @NotNull VarSubType<TreeSet<Path>> PATH_TREE_SET = PATH.tree_sets();
    @NotNull VarSubType<Set<Path>> PATH_SET = PATH.sets();
    @NotNull VarSubType<List<Path>> PATH_LIST = PATH.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Path>> PATH_PRIORITY_BLOCKING_QUEUE = PATH.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Path>> PATH_PRIORITY_QUEUE = PATH.priority_queues();
    @NotNull VarSubType<ArrayDeque<Path>> PATH_ARRAY_DEQUE = PATH.array_deques();
    @NotNull VarSubType<Stack<Path>> PATH_STACK = PATH.stacks();
    @NotNull PathType.ArrayType PATH_ARRAY = PATH.arrays();

    // BOOLEAN
    @NotNull BooleanType BOOLEAN = new BooleanType();
    @NotNull VarSubType<ConcurrentSkipListSet<Boolean>> BOOLEAN_CONCURRENT_SKIP_LIST_SET = BOOLEAN.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Boolean, Boolean>> BOOLEAN_CONCURRENT_SET = BOOLEAN.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Boolean>> BOOLEAN_LINKED_SET = BOOLEAN.linked_sets();
    @NotNull VarSubType<TreeSet<Boolean>> BOOLEAN_TREE_SET = BOOLEAN.tree_sets();
    @NotNull VarSubType<Set<Boolean>> BOOLEAN_SET = BOOLEAN.sets();
    @NotNull VarSubType<List<Boolean>> BOOLEAN_LIST = BOOLEAN.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Boolean>> BOOLEAN_PRIORITY_BLOCKING_QUEUE = BOOLEAN.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Boolean>> BOOLEAN_PRIORITY_QUEUE = BOOLEAN.priority_queues();
    @NotNull VarSubType<ArrayDeque<Boolean>> BOOLEAN_ARRAY_DEQUE = BOOLEAN.array_deques();
    @NotNull VarSubType<Stack<Boolean>> BOOLEAN_STACK = BOOLEAN.stacks();
    @NotNull BooleanType.ArrayType BOOLEAN_ARRAY = BOOLEAN.arrays();

    // BOOLEANARRAY
    @NotNull BooleanArrayType BOOLEANARRAY = new BooleanArrayType();
    @NotNull VarSubType<ConcurrentSkipListSet<boolean[]>> BOOLEANARRAY_CONCURRENT_SKIP_LIST_SET = BOOLEANARRAY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<boolean[], Boolean>> BOOLEANARRAY_CONCURRENT_SET = BOOLEANARRAY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<boolean[]>> BOOLEANARRAY_LINKED_SET = BOOLEANARRAY.linked_sets();
    @NotNull VarSubType<TreeSet<boolean[]>> BOOLEANARRAY_TREE_SET = BOOLEANARRAY.tree_sets();
    @NotNull VarSubType<Set<boolean[]>> BOOLEANARRAY_SET = BOOLEANARRAY.sets();
    @NotNull VarSubType<List<boolean[]>> BOOLEANARRAY_LIST = BOOLEANARRAY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<boolean[]>> BOOLEANARRAY_PRIORITY_BLOCKING_QUEUE = BOOLEANARRAY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<boolean[]>> BOOLEANARRAY_PRIORITY_QUEUE = BOOLEANARRAY.priority_queues();
    @NotNull VarSubType<ArrayDeque<boolean[]>> BOOLEANARRAY_ARRAY_DEQUE = BOOLEANARRAY.array_deques();
    @NotNull VarSubType<Stack<boolean[]>> BOOLEANARRAY_STACK = BOOLEANARRAY.stacks();
    @NotNull BooleanArrayType.ArrayType BOOLEANARRAY_ARRAY = BOOLEANARRAY.arrays();

    // DATE
    @NotNull DateType DATE = new DateType();
    @NotNull VarSubType<ConcurrentSkipListSet<Date>> DATE_CONCURRENT_SKIP_LIST_SET = DATE.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Date, Boolean>> DATE_CONCURRENT_SET = DATE.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Date>> DATE_LINKED_SET = DATE.linked_sets();
    @NotNull VarSubType<TreeSet<Date>> DATE_TREE_SET = DATE.tree_sets();
    @NotNull VarSubType<Set<Date>> DATE_SET = DATE.sets();
    @NotNull VarSubType<List<Date>> DATE_LIST = DATE.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Date>> DATE_PRIORITY_BLOCKING_QUEUE = DATE.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Date>> DATE_PRIORITY_QUEUE = DATE.priority_queues();
    @NotNull VarSubType<ArrayDeque<Date>> DATE_ARRAY_DEQUE = DATE.array_deques();
    @NotNull VarSubType<Stack<Date>> DATE_STACK = DATE.stacks();
    @NotNull DateType.ArrayType DATE_ARRAY = DATE.arrays();

    // INSTANT
    @NotNull InstantType INSTANT = new InstantType();
    @NotNull VarSubType<ConcurrentSkipListSet<Instant>> INSTANT_CONCURRENT_SKIP_LIST_SET = INSTANT.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Instant, Boolean>> INSTANT_CONCURRENT_SET = INSTANT.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Instant>> INSTANT_LINKED_SET = INSTANT.linked_sets();
    @NotNull VarSubType<TreeSet<Instant>> INSTANT_TREE_SET = INSTANT.tree_sets();
    @NotNull VarSubType<Set<Instant>> INSTANT_SET = INSTANT.sets();
    @NotNull VarSubType<List<Instant>> INSTANT_LIST = INSTANT.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Instant>> INSTANT_PRIORITY_BLOCKING_QUEUE = INSTANT.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Instant>> INSTANT_PRIORITY_QUEUE = INSTANT.priority_queues();
    @NotNull VarSubType<ArrayDeque<Instant>> INSTANT_ARRAY_DEQUE = INSTANT.array_deques();
    @NotNull VarSubType<Stack<Instant>> INSTANT_STACK = INSTANT.stacks();
    @NotNull InstantType.ArrayType INSTANT_ARRAY = INSTANT.arrays();

    // LOCALDATE
    @NotNull LocalDateType LOCALDATE = new LocalDateType();
    @NotNull VarSubType<ConcurrentSkipListSet<LocalDate>> LOCALDATE_CONCURRENT_SKIP_LIST_SET = LOCALDATE.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<LocalDate, Boolean>> LOCALDATE_CONCURRENT_SET = LOCALDATE.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<LocalDate>> LOCALDATE_LINKED_SET = LOCALDATE.linked_sets();
    @NotNull VarSubType<TreeSet<LocalDate>> LOCALDATE_TREE_SET = LOCALDATE.tree_sets();
    @NotNull VarSubType<Set<LocalDate>> LOCALDATE_SET = LOCALDATE.sets();
    @NotNull VarSubType<List<LocalDate>> LOCALDATE_LIST = LOCALDATE.lists();
    @NotNull VarSubType<PriorityBlockingQueue<LocalDate>> LOCALDATE_PRIORITY_BLOCKING_QUEUE = LOCALDATE.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<LocalDate>> LOCALDATE_PRIORITY_QUEUE = LOCALDATE.priority_queues();
    @NotNull VarSubType<ArrayDeque<LocalDate>> LOCALDATE_ARRAY_DEQUE = LOCALDATE.array_deques();
    @NotNull VarSubType<Stack<LocalDate>> LOCALDATE_STACK = LOCALDATE.stacks();
    @NotNull LocalDateType.ArrayType LOCALDATE_ARRAY = LOCALDATE.arrays();

    // LOCALDATETIME
    @NotNull LocalDateTimeType LOCALDATETIME = new LocalDateTimeType();
    @NotNull VarSubType<ConcurrentSkipListSet<LocalDateTime>> LOCALDATETIME_CONCURRENT_SKIP_LIST_SET = LOCALDATETIME.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<LocalDateTime, Boolean>> LOCALDATETIME_CONCURRENT_SET = LOCALDATETIME.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<LocalDateTime>> LOCALDATETIME_LINKED_SET = LOCALDATETIME.linked_sets();
    @NotNull VarSubType<TreeSet<LocalDateTime>> LOCALDATETIME_TREE_SET = LOCALDATETIME.tree_sets();
    @NotNull VarSubType<Set<LocalDateTime>> LOCALDATETIME_SET = LOCALDATETIME.sets();
    @NotNull VarSubType<List<LocalDateTime>> LOCALDATETIME_LIST = LOCALDATETIME.lists();
    @NotNull VarSubType<PriorityBlockingQueue<LocalDateTime>> LOCALDATETIME_PRIORITY_BLOCKING_QUEUE = LOCALDATETIME.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<LocalDateTime>> LOCALDATETIME_PRIORITY_QUEUE = LOCALDATETIME.priority_queues();
    @NotNull VarSubType<ArrayDeque<LocalDateTime>> LOCALDATETIME_ARRAY_DEQUE = LOCALDATETIME.array_deques();
    @NotNull VarSubType<Stack<LocalDateTime>> LOCALDATETIME_STACK = LOCALDATETIME.stacks();
    @NotNull LocalDateTimeType.ArrayType LOCALDATETIME_ARRAY = LOCALDATETIME.arrays();

    // PERIOD
    @NotNull PeriodType PERIOD = new PeriodType();
    @NotNull VarSubType<ConcurrentSkipListSet<Period>> PERIOD_CONCURRENT_SKIP_LIST_SET = PERIOD.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Period, Boolean>> PERIOD_CONCURRENT_SET = PERIOD.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Period>> PERIOD_LINKED_SET = PERIOD.linked_sets();
    @NotNull VarSubType<TreeSet<Period>> PERIOD_TREE_SET = PERIOD.tree_sets();
    @NotNull VarSubType<Set<Period>> PERIOD_SET = PERIOD.sets();
    @NotNull VarSubType<List<Period>> PERIOD_LIST = PERIOD.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Period>> PERIOD_PRIORITY_BLOCKING_QUEUE = PERIOD.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Period>> PERIOD_PRIORITY_QUEUE = PERIOD.priority_queues();
    @NotNull VarSubType<ArrayDeque<Period>> PERIOD_ARRAY_DEQUE = PERIOD.array_deques();
    @NotNull VarSubType<Stack<Period>> PERIOD_STACK = PERIOD.stacks();
    @NotNull PeriodType.ArrayType PERIOD_ARRAY = PERIOD.arrays();

    // DURATION
    @NotNull DurationType DURATION = new DurationType();
    @NotNull VarSubType<ConcurrentSkipListSet<Duration>> DURATION_CONCURRENT_SKIP_LIST_SET = DURATION.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Duration, Boolean>> DURATION_CONCURRENT_SET = DURATION.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Duration>> DURATION_LINKED_SET = DURATION.linked_sets();
    @NotNull VarSubType<TreeSet<Duration>> DURATION_TREE_SET = DURATION.tree_sets();
    @NotNull VarSubType<Set<Duration>> DURATION_SET = DURATION.sets();
    @NotNull VarSubType<List<Duration>> DURATION_LIST = DURATION.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Duration>> DURATION_PRIORITY_BLOCKING_QUEUE = DURATION.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Duration>> DURATION_PRIORITY_QUEUE = DURATION.priority_queues();
    @NotNull VarSubType<ArrayDeque<Duration>> DURATION_ARRAY_DEQUE = DURATION.array_deques();
    @NotNull VarSubType<Stack<Duration>> DURATION_STACK = DURATION.stacks();
    @NotNull DurationType.ArrayType DURATION_ARRAY = DURATION.arrays();

    // COMPONENT
    @NotNull ComponentType COMPONENT = new ComponentType();
    @NotNull VarSubType<ConcurrentSkipListSet<Component>> COMPONENT_CONCURRENT_SKIP_LIST_SET = COMPONENT.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Component, Boolean>> COMPONENT_CONCURRENT_SET = COMPONENT.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Component>> COMPONENT_LINKED_SET = COMPONENT.linked_sets();
    @NotNull VarSubType<TreeSet<Component>> COMPONENT_TREE_SET = COMPONENT.tree_sets();
    @NotNull VarSubType<Set<Component>> COMPONENT_SET = COMPONENT.sets();
    @NotNull VarSubType<List<Component>> COMPONENT_LIST = COMPONENT.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Component>> COMPONENT_PRIORITY_BLOCKING_QUEUE = COMPONENT.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Component>> COMPONENT_PRIORITY_QUEUE = COMPONENT.priority_queues();
    @NotNull VarSubType<ArrayDeque<Component>> COMPONENT_ARRAY_DEQUE = COMPONENT.array_deques();
    @NotNull VarSubType<Stack<Component>> COMPONENT_STACK = COMPONENT.stacks();
    @NotNull ComponentType.ArrayType COMPONENT_ARRAY = COMPONENT.arrays();

    // BIGINTEGER
    @NotNull BigIntegerType BIGINTEGER = new BigIntegerType();
    @NotNull VarSubType<ConcurrentSkipListSet<BigInteger>> BIGINTEGER_CONCURRENT_SKIP_LIST_SET = BIGINTEGER.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<BigInteger, Boolean>> BIGINTEGER_CONCURRENT_SET = BIGINTEGER.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<BigInteger>> BIGINTEGER_LINKED_SET = BIGINTEGER.linked_sets();
    @NotNull VarSubType<TreeSet<BigInteger>> BIGINTEGER_TREE_SET = BIGINTEGER.tree_sets();
    @NotNull VarSubType<Set<BigInteger>> BIGINTEGER_SET = BIGINTEGER.sets();
    @NotNull VarSubType<List<BigInteger>> BIGINTEGER_LIST = BIGINTEGER.lists();
    @NotNull VarSubType<PriorityBlockingQueue<BigInteger>> BIGINTEGER_PRIORITY_BLOCKING_QUEUE = BIGINTEGER.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<BigInteger>> BIGINTEGER_PRIORITY_QUEUE = BIGINTEGER.priority_queues();
    @NotNull VarSubType<ArrayDeque<BigInteger>> BIGINTEGER_ARRAY_DEQUE = BIGINTEGER.array_deques();
    @NotNull VarSubType<Stack<BigInteger>> BIGINTEGER_STACK = BIGINTEGER.stacks();
    @NotNull BigIntegerType.ArrayType BIGINTEGER_ARRAY = BIGINTEGER.arrays();

    // BIGDECIMAL
    @NotNull BigDecimalType BIGDECIMAL = new BigDecimalType();
    @NotNull VarSubType<ConcurrentSkipListSet<BigDecimal>> BIGDECIMAL_CONCURRENT_SKIP_LIST_SET = BIGDECIMAL.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<BigDecimal, Boolean>> BIGDECIMAL_CONCURRENT_SET = BIGDECIMAL.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<BigDecimal>> BIGDECIMAL_LINKED_SET = BIGDECIMAL.linked_sets();
    @NotNull VarSubType<TreeSet<BigDecimal>> BIGDECIMAL_TREE_SET = BIGDECIMAL.tree_sets();
    @NotNull VarSubType<Set<BigDecimal>> BIGDECIMAL_SET = BIGDECIMAL.sets();
    @NotNull VarSubType<List<BigDecimal>> BIGDECIMAL_LIST = BIGDECIMAL.lists();
    @NotNull VarSubType<PriorityBlockingQueue<BigDecimal>> BIGDECIMAL_PRIORITY_BLOCKING_QUEUE = BIGDECIMAL.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<BigDecimal>> BIGDECIMAL_PRIORITY_QUEUE = BIGDECIMAL.priority_queues();
    @NotNull VarSubType<ArrayDeque<BigDecimal>> BIGDECIMAL_ARRAY_DEQUE = BIGDECIMAL.array_deques();
    @NotNull VarSubType<Stack<BigDecimal>> BIGDECIMAL_STACK = BIGDECIMAL.stacks();
    @NotNull BigDecimalType.ArrayType BIGDECIMAL_ARRAY = BIGDECIMAL.arrays();

    // WORLD
    @NotNull WorldType WORLD = new WorldType();
    @NotNull VarSubType<ConcurrentSkipListSet<World>> WORLD_CONCURRENT_SKIP_LIST_SET = WORLD.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<World, Boolean>> WORLD_CONCURRENT_SET = WORLD.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<World>> WORLD_LINKED_SET = WORLD.linked_sets();
    @NotNull VarSubType<TreeSet<World>> WORLD_TREE_SET = WORLD.tree_sets();
    @NotNull VarSubType<Set<World>> WORLD_SET = WORLD.sets();
    @NotNull VarSubType<List<World>> WORLD_LIST = WORLD.lists();
    @NotNull VarSubType<PriorityBlockingQueue<World>> WORLD_PRIORITY_BLOCKING_QUEUE = WORLD.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<World>> WORLD_PRIORITY_QUEUE = WORLD.priority_queues();
    @NotNull VarSubType<ArrayDeque<World>> WORLD_ARRAY_DEQUE = WORLD.array_deques();
    @NotNull VarSubType<Stack<World>> WORLD_STACK = WORLD.stacks();
    @NotNull WorldType.ArrayType WORLD_ARRAY = WORLD.arrays();

    // BLOCKDATA
    @NotNull BlockDataType BLOCKDATA = new BlockDataType();
    @NotNull VarSubType<ConcurrentSkipListSet<BlockData>> BLOCKDATA_CONCURRENT_SKIP_LIST_SET = BLOCKDATA.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<BlockData, Boolean>> BLOCKDATA_CONCURRENT_SET = BLOCKDATA.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<BlockData>> BLOCKDATA_LINKED_SET = BLOCKDATA.linked_sets();
    @NotNull VarSubType<TreeSet<BlockData>> BLOCKDATA_TREE_SET = BLOCKDATA.tree_sets();
    @NotNull VarSubType<Set<BlockData>> BLOCKDATA_SET = BLOCKDATA.sets();
    @NotNull VarSubType<List<BlockData>> BLOCKDATA_LIST = BLOCKDATA.lists();
    @NotNull VarSubType<PriorityBlockingQueue<BlockData>> BLOCKDATA_PRIORITY_BLOCKING_QUEUE = BLOCKDATA.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<BlockData>> BLOCKDATA_PRIORITY_QUEUE = BLOCKDATA.priority_queues();
    @NotNull VarSubType<ArrayDeque<BlockData>> BLOCKDATA_ARRAY_DEQUE = BLOCKDATA.array_deques();
    @NotNull VarSubType<Stack<BlockData>> BLOCKDATA_STACK = BLOCKDATA.stacks();
    @NotNull BlockDataType.ArrayType BLOCKDATA_ARRAY = BLOCKDATA.arrays();

    // CHUNK
    @NotNull ChunkType CHUNK = new ChunkType();
    @NotNull VarSubType<ConcurrentSkipListSet<Chunk>> CHUNK_CONCURRENT_SKIP_LIST_SET = CHUNK.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Chunk, Boolean>> CHUNK_CONCURRENT_SET = CHUNK.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Chunk>> CHUNK_LINKED_SET = CHUNK.linked_sets();
    @NotNull VarSubType<TreeSet<Chunk>> CHUNK_TREE_SET = CHUNK.tree_sets();
    @NotNull VarSubType<Set<Chunk>> CHUNK_SET = CHUNK.sets();
    @NotNull VarSubType<List<Chunk>> CHUNK_LIST = CHUNK.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Chunk>> CHUNK_PRIORITY_BLOCKING_QUEUE = CHUNK.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Chunk>> CHUNK_PRIORITY_QUEUE = CHUNK.priority_queues();
    @NotNull VarSubType<ArrayDeque<Chunk>> CHUNK_ARRAY_DEQUE = CHUNK.array_deques();
    @NotNull VarSubType<Stack<Chunk>> CHUNK_STACK = CHUNK.stacks();
    @NotNull ChunkType.ArrayType CHUNK_ARRAY = CHUNK.arrays();

    // DEPRECATEDMATERIAL
    @NotNull DeprecatedMaterialType DEPRECATEDMATERIAL = new DeprecatedMaterialType();
    @NotNull VarSubType<ConcurrentSkipListSet<Material>> DEPRECATEDMATERIAL_CONCURRENT_SKIP_LIST_SET = DEPRECATEDMATERIAL.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Material, Boolean>> DEPRECATEDMATERIAL_CONCURRENT_SET = DEPRECATEDMATERIAL.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Material>> DEPRECATEDMATERIAL_LINKED_SET = DEPRECATEDMATERIAL.linked_sets();
    @NotNull VarSubType<TreeSet<Material>> DEPRECATEDMATERIAL_TREE_SET = DEPRECATEDMATERIAL.tree_sets();
    @NotNull VarSubType<Set<Material>> DEPRECATEDMATERIAL_SET = DEPRECATEDMATERIAL.sets();
    @NotNull VarSubType<List<Material>> DEPRECATEDMATERIAL_LIST = DEPRECATEDMATERIAL.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Material>> DEPRECATEDMATERIAL_PRIORITY_BLOCKING_QUEUE = DEPRECATEDMATERIAL.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Material>> DEPRECATEDMATERIAL_PRIORITY_QUEUE = DEPRECATEDMATERIAL.priority_queues();
    @NotNull VarSubType<ArrayDeque<Material>> DEPRECATEDMATERIAL_ARRAY_DEQUE = DEPRECATEDMATERIAL.array_deques();
    @NotNull VarSubType<Stack<Material>> DEPRECATEDMATERIAL_STACK = DEPRECATEDMATERIAL.stacks();
    @NotNull DeprecatedMaterialType.ArrayType DEPRECATEDMATERIAL_ARRAY = DEPRECATEDMATERIAL.arrays();

    // MATERIAL
    @NotNull MaterialType MATERIAL = new MaterialType();
    @NotNull VarSubType<ConcurrentSkipListSet<Material>> MATERIAL_CONCURRENT_SKIP_LIST_SET = MATERIAL.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Material, Boolean>> MATERIAL_CONCURRENT_SET = MATERIAL.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Material>> MATERIAL_LINKED_SET = MATERIAL.linked_sets();
    @NotNull VarSubType<TreeSet<Material>> MATERIAL_TREE_SET = MATERIAL.tree_sets();
    @NotNull VarSubType<Set<Material>> MATERIAL_SET = MATERIAL.sets();
    @NotNull VarSubType<List<Material>> MATERIAL_LIST = MATERIAL.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Material>> MATERIAL_PRIORITY_BLOCKING_QUEUE = MATERIAL.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Material>> MATERIAL_PRIORITY_QUEUE = MATERIAL.priority_queues();
    @NotNull VarSubType<ArrayDeque<Material>> MATERIAL_ARRAY_DEQUE = MATERIAL.array_deques();
    @NotNull VarSubType<Stack<Material>> MATERIAL_STACK = MATERIAL.stacks();
    @NotNull MaterialType.ArrayType MATERIAL_ARRAY = MATERIAL.arrays();

    // LOCATION
    @NotNull LocationType LOCATION = new LocationType();
    @NotNull VarSubType<ConcurrentSkipListSet<Location>> LOCATION_CONCURRENT_SKIP_LIST_SET = LOCATION.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Location, Boolean>> LOCATION_CONCURRENT_SET = LOCATION.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Location>> LOCATION_LINKED_SET = LOCATION.linked_sets();
    @NotNull VarSubType<TreeSet<Location>> LOCATION_TREE_SET = LOCATION.tree_sets();
    @NotNull VarSubType<Set<Location>> LOCATION_SET = LOCATION.sets();
    @NotNull VarSubType<List<Location>> LOCATION_LIST = LOCATION.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Location>> LOCATION_PRIORITY_BLOCKING_QUEUE = LOCATION.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Location>> LOCATION_PRIORITY_QUEUE = LOCATION.priority_queues();
    @NotNull VarSubType<ArrayDeque<Location>> LOCATION_ARRAY_DEQUE = LOCATION.array_deques();
    @NotNull VarSubType<Stack<Location>> LOCATION_STACK = LOCATION.stacks();
    @NotNull LocationType.ArrayType LOCATION_ARRAY = LOCATION.arrays();

    // ITEMSTACK
    @NotNull ItemStackType ITEMSTACK = new ItemStackType();
    @NotNull VarSubType<ConcurrentSkipListSet<ItemStack>> ITEMSTACK_CONCURRENT_SKIP_LIST_SET = ITEMSTACK.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<ItemStack, Boolean>> ITEMSTACK_CONCURRENT_SET = ITEMSTACK.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<ItemStack>> ITEMSTACK_LINKED_SET = ITEMSTACK.linked_sets();
    @NotNull VarSubType<TreeSet<ItemStack>> ITEMSTACK_TREE_SET = ITEMSTACK.tree_sets();
    @NotNull VarSubType<Set<ItemStack>> ITEMSTACK_SET = ITEMSTACK.sets();
    @NotNull VarSubType<List<ItemStack>> ITEMSTACK_LIST = ITEMSTACK.lists();
    @NotNull VarSubType<PriorityBlockingQueue<ItemStack>> ITEMSTACK_PRIORITY_BLOCKING_QUEUE = ITEMSTACK.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<ItemStack>> ITEMSTACK_PRIORITY_QUEUE = ITEMSTACK.priority_queues();
    @NotNull VarSubType<ArrayDeque<ItemStack>> ITEMSTACK_ARRAY_DEQUE = ITEMSTACK.array_deques();
    @NotNull VarSubType<Stack<ItemStack>> ITEMSTACK_STACK = ITEMSTACK.stacks();
    @NotNull ItemStackType.ArrayType ITEMSTACK_ARRAY = ITEMSTACK.arrays();

    // GAMEMODE
    @NotNull GameModeType GAMEMODE = new GameModeType();
    @NotNull VarSubType<ConcurrentSkipListSet<GameMode>> GAMEMODE_CONCURRENT_SKIP_LIST_SET = GAMEMODE.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<GameMode, Boolean>> GAMEMODE_CONCURRENT_SET = GAMEMODE.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<GameMode>> GAMEMODE_LINKED_SET = GAMEMODE.linked_sets();
    @NotNull VarSubType<TreeSet<GameMode>> GAMEMODE_TREE_SET = GAMEMODE.tree_sets();
    @NotNull VarSubType<Set<GameMode>> GAMEMODE_SET = GAMEMODE.sets();
    @NotNull VarSubType<List<GameMode>> GAMEMODE_LIST = GAMEMODE.lists();
    @NotNull VarSubType<PriorityBlockingQueue<GameMode>> GAMEMODE_PRIORITY_BLOCKING_QUEUE = GAMEMODE.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<GameMode>> GAMEMODE_PRIORITY_QUEUE = GAMEMODE.priority_queues();
    @NotNull VarSubType<ArrayDeque<GameMode>> GAMEMODE_ARRAY_DEQUE = GAMEMODE.array_deques();
    @NotNull VarSubType<Stack<GameMode>> GAMEMODE_STACK = GAMEMODE.stacks();
    @NotNull GameModeType.ArrayType GAMEMODE_ARRAY = GAMEMODE.arrays();

    // INVENTORY
    @NotNull InventoryType INVENTORY = new InventoryType();
    @NotNull VarSubType<ConcurrentSkipListSet<Inventory>> INVENTORY_CONCURRENT_SKIP_LIST_SET = INVENTORY.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<Inventory, Boolean>> INVENTORY_CONCURRENT_SET = INVENTORY.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<Inventory>> INVENTORY_LINKED_SET = INVENTORY.linked_sets();
    @NotNull VarSubType<TreeSet<Inventory>> INVENTORY_TREE_SET = INVENTORY.tree_sets();
    @NotNull VarSubType<Set<Inventory>> INVENTORY_SET = INVENTORY.sets();
    @NotNull VarSubType<List<Inventory>> INVENTORY_LIST = INVENTORY.lists();
    @NotNull VarSubType<PriorityBlockingQueue<Inventory>> INVENTORY_PRIORITY_BLOCKING_QUEUE = INVENTORY.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<Inventory>> INVENTORY_PRIORITY_QUEUE = INVENTORY.priority_queues();
    @NotNull VarSubType<ArrayDeque<Inventory>> INVENTORY_ARRAY_DEQUE = INVENTORY.array_deques();
    @NotNull VarSubType<Stack<Inventory>> INVENTORY_STACK = INVENTORY.stacks();
    @NotNull InventoryType.ArrayType INVENTORY_ARRAY = INVENTORY.arrays();

    // VECTOR
    @NotNull VectorType VECTOR = new VectorType();
    @NotNull VarSubType<ConcurrentSkipListSet<org.bukkit.util.Vector>> VECTOR_CONCURRENT_SKIP_LIST_SET = VECTOR.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<org.bukkit.util.Vector, Boolean>> VECTOR_CONCURRENT_SET = VECTOR.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<org.bukkit.util.Vector>> VECTOR_LINKED_SET = VECTOR.linked_sets();
    @NotNull VarSubType<TreeSet<org.bukkit.util.Vector>> VECTOR_TREE_SET = VECTOR.tree_sets();
    @NotNull VarSubType<Set<org.bukkit.util.Vector>> VECTOR_SET = VECTOR.sets();
    @NotNull VarSubType<List<org.bukkit.util.Vector>> VECTOR_LIST = VECTOR.lists();
    @NotNull VarSubType<PriorityBlockingQueue<org.bukkit.util.Vector>> VECTOR_PRIORITY_BLOCKING_QUEUE = VECTOR.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<org.bukkit.util.Vector>> VECTOR_PRIORITY_QUEUE = VECTOR.priority_queues();
    @NotNull VarSubType<ArrayDeque<org.bukkit.util.Vector>> VECTOR_ARRAY_DEQUE = VECTOR.array_deques();
    @NotNull VarSubType<Stack<org.bukkit.util.Vector>> VECTOR_STACK = VECTOR.stacks();
    @NotNull VectorType.ArrayType VECTOR_ARRAY = VECTOR.arrays();

    // BOUNDINGBOX
    @NotNull BoundingBoxType BOUNDINGBOX = new BoundingBoxType();
    @NotNull VarSubType<ConcurrentSkipListSet<BoundingBox>> BOUNDINGBOX_CONCURRENT_SKIP_LIST_SET = BOUNDINGBOX.concurrent_skip_list_sets();
    @NotNull VarSubType<ConcurrentHashMap.KeySetView<BoundingBox, Boolean>> BOUNDINGBOX_CONCURRENT_SET = BOUNDINGBOX.concurrent_sets();
    @NotNull VarSubType<LinkedHashSet<BoundingBox>> BOUNDINGBOX_LINKED_SET = BOUNDINGBOX.linked_sets();
    @NotNull VarSubType<TreeSet<BoundingBox>> BOUNDINGBOX_TREE_SET = BOUNDINGBOX.tree_sets();
    @NotNull VarSubType<Set<BoundingBox>> BOUNDINGBOX_SET = BOUNDINGBOX.sets();
    @NotNull VarSubType<List<BoundingBox>> BOUNDINGBOX_LIST = BOUNDINGBOX.lists();
    @NotNull VarSubType<PriorityBlockingQueue<BoundingBox>> BOUNDINGBOX_PRIORITY_BLOCKING_QUEUE = BOUNDINGBOX.priority_blocking_queues();
    @NotNull VarSubType<PriorityQueue<BoundingBox>> BOUNDINGBOX_PRIORITY_QUEUE = BOUNDINGBOX.priority_queues();
    @NotNull VarSubType<ArrayDeque<BoundingBox>> BOUNDINGBOX_ARRAY_DEQUE = BOUNDINGBOX.array_deques();
    @NotNull VarSubType<Stack<BoundingBox>> BOUNDINGBOX_STACK = BOUNDINGBOX.stacks();
    @NotNull BoundingBoxType.ArrayType BOUNDINGBOX_ARRAY = BOUNDINGBOX.arrays();
}
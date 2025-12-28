package fr.nexus.api.var.types;

import fr.nexus.Core;
import fr.nexus.api.var.types.parents.normal.adventure.ComponentType;
import fr.nexus.api.var.types.parents.normal.big.BigDecimalType;
import fr.nexus.api.var.types.parents.normal.big.BigIntegerType;
import fr.nexus.api.var.types.parents.normal.bukkit.*;
import fr.nexus.api.var.types.parents.normal.bukkit.InventoryType;
import fr.nexus.api.var.types.parents.normal.bukkit.ItemStackType;
import fr.nexus.api.var.types.parents.normal.java.*;
import fr.nexus.api.var.types.parents.normal.java.date.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface VarTypes{
    static void generateFileTypes() throws IOException {
        // Liste de tous tes types
        List<String> types = Arrays.asList(
                "StringType", "UUIDType", "IntegerType", "IntArrayType",
                "LongType", "LongArrayType", "ShortType", "ShortArrayType",
                "FloatType", "FloatArrayType", "DoubleType", "DoubleArrayType",
                "CharacterType", "CharArrayType", "ByteType", "ByteArrayType",
                "PathType", "BooleanType", "BooleanArrayType",
                "DateType", "InstantType", "LocalDateType", "LocalDateTimeType",
                "PeriodType", "DurationType",
                "ComponentType",
                "BigIntegerType", "BigDecimalType",
                "WorldType", "BlockDataType", "ChunkType", "DeprecatedMaterialType",
                "MaterialType", "LocationType", "ItemStackType", "GameModeType",
                "InventoryType", "VectorType", "BoundingBoxType"
        );

        File genFolder = new File(Core.getInstance().getDataFolder(), "generated"); // Nouveau dossier "generated"
        if (!genFolder.exists()) genFolder.mkdirs(); // Crée le dossier si inexistant

        File outputFile = new File(genFolder, "VarTypes.java");

        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String type : types) {
                String simpleName = type.replace("Type", ""); // pour le nom de la variable
                String varName = simpleName.toUpperCase();

                writer.write("    // " + varName + "\n");
                writer.write("    @NotNull " + type + " " + varName + " = new " + type + "();\n");

                writer.write("    @NotNull " + type + ".ConcurrentSkipListSetType " + varName + "_CONCURRENT_SKIP_LIST_SET = " + varName + ".concurrent_skip_list_sets();\n");
                writer.write("    @NotNull " + type + ".ConcurrentSetType " + varName + "_CONCURRENT_SET = " + varName + ".concurrent_sets();\n");
                writer.write("    @NotNull " + type + ".LinkedSetType " + varName + "_LINKED_SET = " + varName + ".linked_sets();\n");
                writer.write("    @NotNull " + type + ".TreeSetType " + varName + "_TREE_SET = " + varName + ".tree_sets();\n");
                writer.write("    @NotNull " + type + ".SetType " + varName + "_SET = " + varName + ".sets();\n");

                writer.write("    @NotNull " + type + ".ListType " + varName + "_LIST = " + varName + ".lists();\n");

                writer.write("    @NotNull " + type + ".ArrayType " + varName + "_ARRAY = " + varName + ".arrays();\n");

                writer.write("    @NotNull " + type + ".PriorityBlockingQueueType " + varName + "_PRIORITY_BLOCKING_QUEUE = " + varName + ".priority_blocking_queues();\n");
                writer.write("    @NotNull " + type + ".PriorityQueueType " + varName + "_PRIORITY_QUEUE = " + varName + ".priority_queues();\n");

                writer.write("    @NotNull " + type + ".ArrayDequeType " + varName + "_ARRAY_DEQUE = " + varName + ".array_deques();\n");
                writer.write("    @NotNull " + type + ".StackType " + varName + "_STACK = " + varName + ".stacks();\n\n");
            }
        }

        System.out.println("VarTypes.java généré avec succès dans " + outputFile.getAbsolutePath());
    }

    // STRING
    @NotNull StringType STRING = new StringType();
    @NotNull StringType.ConcurrentSkipListSetType STRING_CONCURRENT_SKIP_LIST_SET = STRING.concurrent_skip_list_sets();
    @NotNull StringType.ConcurrentSetType STRING_CONCURRENT_SET = STRING.concurrent_sets();
    @NotNull StringType.LinkedSetType STRING_LINKED_SET = STRING.linked_sets();
    @NotNull StringType.TreeSetType STRING_TREE_SET = STRING.tree_sets();
    @NotNull StringType.SetType STRING_SET = STRING.sets();
    @NotNull StringType.ListType STRING_LIST = STRING.lists();
    @NotNull StringType.ArrayType STRING_ARRAY = STRING.arrays();
    @NotNull StringType.PriorityBlockingQueueType STRING_PRIORITY_BLOCKING_QUEUE = STRING.priority_blocking_queues();
    @NotNull StringType.PriorityQueueType STRING_PRIORITY_QUEUE = STRING.priority_queues();
    @NotNull StringType.ArrayDequeType STRING_ARRAY_DEQUE = STRING.array_deques();
    @NotNull StringType.StackType STRING_STACK = STRING.stacks();

    // UUID
    @NotNull UUIDType UUID = new UUIDType();
    @NotNull UUIDType.ConcurrentSkipListSetType UUID_CONCURRENT_SKIP_LIST_SET = UUID.concurrent_skip_list_sets();
    @NotNull UUIDType.ConcurrentSetType UUID_CONCURRENT_SET = UUID.concurrent_sets();
    @NotNull UUIDType.LinkedSetType UUID_LINKED_SET = UUID.linked_sets();
    @NotNull UUIDType.TreeSetType UUID_TREE_SET = UUID.tree_sets();
    @NotNull UUIDType.SetType UUID_SET = UUID.sets();
    @NotNull UUIDType.ListType UUID_LIST = UUID.lists();
    @NotNull UUIDType.ArrayType UUID_ARRAY = UUID.arrays();
    @NotNull UUIDType.PriorityBlockingQueueType UUID_PRIORITY_BLOCKING_QUEUE = UUID.priority_blocking_queues();
    @NotNull UUIDType.PriorityQueueType UUID_PRIORITY_QUEUE = UUID.priority_queues();
    @NotNull UUIDType.ArrayDequeType UUID_ARRAY_DEQUE = UUID.array_deques();
    @NotNull UUIDType.StackType UUID_STACK = UUID.stacks();

    // INTEGER
    @NotNull IntegerType INTEGER = new IntegerType();
    @NotNull IntegerType.ConcurrentSkipListSetType INTEGER_CONCURRENT_SKIP_LIST_SET = INTEGER.concurrent_skip_list_sets();
    @NotNull IntegerType.ConcurrentSetType INTEGER_CONCURRENT_SET = INTEGER.concurrent_sets();
    @NotNull IntegerType.LinkedSetType INTEGER_LINKED_SET = INTEGER.linked_sets();
    @NotNull IntegerType.TreeSetType INTEGER_TREE_SET = INTEGER.tree_sets();
    @NotNull IntegerType.SetType INTEGER_SET = INTEGER.sets();
    @NotNull IntegerType.ListType INTEGER_LIST = INTEGER.lists();
    @NotNull IntegerType.ArrayType INTEGER_ARRAY = INTEGER.arrays();
    @NotNull IntegerType.PriorityBlockingQueueType INTEGER_PRIORITY_BLOCKING_QUEUE = INTEGER.priority_blocking_queues();
    @NotNull IntegerType.PriorityQueueType INTEGER_PRIORITY_QUEUE = INTEGER.priority_queues();
    @NotNull IntegerType.ArrayDequeType INTEGER_ARRAY_DEQUE = INTEGER.array_deques();
    @NotNull IntegerType.StackType INTEGER_STACK = INTEGER.stacks();

    // INTARRAY
    @NotNull IntArrayType INTARRAY = new IntArrayType();
    @NotNull IntArrayType.ConcurrentSkipListSetType INTARRAY_CONCURRENT_SKIP_LIST_SET = INTARRAY.concurrent_skip_list_sets();
    @NotNull IntArrayType.ConcurrentSetType INTARRAY_CONCURRENT_SET = INTARRAY.concurrent_sets();
    @NotNull IntArrayType.LinkedSetType INTARRAY_LINKED_SET = INTARRAY.linked_sets();
    @NotNull IntArrayType.TreeSetType INTARRAY_TREE_SET = INTARRAY.tree_sets();
    @NotNull IntArrayType.SetType INTARRAY_SET = INTARRAY.sets();
    @NotNull IntArrayType.ListType INTARRAY_LIST = INTARRAY.lists();
    @NotNull IntArrayType.ArrayType INTARRAY_ARRAY = INTARRAY.arrays();
    @NotNull IntArrayType.PriorityBlockingQueueType INTARRAY_PRIORITY_BLOCKING_QUEUE = INTARRAY.priority_blocking_queues();
    @NotNull IntArrayType.PriorityQueueType INTARRAY_PRIORITY_QUEUE = INTARRAY.priority_queues();
    @NotNull IntArrayType.ArrayDequeType INTARRAY_ARRAY_DEQUE = INTARRAY.array_deques();
    @NotNull IntArrayType.StackType INTARRAY_STACK = INTARRAY.stacks();

    // LONG
    @NotNull LongType LONG = new LongType();
    @NotNull LongType.ConcurrentSkipListSetType LONG_CONCURRENT_SKIP_LIST_SET = LONG.concurrent_skip_list_sets();
    @NotNull LongType.ConcurrentSetType LONG_CONCURRENT_SET = LONG.concurrent_sets();
    @NotNull LongType.LinkedSetType LONG_LINKED_SET = LONG.linked_sets();
    @NotNull LongType.TreeSetType LONG_TREE_SET = LONG.tree_sets();
    @NotNull LongType.SetType LONG_SET = LONG.sets();
    @NotNull LongType.ListType LONG_LIST = LONG.lists();
    @NotNull LongType.ArrayType LONG_ARRAY = LONG.arrays();
    @NotNull LongType.PriorityBlockingQueueType LONG_PRIORITY_BLOCKING_QUEUE = LONG.priority_blocking_queues();
    @NotNull LongType.PriorityQueueType LONG_PRIORITY_QUEUE = LONG.priority_queues();
    @NotNull LongType.ArrayDequeType LONG_ARRAY_DEQUE = LONG.array_deques();
    @NotNull LongType.StackType LONG_STACK = LONG.stacks();

    // LONGARRAY
    @NotNull LongArrayType LONGARRAY = new LongArrayType();
    @NotNull LongArrayType.ConcurrentSkipListSetType LONGARRAY_CONCURRENT_SKIP_LIST_SET = LONGARRAY.concurrent_skip_list_sets();
    @NotNull LongArrayType.ConcurrentSetType LONGARRAY_CONCURRENT_SET = LONGARRAY.concurrent_sets();
    @NotNull LongArrayType.LinkedSetType LONGARRAY_LINKED_SET = LONGARRAY.linked_sets();
    @NotNull LongArrayType.TreeSetType LONGARRAY_TREE_SET = LONGARRAY.tree_sets();
    @NotNull LongArrayType.SetType LONGARRAY_SET = LONGARRAY.sets();
    @NotNull LongArrayType.ListType LONGARRAY_LIST = LONGARRAY.lists();
    @NotNull LongArrayType.ArrayType LONGARRAY_ARRAY = LONGARRAY.arrays();
    @NotNull LongArrayType.PriorityBlockingQueueType LONGARRAY_PRIORITY_BLOCKING_QUEUE = LONGARRAY.priority_blocking_queues();
    @NotNull LongArrayType.PriorityQueueType LONGARRAY_PRIORITY_QUEUE = LONGARRAY.priority_queues();
    @NotNull LongArrayType.ArrayDequeType LONGARRAY_ARRAY_DEQUE = LONGARRAY.array_deques();
    @NotNull LongArrayType.StackType LONGARRAY_STACK = LONGARRAY.stacks();

    // SHORT
    @NotNull ShortType SHORT = new ShortType();
    @NotNull ShortType.ConcurrentSkipListSetType SHORT_CONCURRENT_SKIP_LIST_SET = SHORT.concurrent_skip_list_sets();
    @NotNull ShortType.ConcurrentSetType SHORT_CONCURRENT_SET = SHORT.concurrent_sets();
    @NotNull ShortType.LinkedSetType SHORT_LINKED_SET = SHORT.linked_sets();
    @NotNull ShortType.TreeSetType SHORT_TREE_SET = SHORT.tree_sets();
    @NotNull ShortType.SetType SHORT_SET = SHORT.sets();
    @NotNull ShortType.ListType SHORT_LIST = SHORT.lists();
    @NotNull ShortType.ArrayType SHORT_ARRAY = SHORT.arrays();
    @NotNull ShortType.PriorityBlockingQueueType SHORT_PRIORITY_BLOCKING_QUEUE = SHORT.priority_blocking_queues();
    @NotNull ShortType.PriorityQueueType SHORT_PRIORITY_QUEUE = SHORT.priority_queues();
    @NotNull ShortType.ArrayDequeType SHORT_ARRAY_DEQUE = SHORT.array_deques();
    @NotNull ShortType.StackType SHORT_STACK = SHORT.stacks();

    // SHORTARRAY
    @NotNull ShortArrayType SHORTARRAY = new ShortArrayType();
    @NotNull ShortArrayType.ConcurrentSkipListSetType SHORTARRAY_CONCURRENT_SKIP_LIST_SET = SHORTARRAY.concurrent_skip_list_sets();
    @NotNull ShortArrayType.ConcurrentSetType SHORTARRAY_CONCURRENT_SET = SHORTARRAY.concurrent_sets();
    @NotNull ShortArrayType.LinkedSetType SHORTARRAY_LINKED_SET = SHORTARRAY.linked_sets();
    @NotNull ShortArrayType.TreeSetType SHORTARRAY_TREE_SET = SHORTARRAY.tree_sets();
    @NotNull ShortArrayType.SetType SHORTARRAY_SET = SHORTARRAY.sets();
    @NotNull ShortArrayType.ListType SHORTARRAY_LIST = SHORTARRAY.lists();
    @NotNull ShortArrayType.ArrayType SHORTARRAY_ARRAY = SHORTARRAY.arrays();
    @NotNull ShortArrayType.PriorityBlockingQueueType SHORTARRAY_PRIORITY_BLOCKING_QUEUE = SHORTARRAY.priority_blocking_queues();
    @NotNull ShortArrayType.PriorityQueueType SHORTARRAY_PRIORITY_QUEUE = SHORTARRAY.priority_queues();
    @NotNull ShortArrayType.ArrayDequeType SHORTARRAY_ARRAY_DEQUE = SHORTARRAY.array_deques();
    @NotNull ShortArrayType.StackType SHORTARRAY_STACK = SHORTARRAY.stacks();

    // FLOAT
    @NotNull FloatType FLOAT = new FloatType();
    @NotNull FloatType.ConcurrentSkipListSetType FLOAT_CONCURRENT_SKIP_LIST_SET = FLOAT.concurrent_skip_list_sets();
    @NotNull FloatType.ConcurrentSetType FLOAT_CONCURRENT_SET = FLOAT.concurrent_sets();
    @NotNull FloatType.LinkedSetType FLOAT_LINKED_SET = FLOAT.linked_sets();
    @NotNull FloatType.TreeSetType FLOAT_TREE_SET = FLOAT.tree_sets();
    @NotNull FloatType.SetType FLOAT_SET = FLOAT.sets();
    @NotNull FloatType.ListType FLOAT_LIST = FLOAT.lists();
    @NotNull FloatType.ArrayType FLOAT_ARRAY = FLOAT.arrays();
    @NotNull FloatType.PriorityBlockingQueueType FLOAT_PRIORITY_BLOCKING_QUEUE = FLOAT.priority_blocking_queues();
    @NotNull FloatType.PriorityQueueType FLOAT_PRIORITY_QUEUE = FLOAT.priority_queues();
    @NotNull FloatType.ArrayDequeType FLOAT_ARRAY_DEQUE = FLOAT.array_deques();
    @NotNull FloatType.StackType FLOAT_STACK = FLOAT.stacks();

    // FLOATARRAY
    @NotNull FloatArrayType FLOATARRAY = new FloatArrayType();
    @NotNull FloatArrayType.ConcurrentSkipListSetType FLOATARRAY_CONCURRENT_SKIP_LIST_SET = FLOATARRAY.concurrent_skip_list_sets();
    @NotNull FloatArrayType.ConcurrentSetType FLOATARRAY_CONCURRENT_SET = FLOATARRAY.concurrent_sets();
    @NotNull FloatArrayType.LinkedSetType FLOATARRAY_LINKED_SET = FLOATARRAY.linked_sets();
    @NotNull FloatArrayType.TreeSetType FLOATARRAY_TREE_SET = FLOATARRAY.tree_sets();
    @NotNull FloatArrayType.SetType FLOATARRAY_SET = FLOATARRAY.sets();
    @NotNull FloatArrayType.ListType FLOATARRAY_LIST = FLOATARRAY.lists();
    @NotNull FloatArrayType.ArrayType FLOATARRAY_ARRAY = FLOATARRAY.arrays();
    @NotNull FloatArrayType.PriorityBlockingQueueType FLOATARRAY_PRIORITY_BLOCKING_QUEUE = FLOATARRAY.priority_blocking_queues();
    @NotNull FloatArrayType.PriorityQueueType FLOATARRAY_PRIORITY_QUEUE = FLOATARRAY.priority_queues();
    @NotNull FloatArrayType.ArrayDequeType FLOATARRAY_ARRAY_DEQUE = FLOATARRAY.array_deques();
    @NotNull FloatArrayType.StackType FLOATARRAY_STACK = FLOATARRAY.stacks();

    // DOUBLE
    @NotNull DoubleType DOUBLE = new DoubleType();
    @NotNull DoubleType.ConcurrentSkipListSetType DOUBLE_CONCURRENT_SKIP_LIST_SET = DOUBLE.concurrent_skip_list_sets();
    @NotNull DoubleType.ConcurrentSetType DOUBLE_CONCURRENT_SET = DOUBLE.concurrent_sets();
    @NotNull DoubleType.LinkedSetType DOUBLE_LINKED_SET = DOUBLE.linked_sets();
    @NotNull DoubleType.TreeSetType DOUBLE_TREE_SET = DOUBLE.tree_sets();
    @NotNull DoubleType.SetType DOUBLE_SET = DOUBLE.sets();
    @NotNull DoubleType.ListType DOUBLE_LIST = DOUBLE.lists();
    @NotNull DoubleType.ArrayType DOUBLE_ARRAY = DOUBLE.arrays();
    @NotNull DoubleType.PriorityBlockingQueueType DOUBLE_PRIORITY_BLOCKING_QUEUE = DOUBLE.priority_blocking_queues();
    @NotNull DoubleType.PriorityQueueType DOUBLE_PRIORITY_QUEUE = DOUBLE.priority_queues();
    @NotNull DoubleType.ArrayDequeType DOUBLE_ARRAY_DEQUE = DOUBLE.array_deques();
    @NotNull DoubleType.StackType DOUBLE_STACK = DOUBLE.stacks();

    // DOUBLEARRAY
    @NotNull DoubleArrayType DOUBLEARRAY = new DoubleArrayType();
    @NotNull DoubleArrayType.ConcurrentSkipListSetType DOUBLEARRAY_CONCURRENT_SKIP_LIST_SET = DOUBLEARRAY.concurrent_skip_list_sets();
    @NotNull DoubleArrayType.ConcurrentSetType DOUBLEARRAY_CONCURRENT_SET = DOUBLEARRAY.concurrent_sets();
    @NotNull DoubleArrayType.LinkedSetType DOUBLEARRAY_LINKED_SET = DOUBLEARRAY.linked_sets();
    @NotNull DoubleArrayType.TreeSetType DOUBLEARRAY_TREE_SET = DOUBLEARRAY.tree_sets();
    @NotNull DoubleArrayType.SetType DOUBLEARRAY_SET = DOUBLEARRAY.sets();
    @NotNull DoubleArrayType.ListType DOUBLEARRAY_LIST = DOUBLEARRAY.lists();
    @NotNull DoubleArrayType.ArrayType DOUBLEARRAY_ARRAY = DOUBLEARRAY.arrays();
    @NotNull DoubleArrayType.PriorityBlockingQueueType DOUBLEARRAY_PRIORITY_BLOCKING_QUEUE = DOUBLEARRAY.priority_blocking_queues();
    @NotNull DoubleArrayType.PriorityQueueType DOUBLEARRAY_PRIORITY_QUEUE = DOUBLEARRAY.priority_queues();
    @NotNull DoubleArrayType.ArrayDequeType DOUBLEARRAY_ARRAY_DEQUE = DOUBLEARRAY.array_deques();
    @NotNull DoubleArrayType.StackType DOUBLEARRAY_STACK = DOUBLEARRAY.stacks();

    // CHARACTER
    @NotNull CharacterType CHARACTER = new CharacterType();
    @NotNull CharacterType.ConcurrentSkipListSetType CHARACTER_CONCURRENT_SKIP_LIST_SET = CHARACTER.concurrent_skip_list_sets();
    @NotNull CharacterType.ConcurrentSetType CHARACTER_CONCURRENT_SET = CHARACTER.concurrent_sets();
    @NotNull CharacterType.LinkedSetType CHARACTER_LINKED_SET = CHARACTER.linked_sets();
    @NotNull CharacterType.TreeSetType CHARACTER_TREE_SET = CHARACTER.tree_sets();
    @NotNull CharacterType.SetType CHARACTER_SET = CHARACTER.sets();
    @NotNull CharacterType.ListType CHARACTER_LIST = CHARACTER.lists();
    @NotNull CharacterType.ArrayType CHARACTER_ARRAY = CHARACTER.arrays();
    @NotNull CharacterType.PriorityBlockingQueueType CHARACTER_PRIORITY_BLOCKING_QUEUE = CHARACTER.priority_blocking_queues();
    @NotNull CharacterType.PriorityQueueType CHARACTER_PRIORITY_QUEUE = CHARACTER.priority_queues();
    @NotNull CharacterType.ArrayDequeType CHARACTER_ARRAY_DEQUE = CHARACTER.array_deques();
    @NotNull CharacterType.StackType CHARACTER_STACK = CHARACTER.stacks();

    // CHARARRAY
    @NotNull CharArrayType CHARARRAY = new CharArrayType();
    @NotNull CharArrayType.ConcurrentSkipListSetType CHARARRAY_CONCURRENT_SKIP_LIST_SET = CHARARRAY.concurrent_skip_list_sets();
    @NotNull CharArrayType.ConcurrentSetType CHARARRAY_CONCURRENT_SET = CHARARRAY.concurrent_sets();
    @NotNull CharArrayType.LinkedSetType CHARARRAY_LINKED_SET = CHARARRAY.linked_sets();
    @NotNull CharArrayType.TreeSetType CHARARRAY_TREE_SET = CHARARRAY.tree_sets();
    @NotNull CharArrayType.SetType CHARARRAY_SET = CHARARRAY.sets();
    @NotNull CharArrayType.ListType CHARARRAY_LIST = CHARARRAY.lists();
    @NotNull CharArrayType.ArrayType CHARARRAY_ARRAY = CHARARRAY.arrays();
    @NotNull CharArrayType.PriorityBlockingQueueType CHARARRAY_PRIORITY_BLOCKING_QUEUE = CHARARRAY.priority_blocking_queues();
    @NotNull CharArrayType.PriorityQueueType CHARARRAY_PRIORITY_QUEUE = CHARARRAY.priority_queues();
    @NotNull CharArrayType.ArrayDequeType CHARARRAY_ARRAY_DEQUE = CHARARRAY.array_deques();
    @NotNull CharArrayType.StackType CHARARRAY_STACK = CHARARRAY.stacks();

    // BYTE
    @NotNull ByteType BYTE = new ByteType();
    @NotNull ByteType.ConcurrentSkipListSetType BYTE_CONCURRENT_SKIP_LIST_SET = BYTE.concurrent_skip_list_sets();
    @NotNull ByteType.ConcurrentSetType BYTE_CONCURRENT_SET = BYTE.concurrent_sets();
    @NotNull ByteType.LinkedSetType BYTE_LINKED_SET = BYTE.linked_sets();
    @NotNull ByteType.TreeSetType BYTE_TREE_SET = BYTE.tree_sets();
    @NotNull ByteType.SetType BYTE_SET = BYTE.sets();
    @NotNull ByteType.ListType BYTE_LIST = BYTE.lists();
    @NotNull ByteType.ArrayType BYTE_ARRAY = BYTE.arrays();
    @NotNull ByteType.PriorityBlockingQueueType BYTE_PRIORITY_BLOCKING_QUEUE = BYTE.priority_blocking_queues();
    @NotNull ByteType.PriorityQueueType BYTE_PRIORITY_QUEUE = BYTE.priority_queues();
    @NotNull ByteType.ArrayDequeType BYTE_ARRAY_DEQUE = BYTE.array_deques();
    @NotNull ByteType.StackType BYTE_STACK = BYTE.stacks();

    // BYTEARRAY
    @NotNull ByteArrayType BYTEARRAY = new ByteArrayType();
    @NotNull ByteArrayType.ConcurrentSkipListSetType BYTEARRAY_CONCURRENT_SKIP_LIST_SET = BYTEARRAY.concurrent_skip_list_sets();
    @NotNull ByteArrayType.ConcurrentSetType BYTEARRAY_CONCURRENT_SET = BYTEARRAY.concurrent_sets();
    @NotNull ByteArrayType.LinkedSetType BYTEARRAY_LINKED_SET = BYTEARRAY.linked_sets();
    @NotNull ByteArrayType.TreeSetType BYTEARRAY_TREE_SET = BYTEARRAY.tree_sets();
    @NotNull ByteArrayType.SetType BYTEARRAY_SET = BYTEARRAY.sets();
    @NotNull ByteArrayType.ListType BYTEARRAY_LIST = BYTEARRAY.lists();
    @NotNull ByteArrayType.ArrayType BYTEARRAY_ARRAY = BYTEARRAY.arrays();
    @NotNull ByteArrayType.PriorityBlockingQueueType BYTEARRAY_PRIORITY_BLOCKING_QUEUE = BYTEARRAY.priority_blocking_queues();
    @NotNull ByteArrayType.PriorityQueueType BYTEARRAY_PRIORITY_QUEUE = BYTEARRAY.priority_queues();
    @NotNull ByteArrayType.ArrayDequeType BYTEARRAY_ARRAY_DEQUE = BYTEARRAY.array_deques();
    @NotNull ByteArrayType.StackType BYTEARRAY_STACK = BYTEARRAY.stacks();

    // PATH
    @NotNull PathType PATH = new PathType();
    @NotNull PathType.ConcurrentSkipListSetType PATH_CONCURRENT_SKIP_LIST_SET = PATH.concurrent_skip_list_sets();
    @NotNull PathType.ConcurrentSetType PATH_CONCURRENT_SET = PATH.concurrent_sets();
    @NotNull PathType.LinkedSetType PATH_LINKED_SET = PATH.linked_sets();
    @NotNull PathType.TreeSetType PATH_TREE_SET = PATH.tree_sets();
    @NotNull PathType.SetType PATH_SET = PATH.sets();
    @NotNull PathType.ListType PATH_LIST = PATH.lists();
    @NotNull PathType.ArrayType PATH_ARRAY = PATH.arrays();
    @NotNull PathType.PriorityBlockingQueueType PATH_PRIORITY_BLOCKING_QUEUE = PATH.priority_blocking_queues();
    @NotNull PathType.PriorityQueueType PATH_PRIORITY_QUEUE = PATH.priority_queues();
    @NotNull PathType.ArrayDequeType PATH_ARRAY_DEQUE = PATH.array_deques();
    @NotNull PathType.StackType PATH_STACK = PATH.stacks();

    // BOOLEAN
    @NotNull BooleanType BOOLEAN = new BooleanType();
    @NotNull BooleanType.ConcurrentSkipListSetType BOOLEAN_CONCURRENT_SKIP_LIST_SET = BOOLEAN.concurrent_skip_list_sets();
    @NotNull BooleanType.ConcurrentSetType BOOLEAN_CONCURRENT_SET = BOOLEAN.concurrent_sets();
    @NotNull BooleanType.LinkedSetType BOOLEAN_LINKED_SET = BOOLEAN.linked_sets();
    @NotNull BooleanType.TreeSetType BOOLEAN_TREE_SET = BOOLEAN.tree_sets();
    @NotNull BooleanType.SetType BOOLEAN_SET = BOOLEAN.sets();
    @NotNull BooleanType.ListType BOOLEAN_LIST = BOOLEAN.lists();
    @NotNull BooleanType.ArrayType BOOLEAN_ARRAY = BOOLEAN.arrays();
    @NotNull BooleanType.PriorityBlockingQueueType BOOLEAN_PRIORITY_BLOCKING_QUEUE = BOOLEAN.priority_blocking_queues();
    @NotNull BooleanType.PriorityQueueType BOOLEAN_PRIORITY_QUEUE = BOOLEAN.priority_queues();
    @NotNull BooleanType.ArrayDequeType BOOLEAN_ARRAY_DEQUE = BOOLEAN.array_deques();
    @NotNull BooleanType.StackType BOOLEAN_STACK = BOOLEAN.stacks();

    // BOOLEANARRAY
    @NotNull BooleanArrayType BOOLEANARRAY = new BooleanArrayType();
    @NotNull BooleanArrayType.ConcurrentSkipListSetType BOOLEANARRAY_CONCURRENT_SKIP_LIST_SET = BOOLEANARRAY.concurrent_skip_list_sets();
    @NotNull BooleanArrayType.ConcurrentSetType BOOLEANARRAY_CONCURRENT_SET = BOOLEANARRAY.concurrent_sets();
    @NotNull BooleanArrayType.LinkedSetType BOOLEANARRAY_LINKED_SET = BOOLEANARRAY.linked_sets();
    @NotNull BooleanArrayType.TreeSetType BOOLEANARRAY_TREE_SET = BOOLEANARRAY.tree_sets();
    @NotNull BooleanArrayType.SetType BOOLEANARRAY_SET = BOOLEANARRAY.sets();
    @NotNull BooleanArrayType.ListType BOOLEANARRAY_LIST = BOOLEANARRAY.lists();
    @NotNull BooleanArrayType.ArrayType BOOLEANARRAY_ARRAY = BOOLEANARRAY.arrays();
    @NotNull BooleanArrayType.PriorityBlockingQueueType BOOLEANARRAY_PRIORITY_BLOCKING_QUEUE = BOOLEANARRAY.priority_blocking_queues();
    @NotNull BooleanArrayType.PriorityQueueType BOOLEANARRAY_PRIORITY_QUEUE = BOOLEANARRAY.priority_queues();
    @NotNull BooleanArrayType.ArrayDequeType BOOLEANARRAY_ARRAY_DEQUE = BOOLEANARRAY.array_deques();
    @NotNull BooleanArrayType.StackType BOOLEANARRAY_STACK = BOOLEANARRAY.stacks();

    // DATE
    @NotNull DateType DATE = new DateType();
    @NotNull DateType.ConcurrentSkipListSetType DATE_CONCURRENT_SKIP_LIST_SET = DATE.concurrent_skip_list_sets();
    @NotNull DateType.ConcurrentSetType DATE_CONCURRENT_SET = DATE.concurrent_sets();
    @NotNull DateType.LinkedSetType DATE_LINKED_SET = DATE.linked_sets();
    @NotNull DateType.TreeSetType DATE_TREE_SET = DATE.tree_sets();
    @NotNull DateType.SetType DATE_SET = DATE.sets();
    @NotNull DateType.ListType DATE_LIST = DATE.lists();
    @NotNull DateType.ArrayType DATE_ARRAY = DATE.arrays();
    @NotNull DateType.PriorityBlockingQueueType DATE_PRIORITY_BLOCKING_QUEUE = DATE.priority_blocking_queues();
    @NotNull DateType.PriorityQueueType DATE_PRIORITY_QUEUE = DATE.priority_queues();
    @NotNull DateType.ArrayDequeType DATE_ARRAY_DEQUE = DATE.array_deques();
    @NotNull DateType.StackType DATE_STACK = DATE.stacks();

    // INSTANT
    @NotNull InstantType INSTANT = new InstantType();
    @NotNull InstantType.ConcurrentSkipListSetType INSTANT_CONCURRENT_SKIP_LIST_SET = INSTANT.concurrent_skip_list_sets();
    @NotNull InstantType.ConcurrentSetType INSTANT_CONCURRENT_SET = INSTANT.concurrent_sets();
    @NotNull InstantType.LinkedSetType INSTANT_LINKED_SET = INSTANT.linked_sets();
    @NotNull InstantType.TreeSetType INSTANT_TREE_SET = INSTANT.tree_sets();
    @NotNull InstantType.SetType INSTANT_SET = INSTANT.sets();
    @NotNull InstantType.ListType INSTANT_LIST = INSTANT.lists();
    @NotNull InstantType.ArrayType INSTANT_ARRAY = INSTANT.arrays();
    @NotNull InstantType.PriorityBlockingQueueType INSTANT_PRIORITY_BLOCKING_QUEUE = INSTANT.priority_blocking_queues();
    @NotNull InstantType.PriorityQueueType INSTANT_PRIORITY_QUEUE = INSTANT.priority_queues();
    @NotNull InstantType.ArrayDequeType INSTANT_ARRAY_DEQUE = INSTANT.array_deques();
    @NotNull InstantType.StackType INSTANT_STACK = INSTANT.stacks();

    // LOCALDATE
    @NotNull LocalDateType LOCALDATE = new LocalDateType();
    @NotNull LocalDateType.ConcurrentSkipListSetType LOCALDATE_CONCURRENT_SKIP_LIST_SET = LOCALDATE.concurrent_skip_list_sets();
    @NotNull LocalDateType.ConcurrentSetType LOCALDATE_CONCURRENT_SET = LOCALDATE.concurrent_sets();
    @NotNull LocalDateType.LinkedSetType LOCALDATE_LINKED_SET = LOCALDATE.linked_sets();
    @NotNull LocalDateType.TreeSetType LOCALDATE_TREE_SET = LOCALDATE.tree_sets();
    @NotNull LocalDateType.SetType LOCALDATE_SET = LOCALDATE.sets();
    @NotNull LocalDateType.ListType LOCALDATE_LIST = LOCALDATE.lists();
    @NotNull LocalDateType.ArrayType LOCALDATE_ARRAY = LOCALDATE.arrays();
    @NotNull LocalDateType.PriorityBlockingQueueType LOCALDATE_PRIORITY_BLOCKING_QUEUE = LOCALDATE.priority_blocking_queues();
    @NotNull LocalDateType.PriorityQueueType LOCALDATE_PRIORITY_QUEUE = LOCALDATE.priority_queues();
    @NotNull LocalDateType.ArrayDequeType LOCALDATE_ARRAY_DEQUE = LOCALDATE.array_deques();
    @NotNull LocalDateType.StackType LOCALDATE_STACK = LOCALDATE.stacks();

    // LOCALDATETIME
    @NotNull LocalDateTimeType LOCALDATETIME = new LocalDateTimeType();
    @NotNull LocalDateTimeType.ConcurrentSkipListSetType LOCALDATETIME_CONCURRENT_SKIP_LIST_SET = LOCALDATETIME.concurrent_skip_list_sets();
    @NotNull LocalDateTimeType.ConcurrentSetType LOCALDATETIME_CONCURRENT_SET = LOCALDATETIME.concurrent_sets();
    @NotNull LocalDateTimeType.LinkedSetType LOCALDATETIME_LINKED_SET = LOCALDATETIME.linked_sets();
    @NotNull LocalDateTimeType.TreeSetType LOCALDATETIME_TREE_SET = LOCALDATETIME.tree_sets();
    @NotNull LocalDateTimeType.SetType LOCALDATETIME_SET = LOCALDATETIME.sets();
    @NotNull LocalDateTimeType.ListType LOCALDATETIME_LIST = LOCALDATETIME.lists();
    @NotNull LocalDateTimeType.ArrayType LOCALDATETIME_ARRAY = LOCALDATETIME.arrays();
    @NotNull LocalDateTimeType.PriorityBlockingQueueType LOCALDATETIME_PRIORITY_BLOCKING_QUEUE = LOCALDATETIME.priority_blocking_queues();
    @NotNull LocalDateTimeType.PriorityQueueType LOCALDATETIME_PRIORITY_QUEUE = LOCALDATETIME.priority_queues();
    @NotNull LocalDateTimeType.ArrayDequeType LOCALDATETIME_ARRAY_DEQUE = LOCALDATETIME.array_deques();
    @NotNull LocalDateTimeType.StackType LOCALDATETIME_STACK = LOCALDATETIME.stacks();

    // PERIOD
    @NotNull PeriodType PERIOD = new PeriodType();
    @NotNull PeriodType.ConcurrentSkipListSetType PERIOD_CONCURRENT_SKIP_LIST_SET = PERIOD.concurrent_skip_list_sets();
    @NotNull PeriodType.ConcurrentSetType PERIOD_CONCURRENT_SET = PERIOD.concurrent_sets();
    @NotNull PeriodType.LinkedSetType PERIOD_LINKED_SET = PERIOD.linked_sets();
    @NotNull PeriodType.TreeSetType PERIOD_TREE_SET = PERIOD.tree_sets();
    @NotNull PeriodType.SetType PERIOD_SET = PERIOD.sets();
    @NotNull PeriodType.ListType PERIOD_LIST = PERIOD.lists();
    @NotNull PeriodType.ArrayType PERIOD_ARRAY = PERIOD.arrays();
    @NotNull PeriodType.PriorityBlockingQueueType PERIOD_PRIORITY_BLOCKING_QUEUE = PERIOD.priority_blocking_queues();
    @NotNull PeriodType.PriorityQueueType PERIOD_PRIORITY_QUEUE = PERIOD.priority_queues();
    @NotNull PeriodType.ArrayDequeType PERIOD_ARRAY_DEQUE = PERIOD.array_deques();
    @NotNull PeriodType.StackType PERIOD_STACK = PERIOD.stacks();

    // DURATION
    @NotNull DurationType DURATION = new DurationType();
    @NotNull DurationType.ConcurrentSkipListSetType DURATION_CONCURRENT_SKIP_LIST_SET = DURATION.concurrent_skip_list_sets();
    @NotNull DurationType.ConcurrentSetType DURATION_CONCURRENT_SET = DURATION.concurrent_sets();
    @NotNull DurationType.LinkedSetType DURATION_LINKED_SET = DURATION.linked_sets();
    @NotNull DurationType.TreeSetType DURATION_TREE_SET = DURATION.tree_sets();
    @NotNull DurationType.SetType DURATION_SET = DURATION.sets();
    @NotNull DurationType.ListType DURATION_LIST = DURATION.lists();
    @NotNull DurationType.ArrayType DURATION_ARRAY = DURATION.arrays();
    @NotNull DurationType.PriorityBlockingQueueType DURATION_PRIORITY_BLOCKING_QUEUE = DURATION.priority_blocking_queues();
    @NotNull DurationType.PriorityQueueType DURATION_PRIORITY_QUEUE = DURATION.priority_queues();
    @NotNull DurationType.ArrayDequeType DURATION_ARRAY_DEQUE = DURATION.array_deques();
    @NotNull DurationType.StackType DURATION_STACK = DURATION.stacks();

    // COMPONENT
    @NotNull ComponentType COMPONENT = new ComponentType();
    @NotNull ComponentType.ConcurrentSkipListSetType COMPONENT_CONCURRENT_SKIP_LIST_SET = COMPONENT.concurrent_skip_list_sets();
    @NotNull ComponentType.ConcurrentSetType COMPONENT_CONCURRENT_SET = COMPONENT.concurrent_sets();
    @NotNull ComponentType.LinkedSetType COMPONENT_LINKED_SET = COMPONENT.linked_sets();
    @NotNull ComponentType.TreeSetType COMPONENT_TREE_SET = COMPONENT.tree_sets();
    @NotNull ComponentType.SetType COMPONENT_SET = COMPONENT.sets();
    @NotNull ComponentType.ListType COMPONENT_LIST = COMPONENT.lists();
    @NotNull ComponentType.ArrayType COMPONENT_ARRAY = COMPONENT.arrays();
    @NotNull ComponentType.PriorityBlockingQueueType COMPONENT_PRIORITY_BLOCKING_QUEUE = COMPONENT.priority_blocking_queues();
    @NotNull ComponentType.PriorityQueueType COMPONENT_PRIORITY_QUEUE = COMPONENT.priority_queues();
    @NotNull ComponentType.ArrayDequeType COMPONENT_ARRAY_DEQUE = COMPONENT.array_deques();
    @NotNull ComponentType.StackType COMPONENT_STACK = COMPONENT.stacks();

    // BIGINTEGER
    @NotNull BigIntegerType BIGINTEGER = new BigIntegerType();
    @NotNull BigIntegerType.ConcurrentSkipListSetType BIGINTEGER_CONCURRENT_SKIP_LIST_SET = BIGINTEGER.concurrent_skip_list_sets();
    @NotNull BigIntegerType.ConcurrentSetType BIGINTEGER_CONCURRENT_SET = BIGINTEGER.concurrent_sets();
    @NotNull BigIntegerType.LinkedSetType BIGINTEGER_LINKED_SET = BIGINTEGER.linked_sets();
    @NotNull BigIntegerType.TreeSetType BIGINTEGER_TREE_SET = BIGINTEGER.tree_sets();
    @NotNull BigIntegerType.SetType BIGINTEGER_SET = BIGINTEGER.sets();
    @NotNull BigIntegerType.ListType BIGINTEGER_LIST = BIGINTEGER.lists();
    @NotNull BigIntegerType.ArrayType BIGINTEGER_ARRAY = BIGINTEGER.arrays();
    @NotNull BigIntegerType.PriorityBlockingQueueType BIGINTEGER_PRIORITY_BLOCKING_QUEUE = BIGINTEGER.priority_blocking_queues();
    @NotNull BigIntegerType.PriorityQueueType BIGINTEGER_PRIORITY_QUEUE = BIGINTEGER.priority_queues();
    @NotNull BigIntegerType.ArrayDequeType BIGINTEGER_ARRAY_DEQUE = BIGINTEGER.array_deques();
    @NotNull BigIntegerType.StackType BIGINTEGER_STACK = BIGINTEGER.stacks();

    // BIGDECIMAL
    @NotNull BigDecimalType BIGDECIMAL = new BigDecimalType();
    @NotNull BigDecimalType.ConcurrentSkipListSetType BIGDECIMAL_CONCURRENT_SKIP_LIST_SET = BIGDECIMAL.concurrent_skip_list_sets();
    @NotNull BigDecimalType.ConcurrentSetType BIGDECIMAL_CONCURRENT_SET = BIGDECIMAL.concurrent_sets();
    @NotNull BigDecimalType.LinkedSetType BIGDECIMAL_LINKED_SET = BIGDECIMAL.linked_sets();
    @NotNull BigDecimalType.TreeSetType BIGDECIMAL_TREE_SET = BIGDECIMAL.tree_sets();
    @NotNull BigDecimalType.SetType BIGDECIMAL_SET = BIGDECIMAL.sets();
    @NotNull BigDecimalType.ListType BIGDECIMAL_LIST = BIGDECIMAL.lists();
    @NotNull BigDecimalType.ArrayType BIGDECIMAL_ARRAY = BIGDECIMAL.arrays();
    @NotNull BigDecimalType.PriorityBlockingQueueType BIGDECIMAL_PRIORITY_BLOCKING_QUEUE = BIGDECIMAL.priority_blocking_queues();
    @NotNull BigDecimalType.PriorityQueueType BIGDECIMAL_PRIORITY_QUEUE = BIGDECIMAL.priority_queues();
    @NotNull BigDecimalType.ArrayDequeType BIGDECIMAL_ARRAY_DEQUE = BIGDECIMAL.array_deques();
    @NotNull BigDecimalType.StackType BIGDECIMAL_STACK = BIGDECIMAL.stacks();

    // WORLD
    @NotNull WorldType WORLD = new WorldType();
    @NotNull WorldType.ConcurrentSkipListSetType WORLD_CONCURRENT_SKIP_LIST_SET = WORLD.concurrent_skip_list_sets();
    @NotNull WorldType.ConcurrentSetType WORLD_CONCURRENT_SET = WORLD.concurrent_sets();
    @NotNull WorldType.LinkedSetType WORLD_LINKED_SET = WORLD.linked_sets();
    @NotNull WorldType.TreeSetType WORLD_TREE_SET = WORLD.tree_sets();
    @NotNull WorldType.SetType WORLD_SET = WORLD.sets();
    @NotNull WorldType.ListType WORLD_LIST = WORLD.lists();
    @NotNull WorldType.ArrayType WORLD_ARRAY = WORLD.arrays();
    @NotNull WorldType.PriorityBlockingQueueType WORLD_PRIORITY_BLOCKING_QUEUE = WORLD.priority_blocking_queues();
    @NotNull WorldType.PriorityQueueType WORLD_PRIORITY_QUEUE = WORLD.priority_queues();
    @NotNull WorldType.ArrayDequeType WORLD_ARRAY_DEQUE = WORLD.array_deques();
    @NotNull WorldType.StackType WORLD_STACK = WORLD.stacks();

    // BLOCKDATA
    @NotNull BlockDataType BLOCKDATA = new BlockDataType();
    @NotNull BlockDataType.ConcurrentSkipListSetType BLOCKDATA_CONCURRENT_SKIP_LIST_SET = BLOCKDATA.concurrent_skip_list_sets();
    @NotNull BlockDataType.ConcurrentSetType BLOCKDATA_CONCURRENT_SET = BLOCKDATA.concurrent_sets();
    @NotNull BlockDataType.LinkedSetType BLOCKDATA_LINKED_SET = BLOCKDATA.linked_sets();
    @NotNull BlockDataType.TreeSetType BLOCKDATA_TREE_SET = BLOCKDATA.tree_sets();
    @NotNull BlockDataType.SetType BLOCKDATA_SET = BLOCKDATA.sets();
    @NotNull BlockDataType.ListType BLOCKDATA_LIST = BLOCKDATA.lists();
    @NotNull BlockDataType.ArrayType BLOCKDATA_ARRAY = BLOCKDATA.arrays();
    @NotNull BlockDataType.PriorityBlockingQueueType BLOCKDATA_PRIORITY_BLOCKING_QUEUE = BLOCKDATA.priority_blocking_queues();
    @NotNull BlockDataType.PriorityQueueType BLOCKDATA_PRIORITY_QUEUE = BLOCKDATA.priority_queues();
    @NotNull BlockDataType.ArrayDequeType BLOCKDATA_ARRAY_DEQUE = BLOCKDATA.array_deques();
    @NotNull BlockDataType.StackType BLOCKDATA_STACK = BLOCKDATA.stacks();

    // CHUNK
    @NotNull ChunkType CHUNK = new ChunkType();
    @NotNull ChunkType.ConcurrentSkipListSetType CHUNK_CONCURRENT_SKIP_LIST_SET = CHUNK.concurrent_skip_list_sets();
    @NotNull ChunkType.ConcurrentSetType CHUNK_CONCURRENT_SET = CHUNK.concurrent_sets();
    @NotNull ChunkType.LinkedSetType CHUNK_LINKED_SET = CHUNK.linked_sets();
    @NotNull ChunkType.TreeSetType CHUNK_TREE_SET = CHUNK.tree_sets();
    @NotNull ChunkType.SetType CHUNK_SET = CHUNK.sets();
    @NotNull ChunkType.ListType CHUNK_LIST = CHUNK.lists();
    @NotNull ChunkType.ArrayType CHUNK_ARRAY = CHUNK.arrays();
    @NotNull ChunkType.PriorityBlockingQueueType CHUNK_PRIORITY_BLOCKING_QUEUE = CHUNK.priority_blocking_queues();
    @NotNull ChunkType.PriorityQueueType CHUNK_PRIORITY_QUEUE = CHUNK.priority_queues();
    @NotNull ChunkType.ArrayDequeType CHUNK_ARRAY_DEQUE = CHUNK.array_deques();
    @NotNull ChunkType.StackType CHUNK_STACK = CHUNK.stacks();

    // DEPRECATEDMATERIAL
    @NotNull DeprecatedMaterialType DEPRECATEDMATERIAL = new DeprecatedMaterialType();
    @NotNull DeprecatedMaterialType.ConcurrentSkipListSetType DEPRECATEDMATERIAL_CONCURRENT_SKIP_LIST_SET = DEPRECATEDMATERIAL.concurrent_skip_list_sets();
    @NotNull DeprecatedMaterialType.ConcurrentSetType DEPRECATEDMATERIAL_CONCURRENT_SET = DEPRECATEDMATERIAL.concurrent_sets();
    @NotNull DeprecatedMaterialType.LinkedSetType DEPRECATEDMATERIAL_LINKED_SET = DEPRECATEDMATERIAL.linked_sets();
    @NotNull DeprecatedMaterialType.TreeSetType DEPRECATEDMATERIAL_TREE_SET = DEPRECATEDMATERIAL.tree_sets();
    @NotNull DeprecatedMaterialType.SetType DEPRECATEDMATERIAL_SET = DEPRECATEDMATERIAL.sets();
    @NotNull DeprecatedMaterialType.ListType DEPRECATEDMATERIAL_LIST = DEPRECATEDMATERIAL.lists();
    @NotNull DeprecatedMaterialType.ArrayType DEPRECATEDMATERIAL_ARRAY = DEPRECATEDMATERIAL.arrays();
    @NotNull DeprecatedMaterialType.PriorityBlockingQueueType DEPRECATEDMATERIAL_PRIORITY_BLOCKING_QUEUE = DEPRECATEDMATERIAL.priority_blocking_queues();
    @NotNull DeprecatedMaterialType.PriorityQueueType DEPRECATEDMATERIAL_PRIORITY_QUEUE = DEPRECATEDMATERIAL.priority_queues();
    @NotNull DeprecatedMaterialType.ArrayDequeType DEPRECATEDMATERIAL_ARRAY_DEQUE = DEPRECATEDMATERIAL.array_deques();
    @NotNull DeprecatedMaterialType.StackType DEPRECATEDMATERIAL_STACK = DEPRECATEDMATERIAL.stacks();

    // MATERIAL
    @NotNull MaterialType MATERIAL = new MaterialType();
    @NotNull MaterialType.ConcurrentSkipListSetType MATERIAL_CONCURRENT_SKIP_LIST_SET = MATERIAL.concurrent_skip_list_sets();
    @NotNull MaterialType.ConcurrentSetType MATERIAL_CONCURRENT_SET = MATERIAL.concurrent_sets();
    @NotNull MaterialType.LinkedSetType MATERIAL_LINKED_SET = MATERIAL.linked_sets();
    @NotNull MaterialType.TreeSetType MATERIAL_TREE_SET = MATERIAL.tree_sets();
    @NotNull MaterialType.SetType MATERIAL_SET = MATERIAL.sets();
    @NotNull MaterialType.ListType MATERIAL_LIST = MATERIAL.lists();
    @NotNull MaterialType.ArrayType MATERIAL_ARRAY = MATERIAL.arrays();
    @NotNull MaterialType.PriorityBlockingQueueType MATERIAL_PRIORITY_BLOCKING_QUEUE = MATERIAL.priority_blocking_queues();
    @NotNull MaterialType.PriorityQueueType MATERIAL_PRIORITY_QUEUE = MATERIAL.priority_queues();
    @NotNull MaterialType.ArrayDequeType MATERIAL_ARRAY_DEQUE = MATERIAL.array_deques();
    @NotNull MaterialType.StackType MATERIAL_STACK = MATERIAL.stacks();

    // LOCATION
    @NotNull LocationType LOCATION = new LocationType();
    @NotNull LocationType.ConcurrentSkipListSetType LOCATION_CONCURRENT_SKIP_LIST_SET = LOCATION.concurrent_skip_list_sets();
    @NotNull LocationType.ConcurrentSetType LOCATION_CONCURRENT_SET = LOCATION.concurrent_sets();
    @NotNull LocationType.LinkedSetType LOCATION_LINKED_SET = LOCATION.linked_sets();
    @NotNull LocationType.TreeSetType LOCATION_TREE_SET = LOCATION.tree_sets();
    @NotNull LocationType.SetType LOCATION_SET = LOCATION.sets();
    @NotNull LocationType.ListType LOCATION_LIST = LOCATION.lists();
    @NotNull LocationType.ArrayType LOCATION_ARRAY = LOCATION.arrays();
    @NotNull LocationType.PriorityBlockingQueueType LOCATION_PRIORITY_BLOCKING_QUEUE = LOCATION.priority_blocking_queues();
    @NotNull LocationType.PriorityQueueType LOCATION_PRIORITY_QUEUE = LOCATION.priority_queues();
    @NotNull LocationType.ArrayDequeType LOCATION_ARRAY_DEQUE = LOCATION.array_deques();
    @NotNull LocationType.StackType LOCATION_STACK = LOCATION.stacks();

    // ITEMSTACK
    @NotNull ItemStackType ITEMSTACK = new ItemStackType();
    @NotNull ItemStackType.ConcurrentSkipListSetType ITEMSTACK_CONCURRENT_SKIP_LIST_SET = ITEMSTACK.concurrent_skip_list_sets();
    @NotNull ItemStackType.ConcurrentSetType ITEMSTACK_CONCURRENT_SET = ITEMSTACK.concurrent_sets();
    @NotNull ItemStackType.LinkedSetType ITEMSTACK_LINKED_SET = ITEMSTACK.linked_sets();
    @NotNull ItemStackType.TreeSetType ITEMSTACK_TREE_SET = ITEMSTACK.tree_sets();
    @NotNull ItemStackType.SetType ITEMSTACK_SET = ITEMSTACK.sets();
    @NotNull ItemStackType.ListType ITEMSTACK_LIST = ITEMSTACK.lists();
    @NotNull ItemStackType.ArrayType ITEMSTACK_ARRAY = ITEMSTACK.arrays();
    @NotNull ItemStackType.PriorityBlockingQueueType ITEMSTACK_PRIORITY_BLOCKING_QUEUE = ITEMSTACK.priority_blocking_queues();
    @NotNull ItemStackType.PriorityQueueType ITEMSTACK_PRIORITY_QUEUE = ITEMSTACK.priority_queues();
    @NotNull ItemStackType.ArrayDequeType ITEMSTACK_ARRAY_DEQUE = ITEMSTACK.array_deques();
    @NotNull ItemStackType.StackType ITEMSTACK_STACK = ITEMSTACK.stacks();

    // GAMEMODE
    @NotNull GameModeType GAMEMODE = new GameModeType();
    @NotNull GameModeType.ConcurrentSkipListSetType GAMEMODE_CONCURRENT_SKIP_LIST_SET = GAMEMODE.concurrent_skip_list_sets();
    @NotNull GameModeType.ConcurrentSetType GAMEMODE_CONCURRENT_SET = GAMEMODE.concurrent_sets();
    @NotNull GameModeType.LinkedSetType GAMEMODE_LINKED_SET = GAMEMODE.linked_sets();
    @NotNull GameModeType.TreeSetType GAMEMODE_TREE_SET = GAMEMODE.tree_sets();
    @NotNull GameModeType.SetType GAMEMODE_SET = GAMEMODE.sets();
    @NotNull GameModeType.ListType GAMEMODE_LIST = GAMEMODE.lists();
    @NotNull GameModeType.ArrayType GAMEMODE_ARRAY = GAMEMODE.arrays();
    @NotNull GameModeType.PriorityBlockingQueueType GAMEMODE_PRIORITY_BLOCKING_QUEUE = GAMEMODE.priority_blocking_queues();
    @NotNull GameModeType.PriorityQueueType GAMEMODE_PRIORITY_QUEUE = GAMEMODE.priority_queues();
    @NotNull GameModeType.ArrayDequeType GAMEMODE_ARRAY_DEQUE = GAMEMODE.array_deques();
    @NotNull GameModeType.StackType GAMEMODE_STACK = GAMEMODE.stacks();

    // INVENTORY
    @NotNull InventoryType INVENTORY = new InventoryType();
    @NotNull InventoryType.ConcurrentSkipListSetType INVENTORY_CONCURRENT_SKIP_LIST_SET = INVENTORY.concurrent_skip_list_sets();
    @NotNull InventoryType.ConcurrentSetType INVENTORY_CONCURRENT_SET = INVENTORY.concurrent_sets();
    @NotNull InventoryType.LinkedSetType INVENTORY_LINKED_SET = INVENTORY.linked_sets();
    @NotNull InventoryType.TreeSetType INVENTORY_TREE_SET = INVENTORY.tree_sets();
    @NotNull InventoryType.SetType INVENTORY_SET = INVENTORY.sets();
    @NotNull InventoryType.ListType INVENTORY_LIST = INVENTORY.lists();
    @NotNull InventoryType.ArrayType INVENTORY_ARRAY = INVENTORY.arrays();
    @NotNull InventoryType.PriorityBlockingQueueType INVENTORY_PRIORITY_BLOCKING_QUEUE = INVENTORY.priority_blocking_queues();
    @NotNull InventoryType.PriorityQueueType INVENTORY_PRIORITY_QUEUE = INVENTORY.priority_queues();
    @NotNull InventoryType.ArrayDequeType INVENTORY_ARRAY_DEQUE = INVENTORY.array_deques();
    @NotNull InventoryType.StackType INVENTORY_STACK = INVENTORY.stacks();

    // VECTOR
    @NotNull VectorType VECTOR = new VectorType();
    @NotNull VectorType.ConcurrentSkipListSetType VECTOR_CONCURRENT_SKIP_LIST_SET = VECTOR.concurrent_skip_list_sets();
    @NotNull VectorType.ConcurrentSetType VECTOR_CONCURRENT_SET = VECTOR.concurrent_sets();
    @NotNull VectorType.LinkedSetType VECTOR_LINKED_SET = VECTOR.linked_sets();
    @NotNull VectorType.TreeSetType VECTOR_TREE_SET = VECTOR.tree_sets();
    @NotNull VectorType.SetType VECTOR_SET = VECTOR.sets();
    @NotNull VectorType.ListType VECTOR_LIST = VECTOR.lists();
    @NotNull VectorType.ArrayType VECTOR_ARRAY = VECTOR.arrays();
    @NotNull VectorType.PriorityBlockingQueueType VECTOR_PRIORITY_BLOCKING_QUEUE = VECTOR.priority_blocking_queues();
    @NotNull VectorType.PriorityQueueType VECTOR_PRIORITY_QUEUE = VECTOR.priority_queues();
    @NotNull VectorType.ArrayDequeType VECTOR_ARRAY_DEQUE = VECTOR.array_deques();
    @NotNull VectorType.StackType VECTOR_STACK = VECTOR.stacks();

    // BOUNDINGBOX
    @NotNull BoundingBoxType BOUNDINGBOX = new BoundingBoxType();
    @NotNull BoundingBoxType.ConcurrentSkipListSetType BOUNDINGBOX_CONCURRENT_SKIP_LIST_SET = BOUNDINGBOX.concurrent_skip_list_sets();
    @NotNull BoundingBoxType.ConcurrentSetType BOUNDINGBOX_CONCURRENT_SET = BOUNDINGBOX.concurrent_sets();
    @NotNull BoundingBoxType.LinkedSetType BOUNDINGBOX_LINKED_SET = BOUNDINGBOX.linked_sets();
    @NotNull BoundingBoxType.TreeSetType BOUNDINGBOX_TREE_SET = BOUNDINGBOX.tree_sets();
    @NotNull BoundingBoxType.SetType BOUNDINGBOX_SET = BOUNDINGBOX.sets();
    @NotNull BoundingBoxType.ListType BOUNDINGBOX_LIST = BOUNDINGBOX.lists();
    @NotNull BoundingBoxType.ArrayType BOUNDINGBOX_ARRAY = BOUNDINGBOX.arrays();
    @NotNull BoundingBoxType.PriorityBlockingQueueType BOUNDINGBOX_PRIORITY_BLOCKING_QUEUE = BOUNDINGBOX.priority_blocking_queues();
    @NotNull BoundingBoxType.PriorityQueueType BOUNDINGBOX_PRIORITY_QUEUE = BOUNDINGBOX.priority_queues();
    @NotNull BoundingBoxType.ArrayDequeType BOUNDINGBOX_ARRAY_DEQUE = BOUNDINGBOX.array_deques();
    @NotNull BoundingBoxType.StackType BOUNDINGBOX_STACK = BOUNDINGBOX.stacks();
}
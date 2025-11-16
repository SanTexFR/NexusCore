package fr.nexus.api.var.types;

import fr.nexus.api.var.types.parents.normal.adventure.ComponentType;
import fr.nexus.api.var.types.parents.normal.big.BigDecimalType;
import fr.nexus.api.var.types.parents.normal.big.BigIntegerType;
import fr.nexus.api.var.types.parents.normal.bukkit.*;
import fr.nexus.api.var.types.parents.normal.java.*;
import fr.nexus.api.var.types.parents.normal.java.date.*;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface VarTypes{
    //STRING
    @NotNull StringType STRING=new StringType();
    @NotNull StringType.SetType STRING_SET=STRING.sets();
    @NotNull StringType.LinkedSetType STRING_LINKED_SET=STRING.linked_sets();
    @NotNull StringType.ListType STRING_LIST=STRING.lists();
    @NotNull StringType.ArrayType STRING_ARRAY=STRING.arrays();

    //UUID
    @NotNull UUIDType UUID=new UUIDType();
    @NotNull UUIDType.SetType UUID_SET=UUID.sets();
    @NotNull UUIDType.LinkedSetType UUID_LINKED_SET=UUID.linked_sets();
    @NotNull UUIDType.ListType UUID_LIST=UUID.lists();
    @NotNull UUIDType.ArrayType UUID_ARRAY=UUID.arrays();

    //INTEGER
    @NotNull IntegerType INTEGER=new IntegerType();
    @NotNull IntegerType.SetType INTEGER_SET=INTEGER.sets();
    @NotNull IntegerType.LinkedSetType INTEGER_LINKED_SET=INTEGER.linked_sets();
    @NotNull IntegerType.ListType INTEGER_LIST=INTEGER.lists();
    @NotNull IntegerType.ArrayType INTEGER_ARRAY=INTEGER.arrays();

    //LONG
    @NotNull LongType LONG=new LongType();
    @NotNull LongType.SetType LONG_SET=LONG.sets();
    @NotNull LongType.LinkedSetType LONG_LINKED_SET=LONG.linked_sets();
    @NotNull LongType.ListType LONG_LIST=LONG.lists();
    @NotNull LongType.ArrayType LONG_ARRAY=LONG.arrays();

    //SHORT
    @NotNull ShortType SHORT=new ShortType();
    @NotNull ShortType.SetType SHORT_SET=SHORT.sets();
    @NotNull ShortType.LinkedSetType SHORT_LINKED_SET=SHORT.linked_sets();
    @NotNull ShortType.ListType SHORT_LIST=SHORT.lists();
    @NotNull ShortType.ArrayType SHORT_ARRAY=SHORT.arrays();

    //FLOAT
    @NotNull FloatType FLOAT=new FloatType();
    @NotNull FloatType.SetType FLOAT_SET=FLOAT.sets();
    @NotNull FloatType.LinkedSetType FLOAT_LINKED_SET=FLOAT.linked_sets();
    @NotNull FloatType.ListType FLOAT_LIST=FLOAT.lists();
    @NotNull FloatType.ArrayType FLOAT_ARRAY=FLOAT.arrays();

    //DOUBLE
    @NotNull DoubleType DOUBLE=new DoubleType();
    @NotNull DoubleType.SetType DOUBLE_SET=DOUBLE.sets();
    @NotNull DoubleType.LinkedSetType DOUBLE_LINKED_SET=DOUBLE.linked_sets();
    @NotNull DoubleType.ListType DOUBLE_LIST=DOUBLE.lists();
    @NotNull DoubleType.ArrayType DOUBLE_ARRAY=DOUBLE.arrays();

    //CHARACTER
    @NotNull CharType CHAR=new CharType();
    @NotNull CharType.SetType CHAR_SET=CHAR.sets();
    @NotNull CharType.LinkedSetType CHAR_LINKED_SET=CHAR.linked_sets();
    @NotNull CharType.ListType CHAR_LIST=CHAR.lists();
    @NotNull CharType.ArrayType CHAR_ARRAY=CHAR.arrays();

    //BYTE
    @NotNull ByteType BYTE=new ByteType();
    @NotNull ByteType.SetType BYTE_SET=BYTE.sets();
    @NotNull ByteType.LinkedSetType BYTE_LINKED_SET=BYTE.linked_sets();
    @NotNull ByteType.ListType BYTE_LIST=BYTE.lists();
    @NotNull ByteType.ArrayType BYTE_ARRAY=BYTE.arrays();

    //PATH
    @NotNull PathType PATH=new PathType();
    @NotNull PathType.SetType PATH_SET=PATH.sets();
    @NotNull PathType.LinkedSetType PATH_LINKED_SET=PATH.linked_sets();
    @NotNull PathType.ListType PATH_LIST=PATH.lists();
    @NotNull PathType.ArrayType PATH_ARRAY=PATH.arrays();

    //BOOLEAN
    @NotNull BooleanType BOOLEAN=new BooleanType();
    @NotNull BooleanType.SetType BOOLEAN_SET=BOOLEAN.sets();
    @NotNull BooleanType.LinkedSetType BOOLEAN_LINKED_SET=BOOLEAN.linked_sets();
    @NotNull BooleanType.ListType BOOLEAN_LIST=BOOLEAN.lists();
    @NotNull BooleanType.ArrayType BOOLEAN_ARRAY=BOOLEAN.arrays();








    //DATE
    @NotNull DateType DATE=new DateType();
    @NotNull DateType.SetType DATE_SET=DATE.sets();
    @NotNull DateType.LinkedSetType DATE_LINKED_SET=DATE.linked_sets();
    @NotNull DateType.ListType DATE_LIST=DATE.lists();
    @NotNull DateType.ArrayType DATE_ARRAY=DATE.arrays();

    //INSTANT
    @NotNull InstantType INSTANT=new InstantType();
    @NotNull InstantType.SetType INSTANT_SET=INSTANT.sets();
    @NotNull InstantType.LinkedSetType INSTANT_LINKED_SET=INSTANT.linked_sets();
    @NotNull InstantType.ListType INSTANT_LIST=INSTANT.lists();
    @NotNull InstantType.ArrayType INSTANT_ARRAY=INSTANT.arrays();

    //LOCAL_DATE
    @NotNull LocalDateType LOCAL_DATE=new LocalDateType();
    @NotNull LocalDateType.SetType LOCAL_DATE_SET=LOCAL_DATE.sets();
    @NotNull LocalDateType.LinkedSetType LOCAL_DATE_LINKED_SET=LOCAL_DATE.linked_sets();
    @NotNull LocalDateType.ListType LOCAL_DATE_LIST=LOCAL_DATE.lists();
    @NotNull LocalDateType.ArrayType LOCAL_DATE_ARRAY=LOCAL_DATE.arrays();

    //LOCAL_DATE_TIME
    @NotNull LocalDateTimeType LOCAL_DATE_TIME=new LocalDateTimeType();
    @NotNull LocalDateTimeType.SetType LOCAL_DATE_TIME_SET=LOCAL_DATE_TIME.sets();
    @NotNull LocalDateTimeType.LinkedSetType LOCAL_DATE_TIME_LINKED_SET=LOCAL_DATE_TIME.linked_sets();
    @NotNull LocalDateTimeType.ListType LOCAL_DATE_TIME_LIST=LOCAL_DATE_TIME.lists();
    @NotNull LocalDateTimeType.ArrayType LOCAL_DATE_TIME_ARRAY=LOCAL_DATE_TIME.arrays();

    //PERIOD
    @NotNull PeriodType PERIOD=new PeriodType();
    @NotNull PeriodType.SetType PERIOD_SET=PERIOD.sets();
    @NotNull PeriodType.LinkedSetType PERIOD_LINKED_SET=PERIOD.linked_sets();
    @NotNull PeriodType.ListType PERIOD_LIST=PERIOD.lists();
    @NotNull PeriodType.ArrayType PERIOD_ARRAY=PERIOD.arrays();

    //DURATION
    @NotNull DurationType DURATION=new DurationType();
    @NotNull DurationType.SetType DURATION_SET=DURATION.sets();
    @NotNull DurationType.LinkedSetType DURATION_LINKED_SET=DURATION.linked_sets();
    @NotNull DurationType.ListType DURATION_LIST=DURATION.lists();
    @NotNull DurationType.ArrayType DURATION_ARRAY=DURATION.arrays();








    //COMPONENT
    @NotNull ComponentType COMPONENT=new ComponentType();
    @NotNull ComponentType.SetType COMPONENT_SET=COMPONENT.sets();
    @NotNull ComponentType.LinkedSetType COMPONENT_LINKED_SET=COMPONENT.linked_sets();
    @NotNull ComponentType.ListType COMPONENT_LIST=COMPONENT.lists();
    @NotNull ComponentType.ArrayType COMPONENT_ARRAY=COMPONENT.arrays();








    //BIG_INTEGER
    @NotNull BigIntegerType BIG_INTEGER=new BigIntegerType();
    @NotNull BigIntegerType.SetType BIG_INTEGER_SET=BIG_INTEGER.sets();
    @NotNull BigIntegerType.LinkedSetType BIG_INTEGER_LINKED_SET=BIG_INTEGER.linked_sets();
    @NotNull BigIntegerType.ListType BIG_INTEGER_LIST=BIG_INTEGER.lists();
    @NotNull BigIntegerType.ArrayType BIG_INTEGER_ARRAY=BIG_INTEGER.arrays();

    //BIG_DECIMAL
    @NotNull BigDecimalType BIG_DECIMAL=new BigDecimalType();
    @NotNull BigDecimalType.SetType BIG_DECIMAL_SET=BIG_DECIMAL.sets();
    @NotNull BigDecimalType.LinkedSetType BIG_DECIMAL_LINKED_SET=BIG_DECIMAL.linked_sets();
    @NotNull BigDecimalType.ListType BIG_DECIMAL_LIST=BIG_DECIMAL.lists();
    @NotNull BigDecimalType.ArrayType BIG_DECIMAL_ARRAY=BIG_DECIMAL.arrays();








    //WORLD
    @NotNull WorldType WORLD=new WorldType();
    @NotNull WorldType.SetType WORLD_SET=WORLD.sets();
    @NotNull WorldType.LinkedSetType WORLD_LINKED_SET=WORLD.linked_sets();
    @NotNull WorldType.ListType WORLD_LIST=WORLD.lists();
    @NotNull WorldType.ArrayType WORLD_ARRAY=WORLD.arrays();

    //BLOCK-DATA
    @NotNull BlockDataType BLOCKDATA=new BlockDataType();
    @NotNull BlockDataType.SetType BLOCKDATA_SET=BLOCKDATA.sets();
    @NotNull BlockDataType.LinkedSetType BLOCKDATA_LINKED_SET=BLOCKDATA.linked_sets();
    @NotNull BlockDataType.ListType BLOCKDATA_LIST=BLOCKDATA.lists();
    @NotNull BlockDataType.ArrayType BLOCKDATA_ARRAY=BLOCKDATA.arrays();

    //CHUNK
    @NotNull ChunkType CHUNK=new ChunkType();
    @NotNull ChunkType.SetType CHUNK_SET=CHUNK.sets();
    @NotNull ChunkType.LinkedSetType CHUNK_LINKED_SET=CHUNK.linked_sets();
    @NotNull ChunkType.ListType CHUNK_LIST=CHUNK.lists();
    @NotNull ChunkType.ArrayType CHUNK_ARRAY=CHUNK.arrays();

    //DEPRECATED_MATERIAL
    @NotNull DeprecatedMaterialType DEPRECATED_MATERIAL=new DeprecatedMaterialType();
    @NotNull DeprecatedMaterialType.SetType DEPRECATED_MATERIAL_SET=DEPRECATED_MATERIAL.sets();
    @NotNull DeprecatedMaterialType.LinkedSetType DEPRECATED_MATERIAL_LINKED_SET=DEPRECATED_MATERIAL.linked_sets();
    @NotNull DeprecatedMaterialType.ListType DEPRECATED_MATERIAL_LIST=DEPRECATED_MATERIAL.lists();
    @NotNull DeprecatedMaterialType.ArrayType DEPRECATED_MATERIAL_ARRAY=DEPRECATED_MATERIAL.arrays();

    //MATERIAL
    @NotNull MaterialType MATERIAL=new MaterialType();
    @NotNull MaterialType.SetType MATERIAL_SET=MATERIAL.sets();
    @NotNull MaterialType.LinkedSetType MATERIAL_LINKED_SET=MATERIAL.linked_sets();
    @NotNull MaterialType.ListType MATERIAL_LIST=MATERIAL.lists();
    @NotNull MaterialType.ArrayType MATERIAL_ARRAY=MATERIAL.arrays();

    //LOCATION
    @NotNull LocationType LOCATION=new LocationType();
    @NotNull LocationType.SetType LOCATION_SET=LOCATION.sets();
    @NotNull LocationType.LinkedSetType LOCATION_LINKED_SET=LOCATION.linked_sets();
    @NotNull LocationType.ListType LOCATION_LIST=LOCATION.lists();
    @NotNull LocationType.ArrayType LOCATION_ARRAY=LOCATION.arrays();

    //ITEM_STACK
    @NotNull ItemStackType ITEMSTACK=new ItemStackType();
    @NotNull ItemStackType.SetType ITEMSTACK_SET=ITEMSTACK.sets();
    @NotNull ItemStackType.LinkedSetType ITEMSTACK_LINKED_SET=ITEMSTACK.linked_sets();
    @NotNull ItemStackType.ListType ITEMSTACK_LIST=ITEMSTACK.lists();
    @NotNull ItemStackType.ArrayType ITEMSTACK_ARRAY=ITEMSTACK.arrays();

    //INVENTORY
    @NotNull InventoryType INVENTORY=new InventoryType();
    @NotNull InventoryType.SetType INVENTORY_SET=INVENTORY.sets();
    @NotNull InventoryType.LinkedSetType INVENTORY_LINKED_SET=INVENTORY.linked_sets();
    @NotNull InventoryType.ListType INVENTORY_LIST=INVENTORY.lists();
    @NotNull InventoryType.ArrayType INVENTORY_ARRAY=INVENTORY.arrays();

    //VECTOR
    @NotNull VectorType VECTOR=new VectorType();
    @NotNull VectorType.SetType VECTOR_SET=VECTOR.sets();
    @NotNull VectorType.LinkedSetType VECTOR_LINKED_SET=VECTOR.linked_sets();
    @NotNull VectorType.ListType VECTOR_LIST=VECTOR.lists();
    @NotNull VectorType.ArrayType VECTOR_ARRAY=VECTOR.arrays();

    //VECTOR
    @NotNull BoundingBoxType BOUNDING_BOX=new BoundingBoxType();
    @NotNull BoundingBoxType.SetType BOUNDING_BOX_SET=BOUNDING_BOX.sets();
    @NotNull BoundingBoxType.LinkedSetType BOUNDING_BOX_LINKED_SET=BOUNDING_BOX.linked_sets();
    @NotNull BoundingBoxType.ListType BOUNDING_BOX_LIST=BOUNDING_BOX.lists();
    @NotNull BoundingBoxType.ArrayType BOUNDING_BOX_ARRAY=BOUNDING_BOX.arrays();
}
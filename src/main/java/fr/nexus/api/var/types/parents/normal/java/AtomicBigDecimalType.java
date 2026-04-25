package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import fr.nexus.api.var.types.parents.normal.big.BigDecimalType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class AtomicBigDecimalType extends InternalVarType<AtomicReference<BigDecimal>> {

    // On crée une instance locale privée pour éviter de dépendre de VarTypes
    private static final BigDecimalType DELEGATE = new BigDecimalType();

    @Override
    public byte @NotNull [] serializeSync(@NotNull AtomicReference<BigDecimal> value) {
        // On utilise l'instance locale DELEGATE au lieu de VarTypes.BIGDECIMAL
        return DELEGATE.serializeSync(value.get());
    }

    @Override
    public @NotNull AtomicReference<BigDecimal> deserializeSync(int version, byte[] bytes) {
        final BigDecimal decimal = DELEGATE.deserializeSync(version, bytes);
        return new AtomicReference<>(decimal);
    }
}
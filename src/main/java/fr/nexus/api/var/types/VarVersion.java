package fr.nexus.api.var.types;

import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.api.var.types.parents.normal.VarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarVersion implements Vars{
    private final int version;

    public VarVersion(int version){
        this.version=version;
    }

    public int getVersion(){
        return this.version;
    }

    protected @NotNull VarType.VersionAndRemainder readVersionAndRemainder(byte[]bytes){
        if(bytes==null||bytes.length==0)
            throw new IllegalArgumentException("Cannot read version: empty byte array");

        int value=0;
        int position=0;
        int index=0;

        while(true){
            if(index>=bytes.length)
                throw new IllegalArgumentException("Invalid VarInt: truncated");

            final byte b=bytes[index];
            value|=(b&0x7F)<<position;

            if((b&0x80)==0)
                break;

            position+=7;
            index++;

            if(position>=32)
                throw new RuntimeException("VarInt too long");
        }

        index++;

        final byte[]remainder;
        if(index>=bytes.length)
            remainder=new byte[0];
        else {
            remainder=new byte[bytes.length-index];
            System.arraycopy(bytes,index,remainder,0,remainder.length);
        }

        return new VarType.VersionAndRemainder(value,remainder);
    }

    protected byte[]addVersionToBytes(byte[] bytes) {
        if(bytes==null)bytes=new byte[0];

        final byte[]versionBytes=IntegerType.toVarInt(version);

        final byte[]result=new byte[versionBytes.length + bytes.length];

        System.arraycopy(versionBytes,0,result,0,versionBytes.length);

        System.arraycopy(bytes,0,result,versionBytes.length,bytes.length);

        return result;
    }

    protected @NotNull UnsupportedOperationException createUnsupportedVersionException(int unsupportedVersion) {
        return new UnsupportedOperationException("unsupported version for type: '" +
                getStringType() + "' | unsupported_version: '" +
                unsupportedVersion + "' | current_version: '" +
                getVersion() + "'");
    }

    public record VersionAndRemainder(int version,byte[]remainder){}
}

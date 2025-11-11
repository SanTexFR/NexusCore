package fr.nexus.var.types;

import fr.nexus.var.types.parents.Vars;
import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarVersion implements Vars{
    //VARIABLES (INSTANCES)
    private final int version;

    //CONSTRUCTOR
    public VarVersion(int version){
        this.version=version;
    }


    //METHODS

    //VERSION
    public int getVersion(){
        return this.version;
    }

    //DER
    protected@NotNull VarType.VersionAndRemainder readVersionAndRemainder(byte[]bytes){
        final int version=((bytes[0]&0xFF)<<24)|
                ((bytes[1]&0xFF)<<16)|
                ((bytes[2]&0xFF)<<8) |
                (bytes[3]&0xFF);

        return new VarType.VersionAndRemainder(version,Arrays.copyOfRange(bytes,Integer.BYTES,bytes.length));
    }
    public record VersionAndRemainder(int version,byte[]remainder){}

    //SER
    protected byte[]addVersionToBytes(byte[]bytes){
        final int version=getVersion();
        final byte[]result=new byte[Integer.BYTES+bytes.length];

        result[0]=(byte)(version>>>24);
        result[1]=(byte)(version>>>16);
        result[2]=(byte)(version>>>8);
        result[3]=(byte)(version);

        System.arraycopy(bytes,0,result,Integer.BYTES,bytes.length);

        return result;
    }

    //THROW
    protected@NotNull UnsupportedOperationException createUnsupportedVersionException(int unsupportedVersion){
        return new UnsupportedOperationException("unsupported version for type: '"+
                getStringType()+"' | unsupported_version: '"+
                unsupportedVersion+"' | current_version: '"+
                getVersion()+"'");
    }
}
package dev.edigonzales;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.wololo.flatgeobuf.generated.Header;

import com.google.flatbuffers.ByteBufferUtil;

import static com.google.flatbuffers.Constants.SIZE_PREFIX_LENGTH;

public class HeaderMeta {
    public String name;
    public byte geometryType;
    public int srid;
    public Envelope envelope;
    public long featuresCount;
    public boolean hasZ = false;
    public boolean hasM = false;
    public boolean hasT = false;
    public boolean hasTM = false;
    public int indexNodeSize;
//    public List<ColumnMeta> columns;
    public int offset;

    
    public static HeaderMeta read(ByteBuffer bb) throws IOException {        
        int offset = 0;
        if (!Constants.isFlatgeobuf(bb))
            throw new IOException("This is not a flatgeobuf!");
        bb.position(offset += Constants.MAGIC_BYTES.length);
        int headerSize = ByteBufferUtil.getSizePrefix(bb);
        bb.position(offset += SIZE_PREFIX_LENGTH);        
        Header header = Header.getRootAsHeader(bb);
        System.out.println("headerSize: " + headerSize);
//        bb.position(offset += headerSize); // Mein ByteBuffer ist nicht das ganze File, sondern das Resultat eine Range Requests.
        int geometryType = header.geometryType();

        HeaderMeta headerMeta = new HeaderMeta();

        headerMeta.featuresCount = header.featuresCount();
        headerMeta.indexNodeSize = header.indexNodeSize();

        return null;
    }
}

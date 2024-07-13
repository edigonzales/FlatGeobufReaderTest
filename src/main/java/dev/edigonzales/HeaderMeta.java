package dev.edigonzales;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Header;

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
    public List<ColumnMeta> columns;
    public int offset;

    
    public static HeaderMeta read(ByteBuffer bb) throws IOException {                
        Header header = Header.getRootAsHeader(bb);
        
        int geometryType = header.geometryType();

        HeaderMeta headerMeta = new HeaderMeta();
        headerMeta.featuresCount = header.featuresCount();
        headerMeta.indexNodeSize = header.indexNodeSize();

        int columnsLength = header.columnsLength();
        ArrayList<ColumnMeta> columnMetas = new ArrayList<ColumnMeta>();
        for (int i = 0; i < columnsLength; i++) {
            ColumnMeta columnMeta = new ColumnMeta();
            columnMeta.name = header.columns(i).name();
            columnMeta.type = (byte) header.columns(i).type();
            columnMeta.title = header.columns(i).title();
            columnMeta.description = header.columns(i).description();
            columnMeta.width = header.columns(i).width();
            columnMeta.precision = header.columns(i).precision();
            columnMeta.scale = header.columns(i).scale();
            columnMeta.nullable = header.columns(i).nullable();
            columnMeta.unique = header.columns(i).unique();
            columnMeta.nullable = header.columns(i).nullable();
            columnMeta.primary_key = header.columns(i).primaryKey();
            columnMeta.metadata = header.columns(i).metadata();
            columnMetas.add(columnMeta);
        }
        
        Crs crs = header.crs();
        if (crs != null && crs.code() != 0)
            headerMeta.srid = crs.code();
        if (header.envelopeLength() == 4) {
            double minX = header.envelope(0);
            double minY = header.envelope(1);
            double maxX = header.envelope(2);
            double maxY = header.envelope(3);
            headerMeta.envelope = new Envelope(minX, maxX, minY, maxY);
        }

        headerMeta.columns = columnMetas;
        headerMeta.geometryType = (byte) geometryType;
//        headerMeta.offset = offset;

        return headerMeta;
    }
}

package dev.edigonzales;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
//import org.wololo.flatgeobuf.ColumnMeta;
//import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.Feature;

import dev.edigonzales.PackedRTree.IndexItem;

import static java.lang.System.*;

public class FlatGeobufReader {
        
    public void read(String fileUrl, Envelope bbox, HeaderMeta headerMeta) throws IOException {
        out.println("headerMeta.indexNodeSize: " + headerMeta.indexNodeSize);
        out.println("headerMeta.offset: " + headerMeta.offset);
        
        long indexLength = PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);
        out.println("indexLength: " + indexLength);

        ByteBuffer bb = null;

        bb = fetchByteRange(fileUrl, headerMeta.offset, headerMeta.offset + ((int) indexLength) - 1);
        out.println(bb);
        
        List<IndexItem> indexItems = PackedRTree.getIndex(bb, 0, (int) headerMeta.featuresCount, headerMeta.indexNodeSize, bbox);
        for (IndexItem indexItem : indexItems) {
            out.println("index: " + indexItem.index + " --- relativeOffset: " + indexItem.offset);
        }
        
        // Test: Erstes Feature lesen
        int startByte = headerMeta.offset + (int) indexLength;
        int endByte = startByte + 2240 - 1;
        out.println(startByte + " - " + endByte);
        
        for (int i=-15; i<=15; i++) {
//            out.println("i: " + i);
            for (int j=-15; j<=15; j++) {
//                out.println("j: " + j);
                
                bb = fetchByteRange(fileUrl, startByte+i, endByte+j);
//                out.println(bb);
                
                try {
                    Feature feature = Feature.getRootAsFeature(bb);  
                    if (feature.geometry() != null) {
                        out.println(i + " " + j);
                      out.println("feature: " + feature.geometry());

                    }
                } catch (Exception e) {
                }
            }                        
        }
    }
    
    public HeaderMeta getHeaderMeta(String fileUrl) throws MalformedURLException, IOException {        
        ByteBuffer bb = null;
        
        // Magic Bytes und Header Length Prefix anfordern.
        bb = fetchByteRange(fileUrl, 0, 8 + 4 - 1);
        
        // Magic Bytes prüfen, ob korrekt.
        if (!Constants.isFlatgeobuf(bb)) {
            throw new IOException("This is not a flatgeobuf!");
        } 

        // Length Prefix des Header folgt nach den Magic Bytes an Position 8.
        bb.position(8);
        int headerLength = bb.asIntBuffer().get();
                
        bb.clear();
        
        // Header anfordern
        bb = fetchByteRange(fileUrl, 12, 12 + headerLength - 1);
        HeaderMeta headerMeta = HeaderMeta.read(bb);
        
        // 8 + 4 + headerLength = Start von Index 
        headerMeta.offset = 12 + headerLength;
                
        return headerMeta;
    }
    
    private static ByteBuffer fetchByteRange(String url, int startByte, int endByte) throws IOException {
        byte[] buffer = new byte[endByte - startByte + 1]; 
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            inputStream.read(buffer, 0, buffer.length); // Achtung: 0, relativ zum Byte Range, nicht absolut.
            inputStream.close();
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            return bb;            
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        } 
    }
}

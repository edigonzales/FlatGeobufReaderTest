package dev.edigonzales;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.locationtech.jts.geom.Envelope;
//import org.wololo.flatgeobuf.ColumnMeta;
//import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.PackedRTree.SearchHit;
import org.wololo.flatgeobuf.PackedRTree.SearchResult;
import org.wololo.flatgeobuf.generated.Feature;

import com.google.common.io.LittleEndianDataInputStream;

import static com.google.flatbuffers.Constants.SIZE_PREFIX_LENGTH;

public class FlatGeobufReader {
    
    String url = "https://stac.sogeo.services/files/test/zuerihaus.fgb";
//    String url = "https://stac.sogeo.services/files/test/mopublic_bodenbedeckung.fgb";
//    int startByte = 0;
//    int endByte = 200; 
    double minX = 2607882; 
    double minY = 1228246;
//    double maxX = 2607924; // 6 Feature
//    double maxY = 1228311;
    double maxX = 2607891; // 1 Feature
    double maxY = 1228251;

//    double maxX = 2607904;
//    double maxY = 1228491;
    
    
    Envelope bbox = new Envelope(minX, maxX, minY, maxY);


    public void makeItSo() throws MalformedURLException, IOException {
        
//        InputStream inputStream = new URL(url).openStream(); // Achtung: macht 200 und lädt alles runter... 
//
//        HeaderMeta headerMeta = HeaderMeta.read(inputStream);
//        System.err.println(headerMeta.indexNodeSize);
//        System.err.println(headerMeta.name);
//        System.err.println(headerMeta.featuresCount);
//        System.err.println(headerMeta.envelope);
//        System.err.println(headerMeta.srid);
//        System.err.println(headerMeta.offset);
//        System.err.println(headerMeta.columns);
//        
////        for (ColumnMeta columnMeta : headerMeta.columns) {
////            System.err.println(columnMeta.name);            
////        }
//        
//        System.err.println("----------------------------");
//        
//        LittleEndianDataInputStream data = new LittleEndianDataInputStream(inputStream);
//        SearchResult searchResult = PackedRTree.search(data, headerMeta.offset, (int) headerMeta.featuresCount, headerMeta.indexNodeSize, bbox);
//        System.err.println("Anzahl Resultate: " +searchResult.hits.size());
//        System.err.println("Position ?: " + searchResult.pos);
//        
//        for (SearchHit hit : searchResult.hits) {
//            System.err.println("index: " + hit.index);
//            System.err.println("offset: " + hit.offset);
//            
//            
//        }
        
        
        byte[] magicbytes = new byte[8];

        ByteBuffer bb = fetchByteRange(url, 0, 7).get();
        System.err.println(new BigInteger(1, bb.array()).toString(16));
        
        ByteBuffer bb2 = fetchByteRange(url, 8, 11).get();
        bb2.order(ByteOrder.LITTLE_ENDIAN);
//        System.err.println(new BigInteger(1, bb2.array()).toString(16));
//        
//        System.err.println(bb2.getInt()); // 17
        int indexLength = bb2.getInt();
        
//        String hex = new BigInteger(1, bb2.array()).toString(16);
//        int decimalValue = Integer.parseInt(hex, 16);
//        System.err.println(decimalValue);

        // 0 weil read(bb) prüft, ob MagicBytes vorhanden sind.
        ByteBuffer bb3 = fetchByteRange(url, 0, indexLength).get();
        bb3.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("position: " + bb3.position());

        HeaderMeta headerMeta = HeaderMeta.read(bb3);
//        System.err.println(headerMeta.indexNodeSize);
//        System.err.println(headerMeta.name);
//        System.err.println(headerMeta.featuresCount);
//        System.err.println(headerMeta.envelope);
//        System.err.println(headerMeta.srid);
//        System.err.println(headerMeta.offset);
//        System.err.println(headerMeta.columns);
        
        // Grösster Problem: Wie finde ich die Grösser des Features heraus ohne es herunterzuladen?
        // - Man müsste pro gruppierte Feature (sortiert nach index) immer ein den offset des +1 Feature herausfinden.
        // Noch grösseres Problem ist allerdings, das bereits der Suchindex nicht mit partial requests funktioniert.
        // -> Ah doch, er kann auch Bytebuffer. Müsste man wohl trotzdem umschreiben
        
        
        
//        int featureSize = data.readInt();
//        System.err.println("featureSize: " + featureSize);
//        
//        byte[] bytes = new byte[featureSize];
//        data.readFully(bytes);
//        ByteBuffer bb = ByteBuffer.wrap(bytes);
//        Feature feature = Feature.getRootAsFeature(bb);
//        System.err.println(feature.columnsLength());
        

//        // Geotools
//        int treeSize = getTreeSize(headerMeta);
//        System.err.println("treeSize: " + treeSize);
//        
//        int featuresOffset = headerMeta.offset + treeSize;
//        System.err.println("featuresOffset: " + featuresOffset);
//        
//        LittleEndianDataInputStream data = new LittleEndianDataInputStream(inputStream);
//        Iterable<?> iterable;
//
//        if (headerMeta.indexNodeSize > 1) {
//            SearchResult result =
//                    PackedRTree.search(
//                            data,
//                            headerMeta.offset,
//                            (int) headerMeta.featuresCount,
//                            headerMeta.indexNodeSize,
//                            bbox);
//            int skip = treeSize - result.pos;
//            if (skip > 0) skipNBytes(data, treeSize - result.pos);
//            
//            System.out.println(result.hits.size());
//            
//            
//            
////            iterable = new ReadHitsIterable(fb, result.hits, headerMeta, featuresOffset, data);
//        } else {
////            iterable = new ReadAllInterable(headerMeta, data, fb, 0);
//        }

        
        
//        inputStream.reset();
        
//        public static void skipNBytes(InputStream stream, long skip) throws IOException {
//            long actual = 0;
//            long remaining = skip;
//            while (actual < remaining) remaining -= stream.skip(remaining);
//        }

        
    }

    public static void skipNBytes(InputStream stream, long skip) throws IOException {
        long actual = 0;
        long remaining = skip;
        while (actual < remaining)
            remaining -= stream.skip(remaining);
    }

    private static int getTreeSize(HeaderMeta headerMeta) {
        int treeSize =
                headerMeta.featuresCount > 0 && headerMeta.indexNodeSize > 0
                        ? (int)
                                PackedRTree.calcSize(
                                        (int) headerMeta.featuresCount, headerMeta.indexNodeSize)
                        : 0;
        return treeSize;
    }

    
//    private static Optional<ByteBuffer> fetchSpatialIndex(String url) {
//        int headerSize = 1024; // Assuming the header size is 1024 bytes, adjust if needed
//
//        // Fetch the header to determine the spatial index location and size
//        Optional<ByteBuffer> headerBuffer = fetchByteRange(url, 0, headerSize - 1);
//        if (headerBuffer.isPresent()) {
//            return Optional.empty();
//        }
//
//        ByteBuffer header = headerBuffer.get();
//
//        // Parse the header to find the spatial index location and size
//        // This part of the code depends on the FlatGeobuf file structure
//        // For demonstration purposes, assuming the spatial index starts at byte 1024 and its size is 4096 bytes
//        int indexStart = 1024; // Adjust based on the actual header structure
//        int indexSize = 4096; // Adjust based on the actual header structure
//
//        // Fetch the spatial index
//        return fetchByteRange(url, indexStart, indexStart + indexSize - 1);
//    }

 
    
    private static Optional<ByteBuffer> fetchByteRange(String url, int startByte, int endByte) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            // Set the Range header to fetch the specific byte range
            httpGet.setHeader("Range", "bytes=" + startByte + "-" + endByte);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getCode() == 206) { // HTTP 206 Partial Content
                    ReadableByteChannel readableByteChannel = Channels.newChannel(response.getEntity().getContent());
                    ByteBuffer byteBuffer = ByteBuffer.allocate((endByte - startByte) + 1);
                    //ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    System.out.println(endByte);
                    System.out.println(startByte);
                    
                    System.out.println("byteBuffer: " + byteBuffer);
                    
                    readableByteChannel.read(byteBuffer);
                    byteBuffer.flip();
                    return Optional.of(byteBuffer);
                } else {
                    System.err.println("Failed to fetch byte range: " + EntityUtils.toString(response.getEntity()));
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

}

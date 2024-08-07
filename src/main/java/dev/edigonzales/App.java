/*
 * This source file was generated by the Gradle 'init' task
 */
package dev.edigonzales;

import java.io.IOException;
import java.net.MalformedURLException;

import org.locationtech.jts.geom.Envelope;

public class App {

    public static void main(String[] args) throws MalformedURLException, IOException {
        
        
        String fileUrl = "https://stac.sogeo.services/files/test/zuerihaus.fgb";
//        String url = "https://stac.sogeo.services/files/test/mopublic_bodenbedeckung.fgb";

        FlatGeobufReader fgbReader = new FlatGeobufReader();
        HeaderMeta headerMeta = fgbReader.getHeaderMeta(fileUrl);
        
        
        double minX = 2607882; 
        double minY = 1228246;
//        double maxX = 2607924; // 6 Feature
//        double maxY = 1228311;
        double maxX = 2607891; // 1 Feature
        double maxY = 1228251;

//        double maxX = 2607904; // Viele Feature schmalles, hohes Rechteck (wegen index-Nummern, die nicht nachfolgend sind)
//        double maxY = 1228491;
        
        Envelope bbox = new Envelope(minX, maxX, minY, maxY);
        
        fgbReader.read(fileUrl, bbox, headerMeta);
        
    }
}

package aanguita.jacuzzi.io.xml.test;

import aanguita.jacuzzi.hash.CRCMismatchException;
import aanguita.jacuzzi.io.xml.XMLReader;
import aanguita.jacuzzi.io.xml.XMLWriter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Created by Alberto on 24/12/2015.
 */
public class testXML2 {

    private static void write(
            String peerID,
            int localPort,
            int externalPort,
            String ownNick,
            Integer maxDownloadSpeed,
            Integer maxUploadSpeed,
            String tempDownloadsPath,
            String basedDataPath) throws IOException, XMLStreamException {
        XMLWriter xmlWriter = new XMLWriter("config");
        xmlWriter.addField("peer-id", peerID);
        xmlWriter.addField("port", localPort);
        xmlWriter.addField("external-port", externalPort);
        xmlWriter.addField("nick", ownNick);

        xmlWriter.beginStruct("friend-peers");
        xmlWriter.endStruct();
        xmlWriter.beginStruct("blocked-peers");
        xmlWriter.endStruct();

        xmlWriter.addField("max-download-speed", maxDownloadSpeed);
        xmlWriter.addField("max-upload-speed", maxUploadSpeed);

        xmlWriter.addField("temp-downloads-path", tempDownloadsPath);
        xmlWriter.addField("base-data-path", basedDataPath);

        xmlWriter.write("config.xml", 8);
    }

    public static void main(String[] args) throws IOException, XMLStreamException, CRCMismatchException {

        write(
                "c8818z0d1wAGxAy6ZMQ10g4500e20gY0c8812wa20g1",
                0,
                37720,
                "alb",
                null,
                null,
                ".\\etc\\user_0\\temp",
                ".\\etc\\user_0\\downloads"
        );

        XMLReader xmlReader = new XMLReader("config.xml", true);

        System.out.println("end");
    }
}

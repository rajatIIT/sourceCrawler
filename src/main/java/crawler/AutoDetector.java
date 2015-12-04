package crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.xml.sax.SAXException;

public class AutoDetector {

    public AutoDetector(String[] args) throws FileNotFoundException, IOException, TikaException, SAXException {
        File directoryPath = new File(args[0]);
        FilenameFilter dsStorefilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.equals(".DS_Store"))
                    return false;
                else
                    return true;
            }
        };

        File[] allFiles = directoryPath.listFiles(dsStorefilter);

        for (File singleDirectory : allFiles) {

            System.out.println(singleDirectory.getName());

            File[] allHtmlFiles = singleDirectory.listFiles(dsStorefilter);

            for (File singleHTMLFile : allHtmlFiles) {

                AutoDetectParser myAutoDetectParser = new AutoDetectParser();
                
                LinkContentHandler myLinkContentHandler = new LinkContentHandler();
                
                Metadata inputMetadata = new Metadata();
                
                myAutoDetectParser.parse(new FileInputStream(singleHTMLFile), myLinkContentHandler, inputMetadata);
                
                List<Link> linksList = myLinkContentHandler.getLinks();
                
                Iterator<Link> listIterator = linksList.iterator();
                
                while(listIterator.hasNext()){
                    System.out.println(listIterator.next().getText());
                }
                
                String[] propertyNames = inputMetadata.names();
                
                for(String propertyName:propertyNames) {
                    System.out.print(propertyName + " : ");
                    System.out.println(inputMetadata.get(propertyName) + "\n");
                }
            }

        }

    }

}
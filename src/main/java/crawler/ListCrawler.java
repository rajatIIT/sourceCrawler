package crawler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.xml.sax.SAXException;

public class ListCrawler implements Crawler {

    BucketWriter myWriter;

    public ListCrawler(String bucketName, String credentialsFilePath) {
        myWriter = new BucketWriter(bucketName, credentialsFilePath);
    }

    public void crawl(URLSource source, Path OutputDirectory) {

        while (source.hasNext()) {

            URL nextURL = source.nextURL();

            System.out.println("Write URL to Bucket : " + nextURL.toString());

            // crawl this URL
            /*
             * String pageSource = getSource(nextURL);
             * 
             * String hostDirectory = OutputDirectory.toString() +
             * File.separator + nextURL.getHost();
             * 
             * if ((new File(hostDirectory)).exists()) (new
             * File(hostDirectory)).mkdir();
             */
            String newFileName = System.currentTimeMillis() + "-" + nextURL.hashCode();

            // store the inputStream to a String.

            // open an inputStream from the String and pass that to the
            // bucketWriter

            // open an inputStream from the String and update the hashMap of
            // hosts.

            StringBuilder sourceBuilder = new StringBuilder();

            try {
                InputStream urlInputStream;
                urlInputStream = nextURL.openConnection().getInputStream();
                int next = urlInputStream.read();
                while (next != -1) {
                    sourceBuilder.append((char) next);
                    next = urlInputStream.read();
                }
                urlInputStream.close();
                String urlSourceString = sourceBuilder.toString();
                // System.out.println(urlSourceString);

                InputStream pageSource = new ByteArrayInputStream(
                        urlSourceString.getBytes(StandardCharsets.UTF_8));

                if (DomainStore.storeDomain) {
                    // perform link analysis too!
                    System.out.println("Perform link analysis for " + nextURL.toString());
                    AutoDetectParser myAutoDetectParser = new AutoDetectParser();

                    LinkContentHandler myLinkContentHandler = new LinkContentHandler();

                    Metadata inputMetadata = new Metadata();

                    InputStream linkAnalysisInputStream = new ByteArrayInputStream(
                            urlSourceString.getBytes(StandardCharsets.UTF_8));
                    UrlValidator validator = new UrlValidator();

                    try {
                        myAutoDetectParser.parse(linkAnalysisInputStream, myLinkContentHandler,
                                inputMetadata);
                        List<Link> linksList = myLinkContentHandler.getLinks();
                        Iterator<Link> listIterator = linksList.iterator();
                 //       System.out.println("Found Hosts: ");
                        while (listIterator.hasNext()) {
                            // String nextLink = listIterator.next().getText();
                            String nextLink = listIterator.next().getUri();
                            
                            if (validator.isValid(nextLink)) {
                            //    System.out.println("Allowed: " +  nextLink);
                                URL nextLinkURL = new URL(nextLink);
                                //System.out.print(" " + nextLinkURL.getHost() + " ");
                   //             System.out.println("Host: " + nextLinkURL.getHost());
                                Main.domainStore.addDomain(nextLinkURL.getHost());
                            }
                        }

                    } catch (SAXException e) {
                        System.out.println("SAX Exception while handling links!");
                        e.printStackTrace();
                    } catch (TikaException e) {
                        System.out.println("Tika Exception while handling links!");
                        e.printStackTrace();
                    }

                }

                // myWriter.write(newFileName,nextURL.openConnection().getInputStream());
                myWriter.write(newFileName, pageSource);

            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
             * File newPage = new File(newFileName);
             * 
             * try {
             * 
             * // should be write to Bucket writeToFile(pageSource,newPage); }
             * catch (FileNotFoundException e) { e.printStackTrace(); }
             */
        }
    }

    private String getSource(URL next) {
        return null;
    }

}

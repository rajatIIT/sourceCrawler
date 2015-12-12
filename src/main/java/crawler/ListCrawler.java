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

import links.LinkManager;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.xml.sax.SAXException;

import com.amazonaws.auth.AWSCredentials;

/**
 * A basic crawler that is supposed to crawl the URLs in a given list .
 * 
 * TODO : Create a crawler that crawls and methodically throws the links to a
 * bucket.
 * 
 * @author rajatpawar
 *
 */
public class ListCrawler implements Crawler {

    BucketWriter myWriter;
    static boolean throwSourceToBucket, throwLinksToBucket;

    public ListCrawler(String bucketName, String credentialsFilePath) {
        myWriter = new BucketWriter(bucketName, credentialsFilePath);
        setLinksOnlyCrawler();
    }

    public void setSourceOnlyCrawler() {
        throwSourceToBucket = true;
        throwLinksToBucket = false;
    }

    public void setLinksOnlyCrawler() {
        throwSourceToBucket = false;
        throwLinksToBucket = true;
    }

    public void setSourceAndLinksCrawler() {
        throwSourceToBucket = true;
        throwLinksToBucket = true;
    }

    /**
     * 
     * Crawl the URL and throw the URL to the bucket. The basic task of the
     * crawler.
     * 
     */
    public void crawl(URLSource source, Path OutputDirectory) {
        int crawlCount=0;
        LinkManager linkManager;
        linkManager = new LinkManager(Main.credentials);
        

        // source is a SET of URLs : while loop runs for each URL
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

            // CRAWL THE SOURCE
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

                if (throwSourceToBucket) {
                    InputStream pageSource = new ByteArrayInputStream(
                            urlSourceString.getBytes(StandardCharsets.UTF_8));
                    myWriter.write(newFileName, pageSource);
                }
                
                
                
                

                if (throwLinksToBucket) {
                    // perform link analysis too!
                    
                    
                    System.out.println("Perform link analysis for " + nextURL.toString());
                    System.out.println("Links Found: ");
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
                        while (listIterator.hasNext()) {
                            String nextLink = listIterator.next().getUri();
                            if (validator.isValid(nextLink)) {
                                linkManager.handleLink(nextLink);
                            }
                        }
                        
                        crawlCount++;
                        System.out.println("=====================================");
                        System.out.println("Link extraction complete for " + crawlCount + " links from current list.");
                        System.out.println("=====================================");
                    } catch (SAXException e) {
                        System.out.println("SAX Exception while handling links!");
                        e.printStackTrace();
                    } catch (TikaException e) {
                        System.out.println("Tika Exception while handling links!");
                        e.printStackTrace();
                    }

                }


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

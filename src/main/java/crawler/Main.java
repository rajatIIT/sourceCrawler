package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;


/**
 * Manages creation of crawler threads .Each crawler thread :
 * 1)  continuously withdraws lists of links from S3
 * 2)  crawls the links and extracts the links form those web pages.
 * 3)  Aggregates the extracted links and throws them back to S3.  
 * 
 * @author rajatpawar
 *
 */
public class Main {

    // whether to store the hosts in all links in the pages.

    static DomainStore domainStore;
    static PropertiesFileCredentialsProvider initialCredentials;
    static BasicAWSCredentials credentials;
    private static String bucketName;
    private static Properties myProperties;

    public static void main(String[] args) throws IOException {

        // args[0] : File that contains URLs (if manual mode used [non S3])
        // args[1] : Name of Bucket. 
        // args[2] : Credentaiols file path 



        bucketName = args[1];
        initialCredentials = new PropertiesFileCredentialsProvider(args[2]);
        // fetch credentilas from the remote thing. 
        loadProperties();
     //   System.out.println("Initiate crawler with " + myProperties.getProperty("accessKey") + " and " +  myProperties.getProperty("secretKey"));
        credentials = new BasicAWSCredentials(myProperties.getProperty("accessKey"), myProperties.getProperty("secretKey"));
        
        System.out.println("Crawler count is " + myProperties.getProperty("url_count"));
        
        
        
        // get credentials from bucket .
        
        
        // if the size of the queued directory is url_count/1000, then stop this crawler.
        // when the CPU Utilization is very high, stop spawning new threads. 
        // while( the size of queued is less than target) 
        
        
        
        
        int directorySizeGoal = Integer.parseInt(myProperties.getProperty("url_count")) /100;
        System.out.println("We want a directory which has " + directorySizeGoal + " size. ");
        CrawlerThread thread = new CrawlerThread(args, directorySizeGoal);
        thread.start();

        /*
         * if(args[3].equals("true")){ domainStore = new DomainStore();
         * DomainStore.storeDomain=true; } else { DomainStore.storeDomain=false;
         * }
         * 
         * bucketName = args[1];
         * 
         * credentials = new PropertiesFileCredentialsProvider(args[2]);
         * 
         * ListCrawler myCrawler = new ListCrawler(args[1],args[2]);
         * 
         * List<URL> urlList = Utils.getURLListFromFile(args[0]);
         * 
         * URLSource listSource = new URLSource(urlList);
         * 
         * myCrawler.crawl(listSource, Paths.get(args[1]));
         * 
         * // let args[4] be the domain outputpath.
         * 
         * if(DomainStore.storeDomain) writeDomainParams(args[4]);
         */

    }

    public static void loadProperties() {
        try {
            
            myProperties = new Properties();
            AmazonS3Client client = new AmazonS3Client(initialCredentials);
            
            // get an inputStream for the properties file
            S3Object propertiesObject = client.getObject(bucketName, "Properties" + "/"
                    + "crawler.properties");
            myProperties.load(propertiesObject.getObjectContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeDomainParams(String path) {

        try {
            File domainParamFile = new File(path);
            domainParamFile.createNewFile();
            FileWriter myWriter = new FileWriter(domainParamFile);

            Iterator<String> domainIterator = domainStore.domainMap.keySet().iterator();
            while (domainIterator.hasNext()) {
                String nextVal = domainIterator.next();
                myWriter.write(nextVal + " " + domainStore.domainMap.get(nextVal));
                myWriter.write("\n");
            }
            myWriter.flush();
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getBucketName() {
        return bucketName;
    }

}

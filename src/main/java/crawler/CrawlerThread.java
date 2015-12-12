package crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class CrawlerThread extends Thread {

    List<URL> leftURLs;
    AmazonS3Client threadClient;
    String bucketName;
    String[] args;
    String processedDirectoryName;
    int targetSize;

    public CrawlerThread(String[] args, int targetSize) {
        this.args = args;
        processedDirectoryName = "Processed";
        bucketName = args[1];
        //AWSCredentialsProvider credentials = new PropertiesFileCredentialsProvider(args[2]);
       // AWSCredentialsProvider credentials = Main.credentials;
        
        AWSCredentials credentials = Main.credentials;
        threadClient = new AmazonS3Client(credentials);
        leftURLs = new LinkedList<URL>();
        this.targetSize=targetSize;
    }
    

    public void run() {

        // execute a crawler
        // download the list of the objects in the Queue folder.
        // basically continue until there are not URLs in the Queued directory.

        downloadMoreURLs();

        //while (!leftURLs.isEmpty()) {
        
        while (getDirectorySize() < targetSize) {

            // crawl all the URLs and throw the results to the specified output
            // directory

            bucketName = args[1];

            // AWSCredentialsProvider credentials = new
            // PropertiesFileCredentialsProvider(args[2]);

            ListCrawler myCrawler = new ListCrawler(args[1], args[2]);

            List<URL> urlList;

           // try {
              //  urlList = Utils.getURLListFromFile(args[0]);
               // URLSource listSource = new URLSource(urlList);
                URLSource listSource = new URLSource(leftURLs);
                myCrawler.crawl(listSource, Paths.get(args[1]));
            /*} catch (IOException e) {
                e.printStackTrace();
            } */

            if (leftURLs.isEmpty())
                downloadMoreURLs();
        }

    }

    private int getDirectorySize() {
        int num = 0;
        ObjectListing listing = threadClient.listObjects(bucketName, "Queued" + "/" + "list");
        num = listing.getObjectSummaries().size();

        boolean hasMore = listing.isTruncated();
        while (hasMore) {
            listing = threadClient.listNextBatchOfObjects(listing);
            num = num + listing.getObjectSummaries().size();
            hasMore = listing.isTruncated();
        }
        System.out.println("Current Directory size is " + num);
        return num;
    }

    /**
     * Downlaod
     */
    public void downloadMoreURLs() {

        // create a client, get inputStream and using scanner
        // keep on fetching new URLs

        System.out.println("Download more URLs");
        ObjectListing queueListing = threadClient.listObjects(bucketName, "Queue");

        List<S3ObjectSummary> objectSummary = queueListing.getObjectSummaries();

        if (queueListing.getObjectSummaries().size() > 0) {

            // read the object into a String.
            String nextKey = objectSummary.get(0).getKey();

            if (!nextKey.equals("Queued/.DS_Store")) {
                System.out.println("Download " + nextKey);

                InputStream nextObjectInputStream = threadClient.getObject(bucketName, nextKey)
                        .getObjectContent();

                Scanner inputStreamScanner = new Scanner(nextObjectInputStream);

                while (inputStreamScanner.hasNextLine()) {
                    try {
                        String nextURL = inputStreamScanner.nextLine();
                    //    System.out.println("Add " + nextURL);
                        leftURLs.add(new URL(nextURL));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                inputStreamScanner.close();

                // write the object to the processed directory

                S3Object nextObject = threadClient.getObject(bucketName, nextKey);
                InputStream writeInputStream = nextObject.getObjectContent();

                threadClient.putObject(bucketName, processedDirectoryName + "/" + nextKey,
                        writeInputStream, nextObject.getObjectMetadata());

                // Delete the Object.
                threadClient.deleteObject(bucketName, nextKey);

            }

        }

    }

}

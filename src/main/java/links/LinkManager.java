package links;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tika.metadata.Metadata;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import crawler.BucketWriter;
import crawler.Main;

/**
 * 
 * Manages the crawled links and throws a "link file" to bucket after a specified number of links are
 * crawled. 
 * 
 * @author rajatpawar
 *
 */
public class LinkManager {
    
    LinkedList<URL> currentLinkStore;
    int listSize;
    String defaultBucketName;
    AWSCredentials credentials;
    String QueueDirectoryName ;
    
    public LinkManager(AWSCredentials credentials){
        
        currentLinkStore = new LinkedList<URL>();
        listSize = 100;
        this.credentials = credentials;
        System.out.println("Using " + Main.getBucketName() + " as bucket for throwing crawled URL lists.");
        QueueDirectoryName = "Queued";
        
    }
    
    public void handleLink(String link) {
       System.out.println(link);
      //  System.out.println("Total: " + currentLinkStore.size());
        UrlValidator validator = new UrlValidator();
        
        // check if the link's mime type is text/html ?
        
        boolean isHTML = checkIfHtml(link);
     //   if(isHTML)
       //     System.out.println("is HTML!");
        
        
        if(validator.isValid(link)){
        
            if(currentLinkStore.size()==100){
                // throw the links to the store
                throwLinksList();
                currentLinkStore = new LinkedList<URL>();
            }
            
            try {
                currentLinkStore.add(new URL(link));
            } catch (MalformedURLException e) {
                System.out.println("Unable to add link ! " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean checkIfHtml(String link) {
        
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection)  url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            String contentType = connection.getContentType();
        //    System.out.println(contentType);
            
            
            Pattern p = Pattern.compile("text/html.*");
            Matcher m = p.matcher(contentType);
            boolean b = m.matches();
            if(b)
                return true;
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException nex){
            nex.printStackTrace();
        }
        
     return false;
        
    }

    public void throwLinksList() {
        // throw the list as a file to the bucket.
        System.out.println("=================================================================");
        System.out.println("Done with 100 URLs.");
        AmazonS3Client myClient = new AmazonS3Client(credentials);
        
        // convert the LinkedList<String> to a file-writable format. 
        
        String fileToBeWritten = getFileWritableFormat();
        
        
        InputStream listPage = new ByteArrayInputStream(
                fileToBeWritten.getBytes(StandardCharsets.UTF_8));
        
        String key = (new String("list")).concat("-").concat(System.currentTimeMillis()+ "").concat("-").concat("" + fileToBeWritten.hashCode());
        System.out.println("Throw " + key + " to bucket. ");
        System.out.println("=================================================================");
        
        System.out.println( "Write " + fileToBeWritten);
        ObjectMetadata inputMetadata = new ObjectMetadata();
        myClient.putObject(Main.getBucketName(), QueueDirectoryName +  "/" + key, listPage, inputMetadata);
        
    }

    private String getFileWritableFormat() {
        
        StringBuilder builder = new StringBuilder();
        
        for(URL each : currentLinkStore){
            builder.append(each.toString());
            builder.append("\n");
        }
        
        return builder.toString();
    }

}

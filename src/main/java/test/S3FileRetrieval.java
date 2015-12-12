package test;

import java.io.File;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3FileRetrieval {
    
    
    
    
    
    
    /**
     * 
     * First create a folder hierarchy in the bucket by the
     * various names : Queued, Completed. 
     * 
     * 
     * Write a program to retrieve a file from S3 from the Queued folder.
     * 
     * Try to input it as an inputStream and store the text file in 
     * the memory.
     * 
     * 
     */
    
    
    /**
     * 
     * Get some files form the directory and 
     * upload them to the Queued folder. 
     * 
     */
    public void uploadToS3() {
        
        String folderName = "Queued";
        String directory = "/Users/rajatpawar/Documents/Development/aws/queue";
        String bucketName ="cf-templates-1mxuh0y0g7hlp-us-east-1";
        
        File myDirectory = new File(directory);
        
        
        File[] allFiles = myDirectory.listFiles();
        
        
        for(File each : allFiles){
            
            // put them to bucket. 
            String credentialsFilePath = "/Users/rajatpawar/Documents/Development/aws/cred.properties";
            PropertiesFileCredentialsProvider credP = new PropertiesFileCredentialsProvider(credentialsFilePath);
            AmazonS3Client client = new AmazonS3Client(credP);
            // put the file with the name of that file. 
            client.putObject(bucketName, folderName + "/" +  each.getName(), each);
            
        }
        
        
    }
    
    

}

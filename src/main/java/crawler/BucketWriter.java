package crawler;

import java.io.InputStream;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class BucketWriter {
    
    private String bucketName;
    private String credentialsFilePath;
    private ObjectMetadata metadata;
    
    public BucketWriter(String bucketName, String credentialsFilePath) {
        this.bucketName=bucketName;
        this.credentialsFilePath=credentialsFilePath;
        
        metadata = new ObjectMetadata();
        metadata.addUserMetadata("User", "Rajat");
        
    }
    
    public void write(String key, InputStream inputStream) {
        
        PropertiesFileCredentialsProvider pfcp = new PropertiesFileCredentialsProvider(credentialsFilePath);
        AmazonS3Client myClient = new AmazonS3Client(pfcp);
        myClient.putObject(bucketName, key, inputStream, metadata);
        
    }

}

package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {
    
    // whether to store the hosts in all links in the pages. 
    
    static DomainStore domainStore;

    public static void main(String[] args) throws IOException {
        
        // args[0] points to a file that contains a list of URLs
        // args[1] is the name of the bucket
        // args[2] is the properties file for bucket.
        
        if(args[3].equals("true")){
            domainStore = new DomainStore();
            DomainStore.storeDomain=true;
        } else {
            DomainStore.storeDomain=false;
        }
        
        ListCrawler myCrawler = new ListCrawler(args[1],args[2]);
  
        List<URL> urlList =  getURLListFromFile(args[0]);
        
        URLSource listSource = new URLSource(urlList);
        
        myCrawler.crawl(listSource, Paths.get(args[1]));
        
        // let args[4] be the domain outputpath. 
        
        writeDomainParams(args[4]);
        
    }

    private static void writeDomainParams(String path) {
        
        
        try {
            File domainParamFile = new File(path);
            domainParamFile.createNewFile();
            FileWriter myWriter = new FileWriter(domainParamFile);
            
            Iterator<String> domainIterator = domainStore.domainMap.keySet().iterator();
            while(domainIterator.hasNext()){
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

    private static List<URL> getURLListFromFile(String path) throws IOException{
        
        File urlListFile = new File(path);
        
        Scanner fileScanner = new Scanner(urlListFile);
        
        LinkedList<URL> urlList = new LinkedList<URL>();
        
        while(fileScanner.hasNextLine()){
            urlList.add(new URL(fileScanner.nextLine()));
        }
        
        fileScanner.close();
        
        return urlList;
    }

}

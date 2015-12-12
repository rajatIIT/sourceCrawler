package crawler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Utils {
    
    
public static List<URL> getURLListFromFile(String path) throws IOException{
        
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

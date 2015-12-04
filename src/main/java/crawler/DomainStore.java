package crawler;

import java.util.HashMap;


/**
 * 
 * A class used to store the frequency of various domains in the 
 * page sources.  
 * 
 * @author rajatpawar
 *
 */
public class DomainStore {
    static boolean storeDomain;
    HashMap<String,Integer> domainMap = new HashMap<String,Integer>();
    
    public void addDomain(String host) {
        if(!domainMap.containsKey(host)){
            domainMap.put(host, 1);
        } else {
            domainMap.put(host, domainMap.get(host)+1);
        }
    }

}

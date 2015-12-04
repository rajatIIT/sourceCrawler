package crawler;

import java.nio.file.Path;

public interface Crawler {
    
    public void crawl(URLSource source, Path OutputDirectory );
        
}

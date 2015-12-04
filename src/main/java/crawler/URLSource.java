package crawler;

import java.net.URL;
import java.util.List;

public class URLSource {

    private List<URL> urlList;

    public URLSource(List<URL> urlList) {
        this.urlList = urlList;
    }

    public boolean hasNext() {
        if (urlList.isEmpty())
            return false;
        else
            return true;
    }

    public URL nextURL() {
        if (urlList.isEmpty()) {
            return null;
        } else {
            URL nextURL = urlList.get(0);
            urlList.remove(0);
            return nextURL;
        }
    }

}

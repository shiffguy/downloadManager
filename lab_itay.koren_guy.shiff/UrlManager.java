import java.io.*;
import java.net.*;
import java.util.*;

class UrlManager {

    static List<URL> manageUrls(String firstUrlArg) {
        List<URL> urls = new ArrayList<>();

        try {
            if (firstUrlArg.matches("http(s)?://.*")) {
                urls.add(new URL(firstUrlArg));
            } else {
                Scanner s = new Scanner(new File(firstUrlArg));
                while (s.hasNextLine()) {
                    String url = s.nextLine();
                    urls.add(new URL(url));
                }
            }
        } catch (MalformedURLException e) {
            DmUI.printInvalidURL();
        } catch (FileNotFoundException e) {
            DmUI.printFileNotFound();
        }

        return urls;
    }
}

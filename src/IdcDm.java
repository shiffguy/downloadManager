import java.net.*;
import java.util.*;

public class IdcDm {

    public static void main(String[] args) {
        int argsLen = args.length;
        int maxNumOfConnections = 0;
        List<URL> urls = null;
        try {
            switch (argsLen){
                case 1:
                    maxNumOfConnections = 1;
                    urls = UrlManager.manageUrls(args[0]);
                    break;
                case 2:
                    urls = UrlManager.manageUrls(args[0]);
                    maxNumOfConnections = Integer.parseInt(args[1]);
                    break;
                default:
                    DmUI.printUsage();
                    return;
            }

        } catch (NumberFormatException e) {
            DmUI.printNotAnInteger();
        }

        if (urls.size() > 0) {
            Downloader downloader = new Downloader(urls, maxNumOfConnections);

            if (argsLen == 1) {
                DmUI.printDownloading();
            } else {
                DmUI.printDownloadingN(maxNumOfConnections);
            }
            downloader.run();
        }
    }
}

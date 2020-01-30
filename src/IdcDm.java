import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IdcDm {
    /***
     * Get the needed url addresses of the servers from the CMD or from a file
     * @param urlArgument url address or file contains url addresses
     * @return List<URL> contains all servers which will be managed by the DM
     */
    public static List<URL> parseUrlArgument(String urlArgument) {
        List<URL> urlsList = new ArrayList<>();

        try {
            if (urlArgument.matches("http(s)?://.*")) {
                urlsList.add(new URL(urlArgument));
            } else {
                Scanner scanner = new Scanner(new File(urlArgument));
                while (scanner.hasNextLine()) {
                    String url = scanner.nextLine();
                    urlsList.add(new URL(url));
                }
            }
        } catch (MalformedURLException e) {
            DmUI.printInvalidURL();
        } catch (FileNotFoundException e) {
            DmUI.printFileNotFound();
        }

        return urlsList;
    }

    public static void main(String[] args) {
        int argsLen = args.length;
        int numberOfThreads = 0;
        List<URL> urlsList = null;
        try {
            switch (argsLen){
                case 1:
                    numberOfThreads = 1;
                    urlsList = parseUrlArgument(args[0]);
                    break;
                case 2:
                    urlsList = parseUrlArgument(args[0]);
                    numberOfThreads = Integer.parseInt(args[1]);
                    break;
                default:
                    DmUI.printUsage();
                    return;
            }

        } catch (NumberFormatException e) {
            DmUI.printNotAnInteger();
        }

        if (urlsList.size() > 0) {
            DownloadManager downloadManager = new DownloadManager(urlsList, numberOfThreads);

            if (argsLen == 1) {
                DmUI.printDownloading();
            } else {
                DmUI.printDownloadingN(numberOfThreads);
            }
            downloadManager.run();
        }
    }
}

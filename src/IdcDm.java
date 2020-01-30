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
            if (urlArgument.startsWith("http://") || urlArgument.startsWith("https://")) {
                urlsList.add(new URL(urlArgument));

            } else {
                Scanner scanner = new Scanner(new File(urlArgument));
                scanner.useDelimiter(System.lineSeparator());
                while (scanner.hasNext()) {
                    String url = scanner.next();
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
        // TODO: "https://archive.org/download/Mario1_500/ Mario1_500.avi" the ling is not vakid when there is a space
        // but this is the link from them example, how to handle it?
        // TODO: handle exception all ver the program, to make sure the program is terminated
        int numberOfThreads = 0;
        boolean isUrlArgumentValid = false;
        List<URL> urlsList = null;
        try {
            if (args.length == 0) {
                DmUI.printUsage();
                return;
            } else if (args.length == 1) {
                numberOfThreads = 1;
                urlsList = parseUrlArgument(args[0]);
            } else if (args.length == 2) {
                urlsList = parseUrlArgument(args[0]);
                numberOfThreads = Integer.parseInt(args[1]);
            } else {
                DmUI.printArgsOverflow();
            }
        } catch (NumberFormatException e) {
            DmUI.printNotAnInteger();
        }


       // isUrlArgumentValid = urlsList != null && urlsList.size() > 0;

        if (urlsList.size() > 0) {
            DownloadManager downloadManager = new DownloadManager(urlsList, numberOfThreads);
            if (args.length == 1) {
                DmUI.printDownloading();
            } else {
                DmUI.printDownloadingN(numberOfThreads);
            }
            downloadManager.run();
        }
    }
}

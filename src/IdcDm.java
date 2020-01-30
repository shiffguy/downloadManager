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
        String usage = "usage:\n\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]";


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
            System.err.println("Error, invalid url\n" + usage);
        } catch (FileNotFoundException e) {
            System.err.println("Error, can't find urls list file\n" + usage);
        }

        return urlsList;
    }

    public static void main(String[] args) {
        // TODO: "https://archive.org/download/Mario1_500/ Mario1_500.avi" the ling is not vakid when there is a space
        // but this is the link from them example, how to handle it?
        // TODO: handle exception all ver the program, to make sure the program is terminated
        int numberOfThreads = 0;
        boolean isUrlArgumentValid = false;
        String usage = "usage:\n" +
                "\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]";
        List<URL> urlsList = null;
        try {
            if (args.length == 0) {
                System.err.println(usage);
                return;
            } else if (args.length == 1) {
                numberOfThreads = 1;
                urlsList = parseUrlArgument(args[0]);
            } else if (args.length == 2) {
                urlsList = parseUrlArgument(args[0]);
                numberOfThreads = Integer.parseInt(args[1]);
            } else {
                System.err.println("Error, too much arguments \n" + usage);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error, please use vaild integer\n" + usage);
        }


       // isUrlArgumentValid = urlsList != null && urlsList.size() > 0;

        if (urlsList.size() > 0) {
            DownloadManager downloadManager = new DownloadManager(urlsList, numberOfThreads);
            if (args.length == 1) {
                System.err.println("Downloading...");
            } else {
                System.err.printf("Downloading using %d connections...\n", numberOfThreads);
            }
            downloadManager.run();
        }
    }
}

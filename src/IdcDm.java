import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IdcDm {
    /***
     * Util function to read the urls provided by the user in order to download the File
     * If a link is provided the List will contain only 1 url
     * else it will assume that file is provided
     * @param urlArgument String that represent a url or local path
     * @return List<URL> of all server links to download from
     */
    public static List<URL> parseUrlArgument(String urlArgument) {
        List<URL> urlsList = new ArrayList<>();
        boolean isUrlList = !urlArgument.startsWith("http://") && !urlArgument.startsWith("https://");

        try {
            if (isUrlList) {
                Scanner scanner = new Scanner(new File(urlArgument));
                scanner.useDelimiter(System.lineSeparator());
                while (scanner.hasNext()) {
                    String url = scanner.next();
                    urlsList.add(new URL(url));
                }
            } else {
                urlsList.add(new URL(urlArgument));
            }
        } catch (MalformedURLException e) {
            System.err.println("Fail to execute program, invalid url");
        } catch (FileNotFoundException e) {
            System.err.println("Fail to execute program, can't find urls list file");
        }

        return urlsList;
    }

    public static void main(String[] args) {
        // TODO: "https://archive.org/download/Mario1_500/ Mario1_500.avi" the ling is not vakid when there is a space
        // but this is the link from them example, how to handle it?
        // TODO: handke exception all ver the program, to make sure the program is terminated
        int numberOfThreads = 0;
        List<URL> urlsList = null;
        boolean isNumOfThreadProvided = args.length == 2;

        try {
            numberOfThreads = isNumOfThreadProvided ? Integer.parseInt(args[1]) : 1;
        } catch (NumberFormatException e) {
            System.err.println("Fail to execute program, invalid number of threads");
        }

        boolean isThreadsArgumentValid = numberOfThreads > 0;

        if(isThreadsArgumentValid) {
            urlsList = parseUrlArgument(args[0]);
        }

        boolean isUrlArgumentValid = urlsList != null && urlsList.size() > 0;

        if(isUrlArgumentValid) {
            DownloadManager downloadManager = new DownloadManager(urlsList, numberOfThreads);
            if(!isNumOfThreadProvided){
                System.err.println("Downloading...");
            }else{
                System.err.printf("Downloading using %d connections...\n", numberOfThreads);
            }
            downloadManager.run();
        }
    }
}

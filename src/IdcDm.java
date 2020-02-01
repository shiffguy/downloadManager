import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IdcDm {

    private static List<URL> readCommandLineFirstUrlArg(String firstUrlArg) {
        List<URL> urlsList = new ArrayList<>();

        try {
            if (firstUrlArg.matches("http(s)?://.*")) {
                urlsList.add(new URL(firstUrlArg));
            } else {
                Scanner scanner = new Scanner(new File(firstUrlArg));
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
        int maxNumOfConnections = 0;
        List<URL> urlsList = null;
        try {
            switch (argsLen){
                case 1:
                    maxNumOfConnections = 1;
                    urlsList = readCommandLineFirstUrlArg(args[0]);
                    break;
                case 2:
                    urlsList = readCommandLineFirstUrlArg(args[0]);
                    maxNumOfConnections = Integer.parseInt(args[1]);
                    break;
                default:
                    DmUI.printUsage();
                    return;
            }

        } catch (NumberFormatException e) {
            DmUI.printNotAnInteger();
        }

        if (urlsList.size() > 0) {
            DownloadManager downloadManager = new DownloadManager(urlsList, maxNumOfConnections);

            if (argsLen == 1) {
                DmUI.printDownloading();
            } else {
                DmUI.printDownloadingN(maxNumOfConnections);
            }
            downloadManager.run();
        }
    }
}

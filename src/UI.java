public class UI {

    String usage = "usage:\n\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]\n";
    String argsOverflow = "Error, too much arguments \n";
    String notAnInteger = "Error, please use vaild integer\n";
    String invalidURL = "Error, invalid url\n";
    String fileNotFound = "Error, can't find file\n";
    String Downloading = "Downloading...";

    public void printUsage {
        System.err.println(usage);
    }

    public void printArgsOverflow {
        System.err.println(argsOverflow);
    }

    public void printNotAnInteger {
        System.err.println(notAnInteger);
    }

    public void printDownloading {
        System.err.println(Downloading);
    }

    public void printDownloadingN(n) {
        System.err.printf("Downloading using %d connections...\n", n);
    }

    public void printInvalidURL {
        System.err.println(invalidURL);
    }

    public void printFileNotFound {
        System.err.println(fileNotFound);
    }
}
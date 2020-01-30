public class DmUI {

    private final static String Usage = "usage:\n\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]\n";
    private final static String ArgsOverflow = "Error, too much arguments \n";
    private final static String NotAnInteger = "Error, please use vaild integer\n";
    private final static String InvalidURL = "Error, invalid url\n";
    private final static String FileNotFound = "Error, can't find file\n";
    private final static String Downloading = "Downloading...";
    private final static String ConnectionFailed = "Failed to connect to the server. Download failed.\n";

    public static void printUsage() {
        System.err.println(Usage);
    }

    public static void printArgsOverflow() {
        System.err.println(ArgsOverflow);
    }

    public static void printNotAnInteger() {
        System.err.println(NotAnInteger);
    }

    public static void printDownloading() {
        System.err.println(Downloading);
    }

    public static void printDownloadingN(int n) {
        System.err.println("Downloading using %d connections..." + n);
    }

    public static void printInvalidURL() {
        System.err.println(InvalidURL);
    }

    public static void printConnectionFailed() {
        System.err.println(ConnectionFailed);
    }

    public static void printFileNotFound() {
        System.err.println(FileNotFound);
    }

    public static void printFailedToDeleteMetaData(String s) {
        System.err.println("Failed to delete the metadata file" + s);
    }

    public static void printFailedHTTPRequest(String s) {
        System.err.println("HTTP request to" + s + "has been failed");
    }

    public static void printFailedToDownloadPacket(long start ,String s) {
        System.err.println("Failed to download packet " + start + "from " + s);
    }

    public static void printFinishedToDownload(long n) {
        System.err.println("[" + n + "] Finished downloading");
    }

    public static void printStartDownloadMessage(long n,long start, long end) {

        System.err.println("[" + n + " Start downloading range (" + start + " - " + end +") from:");
    }
}
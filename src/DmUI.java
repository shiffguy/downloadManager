public class DmUI {

    private final static String Usage = "usage:\n\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]";
    private final static String ArgsOverflow = "Error, too much arguments.";
    private final static String NotAnInteger = "Error, please use vaild integer.";
    private final static String InvalidURL = "Error, invalid url\n";
    private final static String FileNotFound = "Error, can't find file.";
    private final static String FileNotCreated = "Error, can't create file.";
    private final static String Downloading = "Downloading...";
    private final static String ConnectionFailed = "Failed to connect to the server. Download failed.";
    private final static String DownloadSucceeded = "Download succeeded";
    private final static String DownloadFailed = "Failed to download packets -> Download failed.";
    private final static String FailedToWritePacket = "Failed to write packet to file. Download failed.";


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

    public static void printDownloadSucceeded() {
        System.err.println(DownloadSucceeded);
    }

    public static void printDownloadFailed() {
        System.err.println(DownloadFailed);
    }

    public static void printFailedToWritePacket() {
        System.err.println(FailedToWritePacket);
    }

    public static void printDownloadingN(int n) {
        System.err.println("Downloading using " + n + " connections...");
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

    public static void printFileNotCreated() {
        System.err.println(FileNotCreated);
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

        System.err.println("[" + n + "] Start downloading range (" + start + " - " + end +") from:");
    }

    public static void printDownloadStatus(MetaData metaData,int n, boolean b) {
        double downloadCounterStatus = metaData.GetCounterOfDownloadedPackets();
        int status = (int) ((downloadCounterStatus / metaData.GetNumberOfChunks()) * 100);
        if (status != n || b) {
            n = status;
            System.err.println("Downloaded " + n + "%");
        }
    }
}
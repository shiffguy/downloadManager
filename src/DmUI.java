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


    static void printUsage() {
        System.err.println(Usage);
    }

    static void printArgsOverflow() {
        System.err.println(ArgsOverflow);
    }

    static void printNotAnInteger() {
        System.err.println(NotAnInteger);
    }

    static void printDownloading() {
        System.err.println(Downloading);
    }

    static void printDownloadSucceeded() {
        System.err.println(DownloadSucceeded);
    }

    static void printDownloadFailed() {
        System.err.println(DownloadFailed);
    }

    static void printFailedToWritePacket() {
        System.err.println(FailedToWritePacket);
    }

    static void printDownloadingN(int n) {
        System.err.println("Downloading using " + n + " connections...");
    }

    static void printInvalidURL() {
        System.err.println(InvalidURL);
    }

    static void printConnectionFailed() {
        System.err.println(ConnectionFailed);
    }

    static void printFileNotFound() {
        System.err.println(FileNotFound);
    }

    static void printFileNotCreated() {
        System.err.println(FileNotCreated);
    }


    static void printFailedToDeleteMetaData(String s) {
        System.err.println("Failed to delete the metadata file" + s);
    }

    static void printFailedHTTPRequest(String s) {
        System.err.println("HTTP request to " + s + " has been failed");
    }

    static void printFailedToDownloadPacket(long start ,String s) {
        System.err.println("Failed to download chunk of data " + start + " from " + s);
    }

    static void printFinishedToDownload(long n) {
        System.err.println("[" + n + "] Finished downloading");
    }

    static void printStartDownloadMessage(long n,long start, long end) {

        System.err.println("[" + n + "] Start downloading range (" + start + " - " + end +") from:");
    }

    static void printDownloadStatus(MetaData metaData, int n, boolean b) {
        double downloadCounterStatus = metaData.GetCounterOfDownloadedPackets();
        int status = (int) ((downloadCounterStatus / metaData.GetNumberOfPackets()) * 100);
        if (status != n || b) {
            n = status;
            System.err.println("Downloaded " + n + "%");
        }
    }
}
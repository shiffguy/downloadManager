import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingDeque;

public class PacketDownloader implements Runnable {

    private static final int REQUEST_TIME_OUT = 30 * 1000;  // Request for connection life time in MS
    private static final int READ_TIME_OUT = 30 * 1000;  // Reading connection InputStream life time in MS
    //region Fields
    private final int packetIndex;
    private LinkedBlockingDeque<DataWrapper> packetQueue;
    private URL source;
    private long packetStartPosition;
    private long packetEndPosition;
    //endregion

    //region Constructor

    PacketDownloader(LinkedBlockingDeque<DataWrapper> packetQueue, URL source,
                     long packetStartPosition, long packetEndPosition, int packetIndex) {
        this.packetQueue = packetQueue;
        this.source = source;
        this.packetStartPosition = packetStartPosition;
        this.packetEndPosition = packetEndPosition;
        this.packetIndex = packetIndex;
    }
    //endregion

    //region Public Methods

    /**
     * Handles the given packet by type
     */
    @Override
    public void run() {
        boolean isDataPacket = this.checkPacket();
        if(isDataPacket){
            this.handleDataPacket();
        }
        else{
            this.handlePoisonPill();
        }
    }

    //endregion

    //region Private Methods

    /**
     * Check the type of the packet
     * @return true if data packet otherwise false (if poison pill packet)
     */
    private boolean checkPacket() {
        return this.packetIndex != -1;
    }

    /**
     * Handle packet of type poison pill. Create a message of type DataWrapper and push it to the queue. When the
     * writer will receive this packet it will now that the PacketDownloaders finish to handle all their tasks and it can
     * terminate.
     */
    private void handlePoisonPill(){
        DataWrapper poisonPill = new DataWrapper(-1, -1, null);
        this.packetQueue.add(poisonPill);
    }
    /**
     * Handle a data type packet by creating a request for the data and put the data in the queue
     */
    private void handleDataPacket() {
        InputStream inputStream = this.executeContentRangeRequest();
        if (inputStream != null) {
            this.downloadPacket(inputStream);
        }
    }

    /**
     * Execute a GET request to the source url with the Content-Range header to download a specific packet
     * @return InputStream, an open inputStream to the source url
     */
    private InputStream executeContentRangeRequest() {
        InputStream inputStream = null;
        try {
            String range = this.get_byte_range();
            HttpURLConnection httpConnection = (HttpURLConnection) source.openConnection();
            try {
                httpConnection.setRequestMethod("GET");
                httpConnection.setConnectTimeout(REQUEST_TIME_OUT);
                httpConnection.setReadTimeout(READ_TIME_OUT);
            } catch (ProtocolException e) {
                System.err.printf("Fail to execute http request to %s, wrong request method\n", this.source.toString());
            }
            httpConnection.setRequestProperty("Range", range);
            int responseCode = httpConnection.getResponseCode();
            inputStream = responseCode == HttpURLConnection.HTTP_PARTIAL ? httpConnection.getInputStream() : null;

        } catch (IOException e) {
            System.err.printf("Fail to execute http request to %s\n", this.source.toString());
        }

        return inputStream;
    }

    /**
     * Download the bytes of the packet from the url, create a data wrapper and insert the data wrapper
     * to the packets queue
     * @param inputStream, and open input stream to the url
     */
    private void downloadPacket(InputStream inputStream) {
        try {
            printStartDownloadMessage();
            byte[] buffer = inputStream.readAllBytes();
            DataWrapper dataWrapper = new DataWrapper(packetIndex, packetStartPosition, buffer);
            this.packetQueue.add(dataWrapper);
            printFinishedDownloadMessage();
        } catch (IOException e) {
            System.err.printf("Fail to download packet %d from %s\n", this.packetStartPosition, this.source.toString());
        }
    }

    private void printFinishedDownloadMessage() {
        System.err.printf("[%s] Finished downloading\n",
                Thread.currentThread().getId());
    }

    private void printStartDownloadMessage() {
        String sb = "";
        sb += String.format("[%s] Start downloading range (%d- %d) from:\n",
                Thread.currentThread().getId(), packetStartPosition, packetEndPosition);
        sb += source;

        System.err.println(sb);
    }

    /**
     * Create a string which is the value of the range header in the http request
     * @return string represent the range by the format of the request
     */
    private String get_byte_range(){
        return String.format("Bytes=%d-%d", packetStartPosition, packetEndPosition);
    }
    //endregion
}


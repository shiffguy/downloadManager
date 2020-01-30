import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketDownloader implements Runnable {

    private static final int REQUEST_TIME_OUT = 30 * 1000;  // Request for connection life time in MS
    private static final int READ_TIME_OUT = 30 * 1000;  // Reading connection InputStream life time in MS
    //region Fields
    private final int packetIndex;
    private final boolean killStatus;
    private LinkedBlockingQueue<PacketBuilder> packetQueue;
    private URL source;
    private long packetStartPosition;
    private long packetEndPosition;
    //endregion

    //region Constructor

    PacketDownloader(LinkedBlockingQueue<PacketBuilder> packetQueue, URL source,
                     long packetStartPosition, long packetEndPosition, int packetIndex, boolean killStatus) {
        this.packetQueue = packetQueue;
        this.source = source;
        this.packetStartPosition = packetStartPosition;
        this.packetEndPosition = packetEndPosition;
        this.packetIndex = packetIndex;
        this.killStatus = killStatus;
    }
    //endregion

    //region Public Methods

    /**
     * Handles the given packet by type
     */
    @Override
    public void run() { ;
        if(!killStatus){
            this.handleDataPacket();
        }
        else{
            this.addKillPacket();
        }
    }

    //endregion

    //region Private Methods


    /**
     * Handle packet of type poison pill. Create a message of type DataWrapper and push it to the queue. When the
     * writer will receive this packet it will now that the PacketDownloaders finish to handle all their tasks and it can
     * terminate.
     */
    private void addKillPacket(){
        this.packetQueue.add(new PacketBuilder(-1, -1, null, true));
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
                DmUI.printFailedHTTPRequest(this.source.toString());
            }
            httpConnection.setRequestProperty("Range", range);
            int responseCode = httpConnection.getResponseCode();
            inputStream = responseCode == HttpURLConnection.HTTP_PARTIAL ? httpConnection.getInputStream() : null;

        } catch (IOException e) {
            DmUI.printFailedHTTPRequest(this.source.toString());
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
            DmUI.printStartDownloadMessage(Thread.currentThread().getId(),packetStartPosition,packetEndPosition);
            byte[] buffer = inputStream.readAllBytes();
            PacketBuilder packetBuilder = new PacketBuilder(packetIndex, packetStartPosition, buffer, false);
            this.packetQueue.add(packetBuilder);
            DmUI.printFinishedToDownload(Thread.currentThread().getId());
        } catch (IOException e) {
            DmUI.printFailedToDownloadPacket(this.packetStartPosition, this.source.toString());
        }
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


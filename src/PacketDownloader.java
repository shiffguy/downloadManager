import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketDownloader implements Runnable {

    private static final int REQUEST_TIME_OUT = 30 * 1000;
    private static final int READ_TIME_OUT = 30 * 1000;
    private final int packetIndex;
    private final boolean killStatus;
    private LinkedBlockingQueue <PacketBuilder> packetQueue;
    private URL source;
    private long packetStartPosition;
    private long packetEndPosition;


    PacketDownloader(LinkedBlockingQueue <PacketBuilder> packetQueue, URL source,
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
    public void run() {

        if (!killStatus) {
            InputStream inputStream = null;
            try {
                String range = String.format("Bytes=%d-%d", packetStartPosition, packetEndPosition);
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

            if (inputStream != null) {
                try {
                    DmUI.printStartDownloadMessage(Thread.currentThread().getId(), packetStartPosition, packetEndPosition);
                    byte[] buffer = inputStream.readAllBytes();
                    PacketBuilder packetBuilder = new PacketBuilder(packetIndex, packetStartPosition, buffer);
                    this.packetQueue.add(packetBuilder);
                    DmUI.printFinishedToDownload(Thread.currentThread().getId());
                } catch (IOException e) {
                    DmUI.printFailedToDownloadPacket(this.packetStartPosition, this.source.toString());
                }
            }
        } else {
            this.packetQueue.add(new PacketBuilder(true));
        }
    }
}


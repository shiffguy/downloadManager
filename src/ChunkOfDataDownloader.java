import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkOfDataDownloader implements Runnable {
    private static final int REQUEST_TIME_OUT = 30 * 1000; //request timeout for the http timeout
    private static final int READ_TIME_OUT = 30 * 1000; //read timeout for the http read timeout
    private final int packetIndex;
    private final boolean killStatus;
    private LinkedBlockingQueue <PacketBuilder> packetsBlockingQueue;
    private URL httpRequestedUrl;
    private long chunkStartPos;
    private long chunkEndPos;


    ChunkOfDataDownloader(LinkedBlockingQueue <PacketBuilder> packetsBlockingQueue, URL httpRequestedUrl,
                     long chunkStartPos, long chunkEndPos, int packetIndex, boolean killStatus) {
        this.packetsBlockingQueue = packetsBlockingQueue;
        this.httpRequestedUrl = httpRequestedUrl;
        this.chunkStartPos = chunkStartPos;
        this.chunkEndPos = chunkEndPos;
        this.packetIndex = packetIndex;
        this.killStatus = killStatus;
    }

    /**
     * Handles the given packet by type
     */
    @Override
    public void run() {

        if (!killStatus) {
            InputStream inputStream = null;
            try {
                String range = String.format("Bytes=%d-%d", chunkStartPos, chunkEndPos);
                HttpURLConnection httpConnection = (HttpURLConnection) httpRequestedUrl.openConnection();
                try {
                    httpConnection.setRequestMethod("GET");
                    httpConnection.setConnectTimeout(REQUEST_TIME_OUT);
                    httpConnection.setReadTimeout(READ_TIME_OUT);
                } catch (ProtocolException e) {
                    DmUI.printFailedHTTPRequest(this.httpRequestedUrl.toString());
                }
                httpConnection.setRequestProperty("Range", range);
                int responseCode = httpConnection.getResponseCode();
                inputStream = responseCode == HttpURLConnection.HTTP_PARTIAL ? httpConnection.getInputStream() : null;

            } catch (IOException e) {
                DmUI.printFailedHTTPRequest(this.httpRequestedUrl.toString());
            }

            if (inputStream != null) {
                try {
                    DmUI.printStartDownloadMessage(Thread.currentThread().getId(), chunkStartPos, chunkEndPos);
                    byte[] buffer = inputStream.readAllBytes();
                    PacketBuilder packetBuilder = new PacketBuilder(packetIndex, chunkStartPos, buffer);
                    this.packetsBlockingQueue.add(packetBuilder);
                    DmUI.printFinishedToDownload(Thread.currentThread().getId());
                } catch (IOException e) {
                    DmUI.printFailedToDownloadPacket(this.chunkStartPos, this.httpRequestedUrl.toString());
                }
            }
        } else {
            this.packetsBlockingQueue.add(new PacketBuilder(true));
        }
    }
}


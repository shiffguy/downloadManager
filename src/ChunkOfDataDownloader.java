import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;
import java.net.*;

public class ChunkOfDataDownloader implements Runnable {
    private static final int CONNECT_TIME_OUT = 30 * 1000; //connect timeout for the http timeout
    private static final int READ_TIME_OUT = 30 * 1000; //read timeout for the http read timeout
    private URL httpRequestedUrl;
    private LinkedBlockingQueue <PacketBuilder> packetsBlockingQueue;
    private long chunkStartPos;
    private long chunkEndPos;
    private final int packetIndex;
    private final boolean killStatus;


    ChunkOfDataDownloader(LinkedBlockingQueue <PacketBuilder> packetsBlockingQueue, URL httpRequestedUrl,
                     long chunkStartPos, long chunkEndPos, int packetIndex, boolean killStatus) {
        this.httpRequestedUrl = httpRequestedUrl;
        this.packetsBlockingQueue = packetsBlockingQueue;
        this.chunkStartPos = chunkStartPos;
        this.chunkEndPos = chunkEndPos;
        this.packetIndex = packetIndex;
        this.killStatus = killStatus;
    }


    @Override
    public void run() {

        if (!killStatus) {
            InputStream inputStream = null;
            try {
                String range = String.format("Bytes=%d-%d", chunkStartPos, chunkEndPos);
                HttpURLConnection connection = (HttpURLConnection) httpRequestedUrl.openConnection();
                try {
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(CONNECT_TIME_OUT);
                    connection.setReadTimeout(READ_TIME_OUT);
                } catch (ProtocolException e) {
                    DmUI.printFailedHTTPRequest(this.httpRequestedUrl.toString());
                }
                connection.setRequestProperty("Range", range);
                int response = connection.getResponseCode();
                inputStream = response == HttpURLConnection.HTTP_PARTIAL ? connection.getInputStream() : null;

            } catch (IOException e) {
                DmUI.printFailedHTTPRequest(this.httpRequestedUrl.toString());
            }

            if (inputStream != null) {
                try {
                    DmUI.printStartDownloadMessage(Thread.currentThread().getId(), chunkStartPos, chunkEndPos);
                    byte[] chunksBuffer = inputStream.readAllBytes();
                    PacketBuilder packetBuilder = new PacketBuilder(packetIndex, chunkStartPos, chunksBuffer);
                    this.packetsBlockingQueue.add(packetBuilder);
                    DmUI.printFinishedToDownload(Thread.currentThread().getId());
                    inputStream.close();
                } catch (IOException e) {
                    DmUI.printFailedToDownloadPacket(this.chunkStartPos, this.httpRequestedUrl.toString());
                }
            }
        } else {

            this.packetsBlockingQueue.add(new PacketBuilder(true));
        }
    }
}


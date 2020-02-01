import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;
import java.net.*;

public class HTTPRangeGetter implements Runnable {
    private static final int timeOutInMS = 30000; //connect & read timeout of 30s
    private URL httpRequestedUrl;
    private LinkedBlockingQueue <PacketBuilder> blockingQueue;
    private long chunkStartPos;
    private long chunkEndPos;
    private final int index;
    private final boolean isEndPacket;


    HTTPRangeGetter(LinkedBlockingQueue <PacketBuilder> blockingQueue, URL httpRequestedUrl,
                    long chunkStartPos, long chunkEndPos, int index, boolean isEndPacket) {
        this.httpRequestedUrl = httpRequestedUrl;
        this.blockingQueue = blockingQueue;
        this.chunkStartPos = chunkStartPos;
        this.chunkEndPos = chunkEndPos;
        this.index = index;
        this.isEndPacket = isEndPacket;
    }


    @Override
    public void run() {

        if (!isEndPacket) {
            InputStream inputStream = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) httpRequestedUrl.openConnection();
                connection.setConnectTimeout(timeOutInMS);
                connection.setReadTimeout(timeOutInMS);
                connection.setRequestProperty("Range", "Bytes=" + chunkStartPos + "-" + chunkEndPos);
                if (connection.getResponseCode() / 100 != 2){
                    throw new IOException();
                }
                else{
                    inputStream = connection.getInputStream();
                }

            } catch (IOException e) {
                DmUI.printFailedHTTPRequest(this.httpRequestedUrl.toString());
            }

            if (inputStream != null) {
                try {
                    DmUI.printBeginningDownload(Thread.currentThread().getId(), chunkStartPos, chunkEndPos);
                    byte[] dataChunkSize = inputStream.readAllBytes();
                    PacketBuilder packetBuilder = new PacketBuilder(index, chunkStartPos, dataChunkSize);
                    this.blockingQueue.add(packetBuilder);
                    DmUI.printFinishedToDownload(Thread.currentThread().getId());
                    inputStream.close();
                } catch (IOException e) {
                    DmUI.printFailedToDownloadPacket(this.chunkStartPos, this.httpRequestedUrl.toString());
                }
            }
        } else {

            this.blockingQueue.add(new PacketBuilder(true));
        }
    }
}


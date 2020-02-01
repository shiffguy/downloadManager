import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.net.*;

public class DownloadManager implements Runnable {

    private List<URL> urlsList;
    private LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue;
    private static List<long[]> chunksStartAndEndPositions;
    private ExecutorService threadsPool;
    private long fileSize;
    private int urlIndex;
    private MetaData metaData;
    private static final int dataChunkSize = 500000;  // Each chunk of data size

    DownloadManager(List<URL> urlList, int maxNumOfConnections) {
        this.urlsList = urlList;
        this.packetsBlockingQueue = new LinkedBlockingQueue<>();
        this.threadsPool = Executors.newFixedThreadPool(maxNumOfConnections);
        this.urlIndex = 0;
    }

    /**
     * Initiate a download process of the file from the given urls using threads and dividing the data into chunks
     */
    public void run() {
        this.fileSize = this.getFileSize();
        boolean isSuccessfulConnection = fileSize != -1;

        if (!isSuccessfulConnection) {
            DmUI.printConnectionFailed();
            return;
        }

        String url = this.urlsList.get(0).toString();
        String destFilePath = url.substring( url.lastIndexOf('/')+1);

        this.metaData = MetaData.GetMetaData(getNumOfChunks(), destFilePath + ".tmp");
        chunksStartAndEndPositions = this.getChunksRanges();

        Thread writerThread;
        try {
            writerThread = this.StartWriterThread(destFilePath);
        } catch (IOException e) {
            return;
        }
        IteratesChunks();
        this.threadsPool.shutdown();
        try {
            threadsPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            setEndPacket();
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long getFileSize() {

        HttpURLConnection httpConnection;
        long fileSize = -1;
        URL url = urlsList.get(0);
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            fileSize = httpConnection.getContentLengthLong();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileSize;
    }

    private void newThreadJob(int packetIndex, long[] chunksPositions) {
        URL url = this.urlsList.get(urlIndex);
        long chunkStartPos = chunksPositions[0];
        long chunkEndPos = chunksPositions[1];
        HTTPRangeGetter HTTPRangeGetter = new HTTPRangeGetter(this.packetsBlockingQueue, url,
                chunkStartPos, chunkEndPos, packetIndex, false);

        this.threadsPool.execute(HTTPRangeGetter);
        if(this.urlsList.size()-1 == this.urlIndex){
            this.urlIndex = 0;
        } else {
            this.urlIndex++;
        }
    }

    private Thread StartWriterThread(String destFileName) throws IOException {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(packetsBlockingQueue, metaData, destFileName);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Thread writerThread = new Thread(fileWriter);

        writerThread.start();
        return writerThread;
    }

    private void IteratesChunks() {
        int packetIndex = 0;
        Iterator<long[]> positions = chunksStartAndEndPositions.iterator();
        while (positions.hasNext()){
            long[] chunksPositions = positions.next();
            boolean isPacketDownloaded = metaData.IsIndexDownloaded(packetIndex);
            if (!isPacketDownloaded) {
                newThreadJob(packetIndex, chunksPositions);
            }
            packetIndex++;

        }
    }

    private List<long[]> getChunksRanges() {
        List<long[]> chunksRanges = new ArrayList<>();
        IntStream.range(0, getNumOfChunks()).forEach(i ->  chunksRanges.add(getBytesOfChunkRange(i)));
        return chunksRanges;
    }


    private long[] getBytesOfChunkRange(long chunkStartPos) {
        long chunkStartByte = chunkStartPos * dataChunkSize;
        long chunkEndByte = Math.min(chunkStartByte + dataChunkSize - 1, this.fileSize);

        return new long[]{chunkStartByte, chunkEndByte};
    }

    private int getNumOfChunks() {
        return (fileSize % (long) dataChunkSize == 0) ? (int) (fileSize / dataChunkSize) : (int) (fileSize / dataChunkSize) + 1;
    }

    private void setEndPacket() {
        packetsBlockingQueue.add( new PacketBuilder(true));
    }
}



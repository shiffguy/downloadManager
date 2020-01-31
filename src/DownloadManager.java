import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URL;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class DownloadManager implements Runnable {

    private static final int BUFFER_SIZE = 512 * 1000;  // Each download packet size
    private List<URL> urlsList;
    private LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue;
    private ExecutorService threadsPool;
    private MetaData metaData;
    private long fileSize;
    private static List<long[]> packetPositionsPairs;
    private int urlIndex;

    public DownloadManager(List<URL> urlList, int numberOfThreads) {
        this.urlsList = urlList;
        this.packetsBlockingQueue = new LinkedBlockingQueue<>();
        this.threadsPool = Executors.newFixedThreadPool(numberOfThreads);
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

        this.initMetaData(destFilePath);
        packetPositionsPairs = this.getChunksRanges();

        Thread writerThread;
        try {
            writerThread = this.initPacketWriteThread(destFilePath);
        } catch (IOException e) {
            return;
        }
        accumulatePackets();
        this.threadsPool.shutdown();
        try {
            threadsPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            addKillPacket();
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Accumulates tasks for the threads in thread pool. Stops where gets a kill packet
     */
    private void accumulatePackets() {
        int packetIndex = 0;
        Iterator<long[]> positions = packetPositionsPairs.iterator();
        while (positions.hasNext()){
            long[] packetPositions = positions.next();
            boolean isPacketDownloaded = metaData.IsIndexDownloaded(packetIndex);
            if (!isPacketDownloaded) {
                createTask(packetIndex, packetPositions);
            }
            packetIndex++;

        }
    }

    /**
     * Creates new task fir any of the threads, to download specific chunks of data
     * @param packetIndex the index of the packet
     * @param packetPositions array of tuples which for every tuple 0 - start , 1 - end
     */
    private void createTask(int packetIndex, long[] packetPositions) {
        URL url = this.urlsList.get(urlIndex);
        long chunkStartPos = packetPositions[0];
        long chunkEndPos = packetPositions[1];
        ChunkOfDataDownloader ChunkOfDataDownloader = new ChunkOfDataDownloader(this.packetsBlockingQueue, url,
                chunkStartPos, chunkEndPos, packetIndex, false);

        this.threadsPool.execute(ChunkOfDataDownloader);
        if(this.urlsList.size()-1 == this.urlIndex){
            this.urlIndex = 0;
        } else {
            this.urlIndex++;
        }
    }

    /**
     * Creates a kill packet and adds it to the queue
     */
    private void addKillPacket() {

        packetsBlockingQueue.add( new PacketBuilder(true));
    }

    /**
     * Initiate the PacketWriter
     * @return thread which holds the PacketWriter
     */
    private Thread initPacketWriteThread(String destFileName) throws IOException {
        PacketWriter packetWrite;

        try {
            packetWrite = new PacketWriter(packetsBlockingQueue, metaData, destFileName);
        }
        catch (IOException e){
            System.err.println(e.getMessage());
            throw e;
        }

        Thread packetWriteThread = new Thread(packetWrite);

        packetWriteThread.start();
        return packetWriteThread;
    }

    /**
     * Initiate a metadata file object.
     */
    private void initMetaData(String destFilePath) {
        this.metaData = MetaData.GetMetaData(getNumOfChunks(), destFilePath + "MetaData.ser");
    }

    /**
     * Create a http get request to get the size in bytes of the requested download file.
     * @return the size of the file in bytes
     */
    private long getFileSize() {

        long fileSize = -1;
        for (URL url : this.urlsList) {
            HttpURLConnection httpConnection;
            try {
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("HEAD");
                fileSize = httpConnection.getContentLengthLong();
                break;
            } catch (IOException ignored) {
            }
        }
        return fileSize;
    }

    /**
     * Calculate the right number of chunks of data that are needed in order to download the file
     * @return int, the amount of ranges
     */
    private int getNumOfChunks() {
        return (fileSize % (long) BUFFER_SIZE == 0) ? (int) (fileSize / BUFFER_SIZE) : (int) (fileSize / BUFFER_SIZE) + 1;
    }

    /**
     * Craetes list which contains all the ranges of the right chunks of data
     * @return the list of the ranges
     */
    private List<long[]> getChunksRanges() {
        List<long[]> chunksRanges = new ArrayList<>();
        IntStream.range(0, getNumOfChunks()).forEach(i ->  chunksRanges.add(getBytesOfChunkRange(i)));
        return chunksRanges;
    }

    /**
     * Calculate the range (start byte and end byte) of the requested chunk
     * @param chunkStartPos the start index of the chunk of packets
     * @return array of tuples which for every tuple 0 - start , 1 - end
     */
    private long[] getBytesOfChunkRange(long chunkStartPos) {
        long chunkStartByte = chunkStartPos * BUFFER_SIZE;
        long chunkEndByte = chunkStartByte + BUFFER_SIZE - 1;
        boolean isValidRange = chunkEndByte < this.fileSize;
        chunkEndByte = isValidRange ? chunkEndByte : this.fileSize;

        return new long[]{chunkStartByte, chunkEndByte};
    }
}



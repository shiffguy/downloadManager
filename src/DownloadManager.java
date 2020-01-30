import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class DownloadManager implements Runnable {
    //region Fields
    private static final int BUFFER_SIZE = 512 * 1000;  // Each download packet size
    private List<URL> urlsList;
    private LinkedBlockingQueue<PacketBuilder> packetDataQueue;
    private ExecutorService packetDownloaderPool;
    private MetaData metaData;
    private long fileSize;
    private static List<long[]> packetPositionsPairs;
    private int urlIndex;
    // endregion

    //region Constructor
    public DownloadManager(List<URL> urlList, int numberOfThreads) {
        this.urlsList = urlList;
        this.packetDataQueue = new LinkedBlockingQueue<>();
        this.packetDownloaderPool = Executors.newFixedThreadPool(numberOfThreads);
        this.urlIndex = 0;
    }
    //endregion

    //region Public methods

    /**
     * Initiate a download process of a single file which includes accumulating download tasks to a packet downloaders
     * pool and running a packet writer thread that write the downloaded packets to the destination file.
     */
    public void run() {
        this.fileSize = this.getFileSize();
        boolean isConnectionEstablish = fileSize != -1;

        if (!isConnectionEstablish) {
            DmUI.printConnectionFailed();
            return;
        }

        String url = this.urlsList.get(0).toString();
        String destinationFilePath = url.substring( url.lastIndexOf('/')+1);

        this.initMetaData(destinationFilePath);
        packetPositionsPairs = this.getPacketsRanges();

        Thread writerThread;
        try {
            writerThread = this.initPacketWriteThread(destinationFilePath);
        } catch (IOException e) {
            return;
        }
        accumulatePackets();
        this.packetDownloaderPool.shutdown();
        try {
            packetDownloaderPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            addKillPacket();
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // endregion

    // region Private methods
    /**
     * Accumulates tasks for the thread pool of the packet downloaders. At the end create a poison pill task to inform
     * the writer that all task are done.
     */
    private void accumulatePackets() {
        int packetIndex = 0;
        for (long[] packetPositions : packetPositionsPairs) {
            boolean isPacketDownloaded = metaData.IsIndexDownloaded(packetIndex);
            if (!isPacketDownloaded) {
                createTask(packetIndex, packetPositions);
            }

            packetIndex++;
        }
    }

    /**
     * Create a new task to the packet downloaders pool.
     * @param packetIndex the index of the packet
     * @param packetPositions long array where at index 0 is the start byte of the packet and index 1 is the end byte of
     *                        the packet
     */
    private void createTask(int packetIndex, long[] packetPositions) {
        URL url = this.urlsList.get(urlIndex);
        long packetStartPosition = packetPositions[0];
        long packetEndPosition = packetPositions[1];
        PacketDownloader packetDownloader = new PacketDownloader(this.packetDataQueue, url,
                packetStartPosition, packetEndPosition, packetIndex, false);

        this.packetDownloaderPool.execute(packetDownloader);
        this.setNextUrlIndex();
    }

    /**
     * Creates a kill packet and adds it to the queue
     */
    private void addKillPacket() {

        packetDataQueue.add( new PacketBuilder(-1, -1, null, true));
    }

    /**
     * Initiate the packet writer thread
     * @return the thread object of the packet writer
     */
    private Thread initPacketWriteThread(String destinationFileName) throws IOException {
        PacketWriter packetWrite;

        try {
            packetWrite = new PacketWriter(packetDataQueue, metaData, destinationFileName);
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
     * Initiate a meta data object.
     */
    private void initMetaData(String destinationFilePath) {
        this.metaData = MetaData.GetMetaData(getRangesAmount(), destinationFilePath + "MetaData.ser");
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
     * Calculate the amount of packet that are needed in order to download the file
     * @return int, the amount of ranges
     */
    private int getRangesAmount() {

        int rangesAmount = (fileSize % (long) BUFFER_SIZE == 0) ? (int) (fileSize / BUFFER_SIZE) : (int) (fileSize / BUFFER_SIZE) + 1;

        return rangesAmount;
    }

    /**
     * Craete a list that contains all the ranges of the packets of the file
     * @return the list of the ranges
     */
    private List<long[]> getPacketsRanges() {
        List<long[]> packetRanges = new ArrayList<>();
        IntStream.range(0, getRangesAmount()).forEach(i ->  packetRanges.add(get_byte_range(i)));
        return packetRanges;
    }

    /**
     * Sets the next index that will choose the next url to download a packet from
     */
    private void setNextUrlIndex() {
        this.urlIndex = this.urlIndex < this.urlsList.size() - 1 ? ++this.urlIndex : 0;
    }

    /**
     * Calculate the range (start byte and end byte) of a given packet
     * @param packetStartPosition the index of the packet
     * @return array where at index 0 is the starting byte range and in index 1 the end byte range
     */
    private long[] get_byte_range(long packetStartPosition) {
        long packetStartByte = packetStartPosition * BUFFER_SIZE;
        long packetEndByte = packetStartByte + BUFFER_SIZE - 1;
        boolean isRangeValid = packetEndByte < this.fileSize;
        packetEndByte = isRangeValid ? packetEndByte : this.fileSize;

        return new long[]{packetStartByte, packetEndByte};
    }
    //endregion
}



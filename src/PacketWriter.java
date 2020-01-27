import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.LinkedBlockingDeque;

public class PacketWriter implements Runnable {

    private String downloadedFilePath;
    private LinkedBlockingDeque<DataWrapper> packetDataQueue;
    private MetaData metaData;
    private int downloadStatus;
    private boolean firstPrint;

    PacketWriter(LinkedBlockingDeque<DataWrapper> packetDataQueue, MetaData metaData, String downloadedFileName) throws IOException {
        this.packetDataQueue = packetDataQueue;
        this.metaData = metaData;
        this.downloadedFilePath = downloadedFileName;
        this.downloadStatus = this.metaData.GetDownloadCounter() / this.metaData.GetNumberOfPackets();
        this.firstPrint = true;
        createDownloadFile();
    }

    /**
     * Write all the downloaded packets to the file and update the user if the file was downloaded successfully or not.
     */
    @Override
    public void run() {
        this.writePackets();
        boolean isAllFileDownloaded = this.metaData.IsDownloadFinished();
        if (isAllFileDownloaded) {
            this.metaData.deleteMetaDataFile();
            System.err.println("Download succeeded");
        }
        else{
            System.err.println("Download Fail, fail to download all packets. Restart download");
        }
    }

    /**
     * Receive new packet from the queue and write the data to the new file. This method will stop when it will receive
     * a poison pill packet which means that all producers finish to handle all tasks
     */
    private void writePackets() {
        boolean isFinnishDownload = false;
        while (!isFinnishDownload) {
            DataWrapper dataToHandle = packetDataQueue.poll();
            if (dataToHandle != null) {
                isFinnishDownload = this.handlePacket(dataToHandle);
            }
        }
    }

    /**
     * Check the type of the packet, if the packet is data packet then this function will write the data to the file
     * @param dataToHandle the last packet the wrter received
     * @return true if the producers finish to download all packets otherwise false
     */
    private boolean handlePacket(DataWrapper dataToHandle){
        boolean isFinnishDownload = this.checkIfPoisonPill(dataToHandle);
        if (!isFinnishDownload) {
            long positionToUpdate = dataToHandle.getPacketNumber();
            int packetIndex = dataToHandle.getPacketIndex();
            byte[] dataToWrite = dataToHandle.getPacket();

            writePacket(dataToWrite, positionToUpdate);
            updateMetaData(packetIndex);
            printDownloadStatus();
        }
        return isFinnishDownload;
    }

    /**
     * Check if a given data wrapper is a data wrapper or a poison pill
     * (poison pill is a message to the writer to terminate)
     *
     * @param dataToHandle the given packet to check
     * @return true if the packet is poison pill otherwise false
     */
    private boolean checkIfPoisonPill(DataWrapper dataToHandle) {
        return dataToHandle.getPacketIndex() == -1;
    }

    /**
     * Prints the current status of the download if the status (the decimal percent of packets downloaded) changed
     */
    private void printDownloadStatus() {
        double downloadCounterStatus = metaData.GetDownloadCounter();
        int status = (int) ((downloadCounterStatus / metaData.GetNumberOfPackets()) * 100);
        if (status != this.downloadStatus || this.firstPrint) {
            this.downloadStatus = status;
            System.err.printf("Downloaded %d%%\n", this.downloadStatus);
            this.firstPrint = false;
        }
    }

    /**
     * Writes the data of a packet into the distance file
     *
     * @param dataToWrite      byte array containing the data need to be written to the file
     * @param positionToUpdate long number represent the position where the data need to written from
     */
    private void writePacket(byte[] dataToWrite, long positionToUpdate) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(downloadedFilePath, "rw")) {

            randomAccessFile.seek(positionToUpdate);
            randomAccessFile.write(dataToWrite);
        } catch (IOException e) {
            System.err.println("Fail to write packet to file");
        }
    }

    /**
     * Create the destination of the download file if doesn't exists
     */
    private void createDownloadFile() throws IOException {
        File myFile = new File(this.downloadedFilePath);
        try {
            boolean ignored = myFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Fail Downloading, could not create new file");
            throw e;
        }
    }

    /**
     * Update the meta data that a packet was downloaded
     * @param positionToUpdate the position of the packet
     */
    private void updateMetaData(int positionToUpdate) {
        metaData.UpdateIndex(positionToUpdate);
    }
}

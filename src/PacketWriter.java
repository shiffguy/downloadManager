import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketWriter implements Runnable {

    private String downloadedFilePath;
    private LinkedBlockingQueue<PacketBuilder> packetDataQueue;
    private MetaData metaData;
    private int downloadStatus;
    private boolean firstPrint;

    PacketWriter(LinkedBlockingQueue<PacketBuilder> packetDataQueue, MetaData metaData, String downloadedFileName) throws IOException {
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
            DmUI.printDownloadSucceeded();
        }
        else{
            DmUI.printDownloadFailed();
        }
    }

    /**
     * Receive new packet from the queue and write the data to the new file. This method will stop when it will receive
     * a poison pill packet which means that all producers finish to handle all tasks
     */
    private void writePackets() {
        boolean isFinishedDownload = false;
        while (!isFinishedDownload) {
            PacketBuilder dataToHandle = packetDataQueue.poll();
            if (dataToHandle != null) {
                isFinishedDownload = this.handlePacket(dataToHandle);
            }
        }
    }

    /**
     * Check the type of the packet, if the packet is data packet then this function will write the data to the file
     * @param dataToHandle the last packet the wrter received
     * @return true if the producers finish to download all packets otherwise false
     */
    private boolean handlePacket(PacketBuilder dataToHandle){
        boolean isFinishedDownload = this.checkIfKill(dataToHandle);
        if (!isFinishedDownload) {
            long positionToUpdate = dataToHandle.getPacketNumber();
            int packetIndex = dataToHandle.getPacketIndex();
            byte[] dataToWrite = dataToHandle.getBytesData();

            writePacket(dataToWrite, positionToUpdate);
            updateMetaData(packetIndex);
            DmUI.printDownloadStatus(metaData, downloadStatus, firstPrint);
            this.firstPrint = false;
        }
        return isFinishedDownload;
    }

    /**
     * Check if a given data wrapper is a data wrapper or a poison pill
     * (poison pill is a message to the writer to terminate)
     *
     * @param dataToHandle the given packet to check
     * @return true if the packet is poison pill otherwise false
     */
    private boolean checkIfKill(PacketBuilder dataToHandle) {
        return dataToHandle.getKillStatus();
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
            DmUI.printFailedToWritePacket();
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
            DmUI.printFileNotCreated();
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

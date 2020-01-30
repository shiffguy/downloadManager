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
            long updatedPosition = dataToHandle.getPacketPosition();
            int packetIndex = dataToHandle.getPacketIndex();
            byte[] dataToWrite = dataToHandle.getBytesData();

            writePacket(dataToWrite, updatedPosition);
            updateMetaData(packetIndex);
            DmUI.printDownloadStatus(metaData, downloadStatus, firstPrint);
            this.firstPrint = false;
        }
        return isFinishedDownload;
    }

    /**
     * Check if a given packet is kill packet in order to end the process
     *
     *
     * @param dataToHandle the given packet to check
     * @return packet.getKillStatus() == true
     */
    private boolean checkIfKill(PacketBuilder dataToHandle) {
        return dataToHandle.getKillStatus();
    }


    /**
     * Writes data using randomAccessFile
     *
     * @param dataToWrite      byte array containing the data need to be written to the file
     * @param updatedPosition long number represent the position where the data need to written from
     */
    private void writePacket(byte[] dataToWrite, long updatedPosition) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(downloadedFilePath, "rw")) {

            randomAccessFile.seek(updatedPosition);
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
            myFile.createNewFile();
        } catch (IOException e) {
            DmUI.printFileNotCreated();
            throw e;
        }
    }

    /**
     * Update the meta data that a packet was downloaded
     * @param updatedPosition the position of the downloaded packet
     */
    private void updateMetaData(int updatedPosition) {
        metaData.UpdateIndex(updatedPosition);
    }
}

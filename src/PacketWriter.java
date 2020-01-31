import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketWriter implements Runnable {

    private String downloadedFilePath;
    private LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue;
    private MetaData metaData;
    private int statusOfProgressDownload;
    private boolean openingPrint;

    PacketWriter(LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue, MetaData metaData, String downloadedFileName) throws IOException {
        this.packetsBlockingQueue = packetsBlockingQueue;
        this.metaData = metaData;
        this.downloadedFilePath = downloadedFileName;
        this.statusOfProgressDownload = this.metaData.GetCounterOfDownloadedPackets() / this.metaData.GetNumberOfChunks();
        this.openingPrint = true;
        createDestFile();
    }

    /**
     * Write all the downloaded packets to the file and update the user if the file was downloaded successfully or not.
     */
    @Override
    public void run() {
        this.pollOutPacketsToWrite();
        boolean isWholeFileCompleteDownload = this.metaData.IsDownloadCompleted();
        if (isWholeFileCompleteDownload) {
            this.metaData.deleteMetaData();
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
    private void pollOutPacketsToWrite() {
        boolean isDownloadCompleted = false;
        while (!isDownloadCompleted) {
            PacketBuilder dataToHandle = packetsBlockingQueue.poll();
            if (dataToHandle != null) {
                isDownloadCompleted = this.processSinglePacketData(dataToHandle);
            }
        }
    }

    /**
     * Check the type of the packet, if the packet is data packet then this function will write the data to the file
     * @param dataOfPacket the last packet the wrter received
     * @return true if the producers finish to download all packets otherwise false
     */
    private boolean processSinglePacketData(PacketBuilder dataOfPacket){
        boolean isDownloadCompleted = this.checkIfKill(dataOfPacket);
        if (!isDownloadCompleted) {
            long updatedPosition = dataOfPacket.getPacketPosition();
            int packetIndex = dataOfPacket.getPacketIndex();
            byte[] dataToWrite = dataOfPacket.getBytesData();

            writeChunkOfData(dataToWrite, updatedPosition);
            metaData.UpdateIndex(packetIndex);
            DmUI.printDownloadStatus(metaData, statusOfProgressDownload, openingPrint);
            this.openingPrint = false;
        }
        return isDownloadCompleted;
    }

    /**
     * Check if a given packet is kill packet in order to end the process
     *
     *
     * @param dataOfPacket the given packet to check
     * @return packet.getKillStatus() == true
     */
    private boolean checkIfKill(PacketBuilder dataOfPacket) {
        return dataOfPacket.getKillStatus();
    }


    /**
     * Writes data using randomAccessFile object
     *
     * @param dataToWrite      byte array containing the data need to be written to the file
     * @param updatedPosition long number represent the position where the data need to written from
     */
    private void writeChunkOfData(byte[] dataToWrite, long updatedPosition) {
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
    private void createDestFile() throws IOException {
        File myFile = new File(this.downloadedFilePath);
        try {
            myFile.createNewFile();
        } catch (IOException e) {
            DmUI.printFileNotCreated();
            throw e;
        }
    }
}

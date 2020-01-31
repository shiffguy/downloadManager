import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.IntStream;

class MetaData implements Serializable {
    private String tempSerPath;
    private String serPath;
    private int[] currStatus;
    private int numOfChunks;
    private int counterOfDownloadedPackets;

    private MetaData(int numOfChunks, String serPath){
        this.serPath = serPath;
        this.tempSerPath =  this.serPath + "-temp";
        this.numOfChunks = numOfChunks;
        this.currStatus = new int[numOfChunks];
        IntStream.range(0, numOfChunks).forEach(i -> this.currStatus[i] = 0);
        this.counterOfDownloadedPackets = 0;
    }


    /***
     * GetMetaData - if the metadata file has been created
     * it will get it, otherwise create new metadata file
     * @param counterOfDownloadedPackets counter of chunks (file size / buffer size)
     * @param serPath name of the dest file for packets
     * @return current MetaData object
     */
    static MetaData GetMetaData(int counterOfDownloadedPackets, String serPath){

        File metaDataFile = new File(serPath).getAbsoluteFile();
        if(!metaDataFile.exists()) return new MetaData(counterOfDownloadedPackets, serPath);
        MetaData metaData = ReadFromDisk(serPath);
        return metaData;
    }

    /***
     * Updates currStatus array with 1 in the current packet index
     * @param indexToUpdate index of the metadata object to update
     */
    void UpdateIndex(int indexToUpdate){
        currStatus[indexToUpdate] = 1;
        writeToDisk();
    }

    boolean IsIndexDownloaded(int indexToCheck){
        return (currStatus[indexToCheck] == 1); }

    boolean IsDownloadCompleted() { return IntStream.of(currStatus).sum() == this.numOfChunks; }

    int GetCounterOfDownloadedPackets(){
        return this.counterOfDownloadedPackets;
    }

    int GetNumberOfPackets() {return this.numOfChunks;}

    /***
     * Read from the metadata file
     */
    private static MetaData ReadFromDisk(String serPath){
        MetaData metaData = null;
        try(FileInputStream fileInputStream = new FileInputStream(serPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)){
            metaData = (MetaData) objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return metaData;
    }

    /***
     * Write to the metadata file
     */
    private void writeToDisk(){
        try(FileOutputStream fileOutputStream = new FileOutputStream(tempSerPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.counterOfDownloadedPackets++;
        File temp = new File(this.tempSerPath);
        Path tempPath = Paths.get(temp.getAbsolutePath());
        File dest = new File(this.serPath).getAbsoluteFile();
        Path destPath = Paths.get(dest.getAbsolutePath());
        boolean isRenamed = false;
        while(!isRenamed){
            try {
                //Atomic_Move in order to be sure in the renaming
                Files.move(tempPath, destPath, StandardCopyOption.ATOMIC_MOVE);
                isRenamed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    void deleteMetaData() {
        File metadataFile = new File(this.serPath);

        if(!metadataFile.delete()){
            DmUI.printFailedToDeleteMetaData(this.serPath);
        }
    }
}

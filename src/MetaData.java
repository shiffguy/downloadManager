import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class MetaData implements Serializable {
    //region Fields
    private String tempSerializationPath;
    private String serializationPath;
    private Boolean[] rangesStatus;
    private int downloadCounter;
    //endregion Fields

    private MetaData(int rangesAmount, String serializationPath){
        this.rangesStatus = initRangesStatus(rangesAmount);
        this.serializationPath = serializationPath;
        this.tempSerializationPath =  this.serializationPath + "-temp";
        this.downloadCounter = 0;
    }

    private Boolean[] initRangesStatus(int rangesAmount) {
        Boolean[] bs = new Boolean[rangesAmount];

        Arrays.fill(bs, false);

        return bs;
    }

    /***
     * Method that provided an access to the metaData object
     * If metaData already exist it will take that object
     * else it will create new MetaData object
     * @param rangesAmount packet ranges amount (file size / buffer size)
     * @param serializationPath name of the file to download
     * @return MetaData object of the current file download
     */
    public static MetaData GetMetaData(int rangesAmount, String serializationPath){
        MetaData metaData;
        File metaDataFile = new File(serializationPath).getAbsoluteFile();
        boolean isDownloadResumed = metaDataFile.exists();

        if(isDownloadResumed){
            metaData = ReadFromDisk(serializationPath);
        }else{
            metaData =  new MetaData(rangesAmount, serializationPath);
        }

        return metaData;
    }

    /***
     * Update the boolean array of the meta data object
     * and commit serialization writing
     * @param indexToUpdate index of the metadata object to update
     */
    public void UpdateIndex(int indexToUpdate){
        rangesStatus[indexToUpdate] = true;
        writeToDisk();
    }

    public boolean IsIndexDownloaded(int indexToCheck){
        return rangesStatus[indexToCheck];
    }

    public boolean IsDownloadFinished() {
        return !Arrays.asList(rangesStatus).contains(false);
    }

    //region Serialization

    /***
     * Commit the MetaData serialization writing
     */
    private void writeToDisk(){
        try(FileOutputStream fileOutputStream = new FileOutputStream(tempSerializationPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.downloadCounter++;
        this.renameFile();
    }

    /***
     * Commit the MetaData serialization reading
     */
    private static MetaData ReadFromDisk(String serializationPath){
        MetaData metaData = null;
        try(FileInputStream fileInputStream = new FileInputStream(serializationPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)){
            metaData = (MetaData) objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return metaData;
    }

    private void renameFile() {
        File tmp = new File(tempSerializationPath);
        Path tmpPath = Paths.get(tmp.getAbsolutePath());
        File destination = new File(serializationPath).getAbsoluteFile();
        Path destinationPath = Paths.get(destination.getAbsolutePath());
        boolean isRenamed = false;
        while(!isRenamed){
            try {
                Files.move(tmpPath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
                isRenamed = true;
            } catch (IOException ignored) { }
        }
    }
    //endregion Serialization

    public int GetDownloadCounter(){
        return this.downloadCounter;
    }

    public int GetNumberOfPackets() {return this.rangesStatus.length;}

    public void deleteMetaDataFile() {
        File metadataFile = new File(this.serializationPath);
        boolean isDeleted = metadataFile.delete();
        if(!isDeleted){
            System.err.printf("Fail to delete metadata file %s\n", this.serializationPath);
        }
    }
}

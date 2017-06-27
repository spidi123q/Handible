package lukeentertainment.example;

/**
 * Created by NAKUL on 3/18/2017.
 */

public class OpencvNativeClass {
    public native static int train(long matAddrRgba,String path,int option,int lastPos);
    public native static int testInput(long matAddrRgba,String path);
    public native static int encryptTrain(long matAddrRgba,String path);
    public native static int decryptTest(long matAddrRgba,String path);
    public native static int trainIndi(long matAddrRgba,String path);
    public native static int processImage(long matAddrRgba);
    public native static int rotateImage(long matAddrRgba,int angle);
    public native static int detectWords(long matAddrRgba);
    public native static int testInputMalayalam(long matAddrRgba,String path);

    public native static int trainMalayalam(long matAddrRgba,String path,int option,int lastPos);

    

}

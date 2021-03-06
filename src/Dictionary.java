import java.io.*;
import java.util.Stack;

//EXPLAIN: The definition of the object you want to send through rabbitMQ queue
public class Dictionary implements Serializable{
    //EXPLAIN: define attributes, method, constructor of your object class here
    Stack<String> dict;
    int number;
    int numberMax;
    String inputHash;
    boolean resultFound;

    public Dictionary(Stack<String> dict){
        this.dict = dict;
    }

    public Dictionary(int number){
        this.number = number;
    }

    public Dictionary(Stack<String> dict, int number){
        this.dict = dict;
        this.number = number;
    }

    public Dictionary(Stack<String> dict, String inputHash){
        this.dict = dict;
        this.inputHash = inputHash;
    }

    public Dictionary(Stack<String> dict, String inputHash, int number){
        this.dict = dict;
        this.inputHash = inputHash;
        this.number = number;
    }

    public Dictionary(Stack<String> dict, String inputHash, int number, int numberMax){
        this.dict = dict;
        this.inputHash = inputHash;
        this.number = number;
        this.numberMax = numberMax;
    }

    public Dictionary(Stack<String> dict, String inputHash, int number, int numberMax, boolean resultFound){
        this.dict = dict;
        this.inputHash = inputHash;
        this.number = number;
        this.numberMax = numberMax;
        this.resultFound = resultFound;
    }

    public void setResultFound(boolean resultFound){
        this.resultFound = resultFound;
    }

    public void setDict(Stack<String> dict){
        this.dict = dict;
    }

    public void setInputHash(String inputHash){
        this.inputHash = inputHash;
    }

    public void setNumber(int number){
        this.number = number;
    }

    public void setNumberMax(int numberMax){
        this.numberMax = numberMax;
    }

    public boolean getResultFound(){
        return this.resultFound;
    }

    public Stack<String> getDict(){
        return this.dict;
    }

    public String getInputHash(){
        return this.inputHash;
    }

    public int getNumber(){
        return this.number;
    }

    public int getNumberMax(){
        return this.numberMax;
    }

    //EXPLAIN: mandatory method for converting object to bytes to send. Call this when you want to send an object. You should keep this as it is
    public byte[] toBytes() {
        byte[]bytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.flush();
            oos.reset();
            bytes = baos.toByteArray();
            oos.close();
            baos.close();
        } catch(IOException e){
            bytes = new byte[] {};
            // Logger.getLogger("bsdlog").error("Unable to write to output stream",e);
            System.out.println("Unable to write to output stream" + e);
        }
        return bytes;
    }

    //EXPLAIN: mandatory method for converting object from bytes to use. Call this when you receive the object. You should keep this as it is
    public static Dictionary fromBytes(byte[] body) {
        Dictionary obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (body);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = (Dictionary)ois.readObject();
            ois.close();
            bis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}

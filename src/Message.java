import java.io.*;

//EXPLAIN: The definition of the object you want to send through rabbitMQ queue
public class Message implements Serializable{
    //EXPLAIN: define attributes, method, constructor of your object class here
    String msg;   
    public Message(String msg){
        this.msg = msg;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
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
    public static Message fromBytes(byte[] body) {
        Message obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (body);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = (Message)ois.readObject();
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

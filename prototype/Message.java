// import java.io.ByteArrayOutputStream;
// import java.io.Serializable;
// import java.io.IOException;
import java.io.*;

public class Message implements Serializable{
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

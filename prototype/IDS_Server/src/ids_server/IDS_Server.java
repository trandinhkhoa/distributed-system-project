/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package ids_server;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author whoami
 */
public class IDS_Server {
    
    private static final int chunkSize = 10000;
    private static String dictPath;
    private static String hashValue;
    
    private static Stack<Stack<String>> chunks = new Stack<>();
    
    private static BufferedReader bufferedReader;
    
    private final static String QUEUE_NAME = "chunks";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2){
            System.out.println("The server require a MD5 hash and dictionnary file.");
            System.out.println("Ex: java 50bb076a040843d741168883f6cb0612 /usr/share/dict/american-english");
        }
        hashValue = args[0];
        dictPath = args[1];
        
        // Create the objects needed for rabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection;
        Channel channel;
        
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
        //This part create the pool of chunks
        try {
            bufferedReader = new BufferedReader(new FileReader(dictPath));
            fillChunkPool();
            //printChunks();
        } catch (FileNotFoundException ex) {
            System.out.println("Error: \"" + dictPath + "\" wasn't found.");
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Error reading from dictionnary.");
            System.exit(1);
        }
        
        // This part will distribute the work to clients ?
        // RabiitMQ might do this for us
        while (!workIsDone()){
            
        }
        
        // Those comments are a basic descrption of the algorithm for the whole system
        // First, the initial server need to bootstrap the others
        // While they are booting, we split the file and make it ready to be sent
        
        // Once everyone booted, we can start the load balancer
        
        // Wait for clients to connect
        // On a client first connection, send him the hash to find
        // Then on other requests, send him chunks to compute
        
        try {
            bufferedReader.close();
        } catch (IOException ex) {
            System.out.println("Error closing dictionnary.");
        }
    }
    
    /**
     * This function take the content of a dictionnary file to split it into chunks to send
     * to the clients.
     * @throws IOException 
     */
    private static void fillChunkPool() throws IOException {
        String s = bufferedReader.readLine();
        Stack stack = new Stack();
        while (s != null){
            if (stack.size() < chunkSize)
                stack.add(s);
            else{
                chunks.add(stack);
                stack = new Stack();
                stack.add(s);
            }
            // TODO: limit password size in the dictionnary ?
            s = bufferedReader.readLine();
        }
        chunks.add(stack);
    }
    
    /**
     * Print the content of chunks, for debugging purposes.
     */
    private static void printChunks(){
        int size = chunks.size();
        for (int i = 0; i < size; i++){
            System.out.println(chunks.get(i).size() + " - " + chunks.get(i).toString());
        }
    }
    
    /**
     * Get a chunk to send to a client.
     * @return 
     */
    private static Stack<String> getChunk(){
        return chunks.pop();
    }

    /**
     * Tell wether there is more work to distribute or not.
     * @return true if there is more word to do, false otherwise.
     */
    private static boolean workIsDone() {
        if (chunks.empty())
            return true;
        return false;
    }
    
}

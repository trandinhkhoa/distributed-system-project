/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newtask;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author whoami
 */
public class NewTask {
    private final static String QUEUE_NAME = "hello";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws java.io.IOException, TimeoutException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = getMessage(args);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println("[x ] Sent '" + message + "'");
        
        channel.close();
        connection.close();
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 1)
            return "Hello World!";
        return joinStrings(strings, " ");
    }

    private static String joinStrings(String[] strings, String delimiter) {
        int length = strings.length;
        if (length == 0) return "";
        StringBuilder words = new StringBuilder(strings[0]);
        for (int i = 1; i < length; i++){
            words.append(delimiter).append(strings[i]);
        }
        
        return words.toString();
    }
    
}

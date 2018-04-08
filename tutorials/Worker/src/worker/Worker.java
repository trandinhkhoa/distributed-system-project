/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worker;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author whoami
 */
public class Worker {
    private final static String QUEUE_NAME = "hello";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws java.io.IOException, java.lang.InterruptedException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("[+] Waiting for messages. To exit, press CTRL+C");
        
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties peroperties, byte[] body) throws IOException {
                String message = new String (body, "UTF-8");
                System.out.println("[X] Received '" + message + "'");
                
                try {
                    doWork(message);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    System.out.println("[x] Done");
                }
            }
        };
        
        boolean autoAck = true;
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
        
    }
    
    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()){
            if (ch == '.') Thread.sleep(1000);
        }
    }
    
}

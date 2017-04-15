package rabbitmqlab;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Technik {

    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("TECHNIK");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel elbow_channel = connection.createChannel();
        final  Channel knee_channel = connection.createChannel();
        final  Channel ankle_channel = connection.createChannel();
        final  Channel response_channel = connection.createChannel();
        final  Channel admin_channel = connection.createChannel();
        final  Channel log_channel = connection.createChannel();
        //receive queue
        final String elbow_queue = "elbow_queue";
        final String knee_queue = "knee_queue";
        final String ankle_queue = "ankle_queue";
        final String response_queue = "response_queue";
        final String admin_queue = "admin_queue";


        elbow_channel.queueDeclare(elbow_queue, false, false, false, null);
        knee_channel.queueDeclare(knee_queue, false, false, false, null);
        ankle_channel.queueDeclare(ankle_queue, false, false, false, null);
        response_channel.queueDeclare(response_queue, false, false, false, null);
        admin_channel.queueDeclare(admin_queue, false, false, false, null);
        response_channel.basicPublish("", response_queue, null, "Hello doctor".getBytes());
        admin_channel.basicPublish("", admin_queue, null, "Hello admin".getBytes());

        // read msg
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter key1: ");
        String key1 = br.readLine();
        System.out.println("Enter key2: ");
        String key2 = br.readLine();

        String log_exchange = "log";
        log_channel.exchangeDeclare(log_exchange, BuiltinExchangeType.FANOUT);
        // queue & bind
        String log_queue = log_channel.queueDeclare().getQueue();
        log_channel.queueBind(log_queue, log_exchange, "");
        System.out.println("created queue: " + log_queue);

        // consumer (message handling)
        Consumer log_consumer = new DefaultConsumer(log_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from admin: " + message);
            }
        };


        // consumer (handle msg)
        Consumer knee_consumer = new DefaultConsumer(knee_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                String response = "";
                response = message.substring("knee".length()) + " results";
                response_channel.basicPublish("", response_queue, null, response.getBytes());
                admin_channel.basicPublish("", admin_queue, null, response.getBytes());
            }
        };


        // consumer (handle msg)
        Consumer elbow_consumer = new DefaultConsumer(elbow_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                String response = "";
                response = message.substring("elbow".length()) + " results";
                response_channel.basicPublish("", response_queue, null, response.getBytes());
                admin_channel.basicPublish("", admin_queue, null, response.getBytes());
            }
        };
        // consumer (handle msg)
        Consumer ankle_consumer = new DefaultConsumer(ankle_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                String response = "";
                response = message.substring("ankle".length()) + " results";
                response_channel.basicPublish("", response_queue, null, response.getBytes());
                admin_channel.basicPublish("", admin_queue, null, response.getBytes());
            }
        };

        log_channel.basicConsume(log_queue, true, log_consumer);

        if(key1.equals("ankle") || key2.equals("ankle")){
            ankle_channel.basicConsume(ankle_queue, true, ankle_consumer);

        }if(key1.equals("elbow") || key2.equals("elbow")){
            elbow_channel.basicConsume(elbow_queue, true, elbow_consumer);

        }if(key1.equals("knee") || key2.equals("knee")){
            knee_channel.basicConsume(knee_queue, true, knee_consumer);

        }

        // start listening
        System.out.println("Waiting for messages...");

    }
}



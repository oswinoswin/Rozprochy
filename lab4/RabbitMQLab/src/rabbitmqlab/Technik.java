package rabbitmqlab;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;

public class Technik {

    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("TECHNIK");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        //receive queue
        String elbow_queue = "elbow_queue";
        channel.queueDeclare(elbow_queue, false, false, false, null);

        // exchange
        final String EXCHANGE_NAME = "exchange";
        final String T_RESPONSE = "t_response";
        final String TASK = "task";

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(T_RESPONSE, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(TASK, BuiltinExchangeType.TOPIC);

        channel.basicQos(1);

        // queue & bind
        final String queueName = channel.queueDeclare().getQueue();
        //channel.queueBind(queueName, EXCHANGE_NAME, "");

        final String key1 = "elbow";
        final String key2 = "knee";
        channel.queueBind(queueName, TASK, key1);
        System.out.println("created queue: " + queueName);
        channel.queueBind(queueName, TASK, key2);
        System.out.println("created queue: " + queueName);

        String message = "Hello doctor!";
        // say hello to the doctor
        channel.basicPublish(T_RESPONSE, "", null, message.getBytes("UTF-8"));
        System.out.println("Sent: " + message);

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                String message = new String(body, "UTF-8");
                System.out.println("Technican received: " + message.substring(key1.length()));

                String response = "";

                if(message.startsWith(key1)){
                    response = message.substring(key1.length()) + " results";
                }else if(message.startsWith(key2)){
                    response = message.substring(key2.length()) + " results";
                }
                // publish
                channel.basicPublish(T_RESPONSE, "", null, response.getBytes("UTF-8"));

            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(queueName, false, consumer);
        channel.basicConsume(elbow_queue, false, consumer);
    }
}

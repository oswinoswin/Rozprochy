package akkalab;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SearchWorker extends AbstractActor {
    // for logging
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    // must be implemented -> creates initial behaviour
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    //System.out.println(s);
                    int result = findPrice("/media/oswin/DATA/00_studia/Rozprochy/5_akka/akka/AkkaLab/data_base1.txt",s);
                    System.out.println("[search worker] price: "+result);
                    if(result == -1){
                        getContext().parent().tell("title " + s + "not found", getSelf());
                    }else{
                        getContext().parent().tell("price of "+ s+ ": " + result, getSelf());
                    }

                })
                .matchAny(s -> {log.info("received unknown message");
                })
                .build();
    }

    // optional
    @Override
    public void preStart() throws Exception {
        log.info("SearchWorker has started!");
    }

    private int findPrice(String dbPath, String toFind){

        //read file into stream, try-with-resources
        try (BufferedReader br = new BufferedReader(new FileReader(dbPath))) {
            String line;
            String title;
            int price;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                title = line.substring(0, line.indexOf(' '));
                if(title.equals(toFind)){
                    price = Integer.valueOf(line.substring(line.indexOf(' ') + 1));
                    //System.out.println("price: " + price);
                    return price;

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

}

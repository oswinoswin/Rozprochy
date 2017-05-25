package akkalab;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class BookShop {

    public static void main(String[] args) throws Exception {

        // create actor system & actors
        //final ActorSystem system = ActorSystem.create("local_system");
        //final ActorRef actor = system.actorOf(Props.create(SearchWorker.class), "searcher");

        File configFile = new File("/media/oswin/DATA/00_studia/Rozprochy/5_akka/akka/AkkaLab/book_shop.conf");
        Config config = ConfigFactory.parseFile(configFile);

        // create actor system & actors
        final ActorSystem system = ActorSystem.create("book_shop", config);
        final ActorRef bookShopActor = system.actorOf(Props.create(BookShopActor.class), "book_shop");
        
        // read line & send to actor
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            else
                bookShopActor.tell(line, null);
        }

        // finish
        system.terminate();
    }
}
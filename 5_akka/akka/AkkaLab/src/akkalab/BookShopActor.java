package akkalab;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class BookShopActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    @Override
    public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class,s -> {
                        System.out.println("BookShopActor received: "+s + " from " + getSender());
                        if(s.startsWith("check")){
                            context().child("searchWorker").get().tell(s.substring(s.indexOf(' ')+1), getSelf());
                        }
                        if(s.startsWith("price")){
                            System.out.println("booksop received price");
                        }
                        //getContext().actorSelection("akka.tcp://local_system@127.0.0.1:2552/user/local").tell("from remote"+s,getSelf());
                    })
                    .matchAny(o -> log.info("received unknown message"))
                    .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchWorker.class), "searchWorker");
    }
}
package actors;

import actorPrtocols.AirlineActorProtocol;
import actorPrtocols.BookingActorProtocol;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import models.Trip;
import scala.Int;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

public class BookingActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(BookingActorProtocol.BookTrip.class,this::bookTrip).build();
    }
    public static Props getProps(){
        return Props.create(BookingActor.class);
    }

    private void bookTrip(BookingActorProtocol.BookTrip bookTrip) throws Exception {
        String source = bookTrip.getSource();
        String destination = bookTrip.getDestination();
        if(source.equals("X")){
            if(destination.equals("Z")){
                ActorSystem system = ActorSystem.create();
                ActorRef aaActor = system.actorOf(AAActor.getProps(), "AAActor");
                Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
                Future<Object> askSeats = Patterns.ask(aaActor, new AirlineActorProtocol.Seats("AA001"),timeout);
                String noOfSeatsAA = (String) Await.result(askSeats, timeout.duration());
                if(Integer.parseInt(noOfSeatsAA)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId = confirmSeat("AA001",trip,aaActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else{
                    sender().tell("No trips available",self());
                }
            }
            else if(destination.equals("W")){
                ActorSystem system = ActorSystem.create();
                ActorRef aaActor = system.actorOf(AAActor.getProps(), "AAActor");
                ActorRef caActor = system.actorOf(CAActor.getProps(), "CAActor");
                Timeout timeout = new Timeout(20, TimeUnit.SECONDS);

                Future<Object> askSeats = Patterns.ask(aaActor, new AirlineActorProtocol.Seats("AA001"),timeout);
                String noOfSeatsAA = (String) Await.result(askSeats, timeout.duration());

                askSeats = Patterns.ask(caActor, new AirlineActorProtocol.Seats("CA002"),timeout);
                String noOfSeatsCA = (String) Await.result(askSeats, timeout.duration());

                if(Integer.parseInt(noOfSeatsAA)>0 && Integer.parseInt(noOfSeatsCA)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("AA001",trip,aaActor,timeout);
                    long confirmationId2 = confirmSeat("CA002",trip,caActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else{
                    sender().tell("No trips available",self());
                }
            }
        }
    }
    public long confirmSeat(String flightNo,Trip trip, ActorRef actor, Timeout timeout) throws Exception {

        Future<Object> requestHold = Patterns.ask(actor, new AirlineActorProtocol.HoldMessage(""+flightNo,trip), timeout);
        String holdReply = (String) Await.result(requestHold, timeout.duration());
        long bookingId = Long.parseLong(""+holdReply.charAt(holdReply.length()-1));

        Future<Object> requestConfirm = Patterns.ask(actor, new AirlineActorProtocol.ConfirmMessage(""+bookingId), timeout);
        String confirmReply = (String) Await.result(requestConfirm, timeout.duration());
        long confirmationId = Long.parseLong(""+confirmReply.charAt(confirmReply.length()-1));

        return confirmationId;

    }
    public Trip createTrip(String source, String destination){
        Trip trip = new Trip();
        trip.source=source;
        trip.destination = destination;
        trip.save();
        return trip;
    }
}

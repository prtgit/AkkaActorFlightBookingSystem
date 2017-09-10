package actors;

import actorPrtocols.AirlineActorProtocol;
import actorPrtocols.BookingActorProtocol;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import models.Booking;
import models.Operator;
import models.Trip;
import play.libs.Json;
import scala.Int;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(BookingActorProtocol.BookTrip.class,this::bookTrip)
                .match(BookingActorProtocol.AllTrips.class,this::getAllTrips)
                .match(BookingActorProtocol.SingleTrip.class,this::getSingleTrip)
                .match(BookingActorProtocol.FlightSeats.class, this::getFlightSeats)
                .match(BookingActorProtocol.OperatorFlights.class,this::getFlights)
                .match(BookingActorProtocol.Operators.class,this::getOperators)
                .build();
    }

    private void getOperators(BookingActorProtocol.Operators operators) {
        List<Operator> operatorsList = Operator.find.all();
        List<String> operatorStringList = new ArrayList<String>();
        for(Operator operator:operatorsList){
            operatorStringList.add(operator.operatorName);
        }
        sender().tell(operatorStringList,self());
    }

    private void getFlights(BookingActorProtocol.OperatorFlights operatorFlights) {
        String operator = operatorFlights.operator;
        SqlQuery countQuery = Ebean.createSqlQuery("SELECT COUNT(*) FROM operator WHERE operator.op_code=:operator");
        countQuery.setParameter("operator",""+operator);
        SqlRow operatorCount = countQuery.findOne();
        if(Integer.parseInt(operatorCount.getString("count(*)"))<=0){
            sender().tell("No operator exists with the passed operator code",self());
        }
        else {
            SqlQuery query = Ebean.createSqlQuery("SELECT flight_no FROM flight WHERE flight.operator_id = (select id from operator where operator.op_code=:operator);");
            query.setParameter("operator",""+operator);
            List<SqlRow> flights = query.findList();
            sender().tell(flights,self());
        }
    }

    public static Props getProps(){
        return Props.create(BookingActor.class);
    }

    private void getFlightSeats(BookingActorProtocol.FlightSeats flightSeats) throws Exception {
        ActorSystem system = ActorSystem.create();
        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);

        ActorRef aaActor = system.actorOf(AAActor.getProps(), "AAActor");
        ActorRef caActor = system.actorOf(CAActor.getProps(), "CAActor");
        ActorRef baActor = system.actorOf(BAActor.getProps(), "BAActor");
        String noOfSeats ="";

        if(flightSeats.operator.equals("AA")){
            Future<Object> askSeats = Patterns.ask(aaActor, new AirlineActorProtocol.Seats(""+flightSeats.flight),timeout);
            noOfSeats = (String) Await.result(askSeats, timeout.duration());
            sender().tell(noOfSeats,self());
        }
        else if(flightSeats.operator.equals("BA")){
            Future<Object> askSeats = Patterns.ask(baActor, new AirlineActorProtocol.Seats(""+flightSeats.flight),timeout);
            noOfSeats = (String) Await.result(askSeats, timeout.duration());
            sender().tell(noOfSeats,self());
        }
        else if(flightSeats.operator.equals("CA")){
            Future<Object> askSeats = Patterns.ask(caActor, new AirlineActorProtocol.Seats(""+flightSeats.flight),timeout);
            noOfSeats = (String) Await.result(askSeats, timeout.duration());
            sender().tell(noOfSeats,self());
        }
        else{
            sender().tell("No such flight",self());
        }
    }


    private void getSingleTrip(BookingActorProtocol.SingleTrip singleTrip) {
        Trip trip = Trip.find.byId(Long.parseLong(singleTrip.tripID));
        if(trip == null){
            sender().tell("No such Trip Id",self());
        }
        List<Booking> tripBookings = trip.bookings;
        List<String> flightCodeList = new ArrayList<>();
        for(Booking booking:tripBookings){
            flightCodeList.add(booking.flightNo);
        }
        sender().tell(flightCodeList,self());
    }


    private void getAllTrips(BookingActorProtocol.AllTrips p) {
        List<Trip> tripsList = Trip.find.all();
        List<Long> tripIDList = new ArrayList<Long>();
        for(Trip trip: tripsList){
            tripIDList.add(trip.id);
        }
        sender().tell(tripIDList,self());
    }

    private void bookTrip(BookingActorProtocol.BookTrip bookTrip) throws Exception {
        String source = bookTrip.getSource();
        String destination = bookTrip.getDestination();
        ActorSystem system = ActorSystem.create();
        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);

        ActorRef aaActor = system.actorOf(AAActor.getProps(), "AAActor");
        ActorRef caActor = system.actorOf(CAActor.getProps(), "CAActor");
        ActorRef baActor = system.actorOf(BAActor.getProps(), "BAActor");


        Future<Object> askSeats = Patterns.ask(aaActor, new AirlineActorProtocol.Seats("AA001"),timeout);
        String noOfSeatsAA001 = (String) Await.result(askSeats, timeout.duration());

        askSeats = Patterns.ask(aaActor, new AirlineActorProtocol.Seats("AA002"),timeout);
        String noOfSeatsAA002 = (String) Await.result(askSeats, timeout.duration());

        askSeats = Patterns.ask(baActor, new AirlineActorProtocol.Seats("BA001"),timeout);
        String noOfSeatsBA001 = (String) Await.result(askSeats, timeout.duration());

        askSeats = Patterns.ask(caActor, new AirlineActorProtocol.Seats("CA001"),timeout);
        String noOfSeatsCA001 = (String) Await.result(askSeats, timeout.duration());

        askSeats = Patterns.ask(caActor, new AirlineActorProtocol.Seats("CA002"),timeout);
        String noOfSeatsCA002 = (String) Await.result(askSeats, timeout.duration());

        if(source.equals("X")){
            if(destination.equals("Z")){
                if(Integer.parseInt(noOfSeatsAA001)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId = confirmSeat("AA001",trip,aaActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else{
                    sender().tell("No trips available",self());
                }
            }
            else if(destination.equals("W")){
                if(Integer.parseInt(noOfSeatsAA001)>0 && Integer.parseInt(noOfSeatsCA002)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("AA001",trip,aaActor,timeout);
                    long confirmationId2 = confirmSeat("CA002",trip,caActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else{
                    sender().tell("No trips available",self());
                }
            }
            else if(destination.equals("Y")){
                if(Integer.parseInt(noOfSeatsCA001)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("CA001",trip,caActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else if(Integer.parseInt(noOfSeatsAA001)>0 && Integer.parseInt(noOfSeatsBA001)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("AA001",trip,aaActor,timeout);
                    long confirmationId2 = confirmSeat("BA001",trip,baActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else if(Integer.parseInt(noOfSeatsAA001)>0 && Integer.parseInt(noOfSeatsCA002)>0 && Integer.parseInt(noOfSeatsAA002)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("AA001",trip,aaActor,timeout);
                    long confirmationId2 = confirmSeat("CA002",trip,caActor,timeout);
                    long confirmationId3 = confirmSeat("AA002",trip,aaActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else {
                    sender().tell("No trips available",self());
                }
            }
            else{
                sender().tell("No trips available",self());
            }
        }
        else if(source.equals("Z")){
            if(destination.equals("W")){
                if(Integer.parseInt(noOfSeatsCA002)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("CA002",trip,caActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else {
                    sender().tell("No trips available",self());
                }
            }
            else if(destination.equals("Y")){
                if(Integer.parseInt(noOfSeatsBA001)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("BA001",trip,baActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else if(Integer.parseInt(noOfSeatsCA002)>0 && Integer.parseInt(noOfSeatsAA002)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("CA002",trip,caActor,timeout);
                    long confirmationId2 = confirmSeat("AA002",trip,aaActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else {
                    sender().tell("No trips available",self());
                }
            }
            else {
                sender().tell("No trips available",self());
            }
        }
        else if(source.equals("W")){
            if(destination.equals("Y")){
                if(Integer.parseInt(noOfSeatsAA002)>0){
                    Trip trip = createTrip(source,destination);
                    long confirmationId1 = confirmSeat("AA002",trip,aaActor,timeout);
                    sender().tell(""+trip.id,self());
                }
                else {
                    sender().tell("No trips available",self());
                }
            }
            else {
                sender().tell("No trips available",self());
            }
        }
        else{
            sender().tell("No trips available",self());
        }
    }
    public long confirmSeat(String flightNo,Trip trip, ActorRef actor, Timeout timeout) throws Exception {

        Future<Object> requestHold = Patterns.ask(actor, new AirlineActorProtocol.HoldMessage(""+flightNo,trip), timeout);
        String holdReply = (String) Await.result(requestHold, timeout.duration());
        long bookingId = Long.parseLong(""+holdReply);

        Future<Object> requestConfirm = Patterns.ask(actor, new AirlineActorProtocol.ConfirmMessage(""+bookingId), timeout);
        String confirmReply = (String) Await.result(requestConfirm, timeout.duration());
        long confirmationId = Long.parseLong(""+confirmReply);

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

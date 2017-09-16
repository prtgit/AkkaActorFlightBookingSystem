package controllers;

import actorPrtocols.AirlineActorProtocol;
import actorPrtocols.BookingActorProtocol;
import actors.AAActor;
import actors.BAActor;
import actors.BookingActor;
import actors.CAActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Booking;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.Await;
import scala.concurrent.Future;



import static akka.pattern.Patterns.ask;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FlightBookingController extends Controller {
    final ActorRef aaActor, baActor, caActor, bookingActor;
    @Inject
    public FlightBookingController(ActorSystem system) {
        aaActor = system.actorOf(AAActor.getProps());
        baActor = system.actorOf(BAActor.getProps());
        caActor = system.actorOf(CAActor.getProps());
        bookingActor =system.actorOf(BookingActor.getProps(aaActor,baActor,caActor));
    }

    public Result getFlightOperators() throws Exception {

        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askOperatorts = Patterns.ask(bookingActor, new BookingActorProtocol.Operators(),timeout);
        ObjectNode operatorsJson = Json.newObject();
        operatorsJson.put("status","success");
        operatorsJson.put("operators",""+(List)Await.result(askOperatorts,timeout.duration()));
        return ok(operatorsJson);
    }

    public Result getFlights(String operator) throws Exception {

        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askOperatorFlights = Patterns.ask(bookingActor, new BookingActorProtocol.OperatorFlights(operator),timeout);
        if(Await.result(askOperatorFlights,timeout.duration()) instanceof List){
            ObjectNode flightsJson = Json.newObject();
            flightsJson.put("status","success");
            flightsJson.put("flights",""+(List)Await.result(askOperatorFlights,timeout.duration()));
            return ok(flightsJson);
        }
        else {
            ObjectNode flightsJson = Json.newObject();
            flightsJson.put("status","error");
            flightsJson.put("flights",""+(String)Await.result(askOperatorFlights,timeout.duration()));
            return notFound(flightsJson);
        }
    }

    public Result getFlight(String operator, String flight) throws Exception {

        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askFlightSeats = Patterns.ask(bookingActor, new BookingActorProtocol.FlightSeats(operator,flight),timeout);
        String askSeatsResponse = (String)Await.result(askFlightSeats,timeout.duration());
        if(StringUtils.isNumeric(askSeatsResponse)){
            ObjectNode seatsJson = Json.newObject();
            seatsJson.put("status","success");
            seatsJson.put("seats",""+askSeatsResponse);
            return ok(seatsJson);
        }
        else{
            ObjectNode seatsJson = Json.newObject();
            seatsJson.put("status","error");
            seatsJson.put("message",""+askSeatsResponse);
            return ok(seatsJson);
        }
    }
    public Result createTrip(String from, String to) throws Exception {

        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askTrip = Patterns.ask(bookingActor, new BookingActorProtocol.BookTrip(from,to),timeout);
        String askTripResponse = (String)Await.result(askTrip,timeout.duration());
        System.out.println(askTripResponse);
        if(StringUtils.isNumeric(askTripResponse)){
            ObjectNode tripJson = Json.newObject();
            tripJson.put("status","success");
            tripJson.put("tripID",""+askTripResponse);
            return ok(tripJson);
        }
        else{
            ObjectNode tripJson = Json.newObject();
            tripJson.put("status","error");
            tripJson.put("message",""+askTripResponse);
            return notFound(tripJson);
        }
    }

    public Result getTrips() throws Exception {

        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askTrips = Patterns.ask(bookingActor, new BookingActorProtocol.AllTrips(),timeout);
        List<Long> askTripsResponse = (List<Long>) Await.result(askTrips,timeout.duration());
        ObjectNode tripsJson = Json.newObject();
        tripsJson.put("status","success");
        tripsJson.put("trips",""+askTripsResponse);
        return ok(tripsJson);
    }

    public Result getTrip(String tripID) throws Exception {

        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askTrip = Patterns.ask(bookingActor, new BookingActorProtocol.SingleTrip(tripID),timeout);
        if(Await.result(askTrip,timeout.duration()) instanceof  List){
            List<String> askTripResponse = (List<String>) Await.result(askTrip,timeout.duration());
            ObjectNode tripJson = Json.newObject();
            tripJson.put("status","success");
            tripJson.put("segments",""+askTripResponse);
            return ok(tripJson);
        }
        else {
            String askTripResponse = (String) Await.result(askTrip,timeout.duration());
            ObjectNode tripJson = Json.newObject();
            tripJson.put("status","error");
            tripJson.put("message",""+askTripResponse);
            return ok(tripJson);
        }

    }
    public Result confirmFail(String airline) throws Exception {

        Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
        ActorRef actorRef = null;
        if(airline.equals("AA")){
            actorRef = aaActor;
        }
        else if(airline.equals("BA")){
            actorRef = baActor;
        }
        else if(airline.equals("CA")){
            actorRef = caActor;
        }
        if(actorRef != null){
            Future<Object> debugAsk = Patterns.ask(actorRef, new AirlineActorProtocol.DebugConfirmFail(),timeout);
            String result = (String)Await.result(debugAsk, timeout.duration());
            System.out.println("Confirm Fail Response = "+result);
            ObjectNode confirmFailJson = Json.newObject();
            confirmFailJson.put("status","success");
            return ok(confirmFailJson);
        }
        else{
            ObjectNode confirmFailJson = Json.newObject();
            confirmFailJson.put("status","Airline not found");
            return notFound(confirmFailJson);
        }
    }
    public Result confirmNoResponse(String airline)throws Exception{

        Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
        ActorRef actorRef = null;
        if(airline.equals("AA")){
            actorRef = aaActor;
        }
        else if(airline.equals("BA")){
            actorRef = baActor;
        }
        else if(airline.equals("CA")){
            actorRef = caActor;
        }
        if(actorRef != null){
            Future<Object> debugAsk = Patterns.ask(actorRef, new AirlineActorProtocol.DebugConfirmNoResponse(),timeout);
            /*String result = (String)Await.result(debugAsk, timeout.duration());
            System.out.println("Confirm NoResponse reply = "+result);*/
            ObjectNode confirmNoResponse = Json.newObject();
            confirmNoResponse.put("status","success");
            return ok(confirmNoResponse);
        }
        else{
            ObjectNode confirmNoResponse = Json.newObject();
            confirmNoResponse.put("status","Airline not found");
            return notFound(confirmNoResponse);
        }
    }
    public Result reset(String airline) throws Exception{

        Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
        ActorRef actorRef = null;
        if(airline.equals("AA")){
            actorRef = aaActor;
        }
        else if(airline.equals("BA")){
            actorRef = baActor;
        }
        else if(airline.equals("CA")){
            actorRef = caActor;
        }
        if(actorRef != null){
            Future<Object> debugAsk = Patterns.ask(actorRef, new AirlineActorProtocol.DebugReset(),timeout);
            /*String result = (String)Await.result(debugAsk, timeout.duration());
            System.out.println("Confirm NoResponse reply = "+result);*/
            ObjectNode resetJson = Json.newObject();
            resetJson.put("status","success");
            return ok(resetJson);
        }
        else{
            ObjectNode resetJson = Json.newObject();
            resetJson.put("status","Airline not found");
            return notFound(resetJson);
        }
    }



}

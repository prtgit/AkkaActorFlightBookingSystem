package controllers;

import actorPrtocols.AirlineActorProtocol;
import actorPrtocols.BookingActorProtocol;
import actors.AAActor;
import actors.BookingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.*;
import models.Operator;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.Await;
import scala.concurrent.Future;

import static akka.pattern.Patterns.ask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FlightBookingController extends Controller {

    public Result getFlightOperators() throws Exception {
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askOperatorts = Patterns.ask(bookingActor, new BookingActorProtocol.Operators(),timeout);
        ObjectNode operatorsJson = Json.newObject();
        operatorsJson.put("status","success");
        operatorsJson.put("operators",""+(List)Await.result(askOperatorts,timeout.duration()));
        return ok(operatorsJson);
    }

    public Result getFlights(String operator) throws Exception {
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
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
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
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
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
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
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
        Timeout timeout = new Timeout(50, TimeUnit.SECONDS);
        Future<Object> askTrips = Patterns.ask(bookingActor, new BookingActorProtocol.AllTrips(),timeout);
        List<Long> askTripsResponse = (List<Long>) Await.result(askTrips,timeout.duration());
        ObjectNode tripsJson = Json.newObject();
        tripsJson.put("status","success");
        tripsJson.put("trips",""+askTripsResponse);
        return ok(tripsJson);
    }

    public Result getTrip(String tripID) throws Exception {
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
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




}

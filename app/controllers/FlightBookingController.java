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

    public Result getFlightOperators(){
        List<Operator> operators = Ebean.find(Operator.class).select("operatorName").findList();
        com.fasterxml.jackson.databind.node.ObjectNode operatorsJson = Json.newObject();
        operatorsJson.put("status","success");
        List<String> operatorStringList = new ArrayList<String>();
        for(Operator operator:operators){
            operatorStringList.add(operator.operatorName);
        }
        operatorsJson.put("operators",""+operatorStringList);
        return ok(operatorsJson);
    }

    public Result getFlights(String operator){
        SqlQuery countQuery = Ebean.createSqlQuery("SELECT COUNT(*) FROM operator WHERE operator.op_code=:operator");
        countQuery.setParameter("operator",""+operator);
        SqlRow operatorCount = countQuery.findOne();
        if(Integer.parseInt(operatorCount.getString("count(*)"))<=0){
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status","error");
            errorJson.put("message","No operator exists with the passed operator code");
            return notFound(errorJson);
        }
        else {
            SqlQuery query = Ebean.createSqlQuery("SELECT flight_no FROM flight WHERE flight.operator_id = (select id from operator where operator.op_code=:operator);");
            query.setParameter("operator",""+operator);
            List<SqlRow> flights = query.findList();
            ObjectNode flightsJson = Json.newObject();
            flightsJson.put("status","success");
            flightsJson.put("flights",""+flights);
            return ok(flightsJson);
        }
    }

    public Result getFlight(String operator, String flight) throws Exception {
        /*ActorSystem system = ActorSystem.create();
        ActorRef aaActor = system.actorOf(AAActor.getProps(), "AAActor");
        Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
        Future<Object> askSeats = Patterns.ask(aaActor, new AirlineActorProtocol.Seats("AA001"), 5000);
        String noOfSeats = (String) Await.result(askSeats, timeout.duration());
        System.out.println("No Of Seats = "+noOfSeats);

        Future<Object> requestHold = Patterns.ask(aaActor, new AirlineActorProtocol.HoldMessage("AA001"), timeout);
        String holdReply = (String) Await.result(requestHold, timeout.duration());
        System.out.println("Hold Reply = "+holdReply);
        long bookingId = Long.parseLong(""+holdReply.charAt(holdReply.length()-1));
        Thread.sleep(3000);

        Future<Object> requestConfirm = Patterns.ask(aaActor, new AirlineActorProtocol.ConfirmMessage(""+bookingId), timeout);
        String confirmReply = (String) Await.result(requestConfirm,timeout.duration());
        System.out.println("Confirm Reply = "+confirmReply);

*/
        SqlQuery countFlightQuery = Ebean.createSqlQuery("SELECT COUNT(*) FROM flight where flight.flight_no=:flightNo and flight.operator_id=(select operator.id from operator where operator.op_code=:opCode);");
        countFlightQuery.setParameter("flightNo",""+flight);
        countFlightQuery.setParameter("opCode",""+operator);
        SqlRow flightCount = countFlightQuery.findOne();
        if(Integer.parseInt(flightCount.getString("count(*)"))<=0){
            ObjectNode errorJson = Json.newObject();
            errorJson.put("status","error");
            errorJson.put("message","Flight not found");
            return notFound(errorJson);
        }
        else{
            SqlQuery flightQuery = Ebean.createSqlQuery("SELECT available_seats FROM flight where flight.flight_no=:flightNo and flight.operator_id=(select operator.id from operator where operator.op_code=:opCode);");
            flightQuery.setParameter("flightNo",""+flight);
            flightQuery.setParameter("opCode",""+operator);
            SqlRow flightResult = flightQuery.findOne();
            ObjectNode flightJson = Json.newObject();
            flightJson.put("status","success");
            flightJson.put("seats",""+flightResult.getString("available_seats"));
            return ok(flightJson);
        }
    }
    public Result createTrip(String from, String to) throws Exception {
        ActorSystem system = ActorSystem.create();
        ActorRef bookingActor = system.actorOf(BookingActor.getProps(), "BookingActor");
        Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
        Future<Object> askTrip = Patterns.ask(bookingActor, new BookingActorProtocol.BookTrip(from,to),timeout);
        String askTripResponse = (String)Await.result(askTrip,timeout.duration());
        System.out.println(askTripResponse);

        ObjectNode tripJson = Json.newObject();
        tripJson.put("status","success");
        tripJson.put("tripId",""+askTripResponse);
        return ok(tripJson);
    }




}

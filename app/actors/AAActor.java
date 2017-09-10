package actors;

import actorPrtocols.AirlineActorProtocol;
import akka.actor.*;
import io.ebean.*;
import models.Booking;

import java.util.Timer;
import java.util.TimerTask;

public class AAActor extends AbstractActor {

    private Timer timer = new Timer();
    public static Props getProps(){
        return Props.create(AAActor.class);
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(AirlineActorProtocol.ConfirmMessage.class, this::bookTicket).match(AirlineActorProtocol.Seats.class, this::getSeats).match(AirlineActorProtocol.HoldMessage.class, this::holdTicket).build();
    }

    private void holdTicket(AirlineActorProtocol.HoldMessage holdMessage) {
        Booking booking = new Booking();
        booking.flightNo = holdMessage.getFlightNo();
        booking.status = "On Hold";
        booking.trip = holdMessage.getTrip();
        booking.save();
        sender().tell("The seat is on Hold. Booking id="+booking.id,self());
        startTimer(booking.id);
    }

    private void bookTicket(AirlineActorProtocol.ConfirmMessage confirmMessage) {
        stopTimer();
        SqlQuery checkStatusQuery = Ebean.createSqlQuery("SELECT status FROM booking where booking.id=:bookingId;");
        checkStatusQuery.setParameter("bookingId",""+confirmMessage.getBookingId());
        SqlRow flightCount = checkStatusQuery.findOne();
        String bookingStatus = flightCount.getString("status");
        if(bookingStatus.equals("On Hold")){
            SqlUpdate updateBookingStatus = Ebean.createSqlUpdate("UPDATE booking SET status='Confirmed' WHERE booking.id =:bookingId ");
            updateBookingStatus.setParameter("bookingId",confirmMessage.getBookingId());
            updateBookingStatus.execute();

            SqlUpdate countFlightQuery = Ebean.createSqlUpdate("UPDATE flight set available_seats = available_seats - 1 where flight_no=(select flight_no from booking where booking.id=:bookingId) and available_seats > 0;");
            countFlightQuery.setParameter("bookingId",""+confirmMessage.getBookingId());
            countFlightQuery.execute();
            sender().tell("The seat has been booked. Booking id ="+confirmMessage.getBookingId(),self());
        }
        else{
            sender().tell("The seat is not on hold",self());
        }
    }

    private void getSeats(AirlineActorProtocol.Seats seats) {
        SqlQuery flightQuery = Ebean.createSqlQuery("SELECT available_seats FROM flight where flight.flight_no=:flightNo and flight.operator_id=(select operator.id from operator where operator.op_code=:opCode);");
        flightQuery.setParameter("flightNo",""+seats.getFlightNo());
        flightQuery.setParameter("opCode","AA");
        SqlRow flightResult = flightQuery.findOne();
        sender().tell(flightResult.getString("available_seats"),self());
    }

    private void startTimer(Long bookingId) {
        timer.schedule(new TimerTask() {
            int n=0;
            @Override
            public void run(){
                if(++n == 15){
                    SqlUpdate deleteBooking = Ebean.createSqlUpdate("DELETE FROM booking WHERE booking.id=:bookingId");
                    deleteBooking.setParameter("bookingId",bookingId);
                    deleteBooking.execute();
                    sender().tell("Hold on the booking has timed out",self());
                }
            }
        },1000,1000);
    }

    private void stopTimer(){
        timer.cancel();
    }
}

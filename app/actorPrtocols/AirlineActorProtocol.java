package actorPrtocols;

import models.Trip;

public class AirlineActorProtocol {
    public static class Seats{
        private String flightNo;
        public Seats(String flightNo){
            this.flightNo = flightNo;
        }
        public String getFlightNo() {
            return flightNo;
        }
    }
    public static class HoldMessage{
        private String flightNo;
        private Trip trip;
        public HoldMessage(String flightNo, Trip trip){
            this.flightNo = flightNo;
            this.trip = trip;
        }
        public String getFlightNo() {
            return flightNo;
        }
        public Trip getTrip() {
            return trip;
        }
    }
    public static class ConfirmMessage{
        private String bookingId;
        public ConfirmMessage(String bookingId){
            this.bookingId = bookingId;
        }
        public String getBookingId() {
            return bookingId;
        }
    }
}

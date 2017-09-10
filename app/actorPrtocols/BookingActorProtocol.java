package actorPrtocols;

public class BookingActorProtocol {
    public static class BookTrip{
        private String source,destination;
        public BookTrip(String source, String destination){
            this.source = source;
            this.destination = destination;
        }
        public String getSource() {
            return source;
        }
        public String getDestination() {
            return destination;
        }

    }
    public static class AllTrips{}
    public static class SingleTrip{
        public String tripID;
        public SingleTrip(String tripID){
            this.tripID = tripID;
        }
    }
    public static class FlightSeats{
        public String operator,flight;
        public FlightSeats(String operator, String flight){
            this.operator = operator;
            this.flight = flight;
        }
    }
    public static class OperatorFlights{
        public String operator;
        public OperatorFlights(String operator){
            this.operator = operator;
        }
    }
    public static class Operators{}
}

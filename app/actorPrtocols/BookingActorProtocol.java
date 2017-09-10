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
}

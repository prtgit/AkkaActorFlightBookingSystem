package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
public class Flight extends Model {

    @Id@GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;

    public String flightNo;
    public String availableSeats;

    @ManyToOne
    public Operator operator;

    public static final Finder<Long, Flight> find = new Finder<>(Flight.class);

}

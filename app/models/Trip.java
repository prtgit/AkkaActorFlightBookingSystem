package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Trip extends Model {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String source;
    public String destination;

    @OneToMany(mappedBy = "trip")
    public List<Booking> bookings = new ArrayList<Booking>();
    public static final Finder<Long, Trip> find = new Finder<Long, Trip>(Trip.class);
}

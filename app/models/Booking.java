package models;

import io.ebean.Model;

import javax.persistence.*;

@Entity
public class Booking extends Model{
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String flightNo;
    public String status;
    @ManyToOne
    public Trip trip;

}

package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Operator extends Model {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String opCode;
    public String operatorName;

    @OneToMany(mappedBy = "operator")
    public List<Flight> flights = new ArrayList<>();

    public static final Finder<Long, Operator> find = new Finder<>(Operator.class);


}

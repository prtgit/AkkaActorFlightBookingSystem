# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table booking (
  id                            integer not null,
  flight_no                     varchar(255),
  status                        varchar(255),
  trip_id                       integer,
  constraint pk_booking primary key (id),
  foreign key (trip_id) references trip (id) on delete restrict on update restrict
);

create table flight (
  id                            integer not null,
  flight_no                     varchar(255),
  available_seats               varchar(255),
  operator_id                   integer,
  constraint pk_flight primary key (id),
  foreign key (operator_id) references operator (id) on delete restrict on update restrict
);

create table operator (
  id                            integer not null,
  op_code                       varchar(255),
  operator_name                 varchar(255),
  constraint pk_operator primary key (id)
);

create table trip (
  id                            integer not null,
  source                        varchar(255),
  destination                   varchar(255),
  constraint pk_trip primary key (id)
);


# --- !Downs

drop table if exists booking;

drop table if exists flight;

drop table if exists operator;

drop table if exists trip;


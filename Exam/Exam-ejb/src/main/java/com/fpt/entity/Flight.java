package com.fpt.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Flight")
@NamedQueries({
        @NamedQuery(name = "Flight.findAll", query = "SELECT f FROM Flight f"),
        @NamedQuery(name = "Flight.searchFlights", query = "SELECT f FROM Flight f WHERE f.departure = :departure "
                + "AND f.destination = :destination AND f.availableSeats > 0")
})
public class Flight implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "FlightNo")
    private String flightNo;

    @Basic(optional = false)
    @Column(name = "Departure")
    private String departure;

    @Basic(optional = false)
    @Column(name = "Destination")
    private String destination;

    @Basic(optional = false)
    @Column(name = "DepartureTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date departureTime;

    @Basic(optional = false)
    @Column(name = "AvailableSeats")
    private int availableSeats;

    @Basic(optional = false)
    @Column(name = "Price")
    private BigDecimal price;

    public Flight() {
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public int hashCode() {
        return flightNo != null ? flightNo.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Flight))
            return false;
        Flight other = (Flight) object;
        return (this.flightNo != null) && this.flightNo.equals(other.flightNo);
    }

    @Override
    public String toString() {
        return "Flight[ flightNo=" + flightNo + " ]";
    }
}

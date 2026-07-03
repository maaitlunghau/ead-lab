package com.fpt.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Booking")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "BookingId")
    private Integer bookingId;

    @Basic(optional = false)
    @Column(name = "FlightNo")
    private String flightNo;

    @Basic(optional = false)
    @Column(name = "CustomerName")
    private String customerName;

    @Basic(optional = false)
    @Column(name = "SeatsBooked")
    private int seatsBooked;

    @Basic(optional = false)
    @Column(name = "TotalPrice")
    private BigDecimal totalPrice;

    @Basic(optional = false)
    @Column(name = "BookingDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    public Booking() {
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    @Override
    public int hashCode() {
        return bookingId != null ? bookingId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Booking))
            return false;
        Booking other = (Booking) object;
        return (this.bookingId != null) && this.bookingId.equals(other.bookingId);
    }

    @Override
    public String toString() {
        return "Booking[ bookingId=" + bookingId + " ]";
    }
}

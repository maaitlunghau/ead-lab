package com.fpt.sb;

import com.fpt.entity.Booking;
import com.fpt.entity.Flight;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Stateless
public class FlightFacade implements FlightFacadeLocal {

    @PersistenceContext(unitName = "FlightPU")
    private EntityManager em;

    @Override
    public List<Flight> searchFlights(String departure, String destination) {
        TypedQuery<Flight> q = em.createNamedQuery("Flight.searchFlights", Flight.class);

        q.setParameter("departure", departure.trim());
        q.setParameter("destination", destination.trim());

        return q.getResultList();
    }

    @Override
    public boolean bookTicket(String flightNo, String customerName, int seats) {
        Flight flight = em.find(Flight.class, flightNo);
        if (flight == null)
            return false;

        if (flight.getAvailableSeats() < seats)
            return false;

        flight.setAvailableSeats(flight.getAvailableSeats() - seats);
        em.merge(flight);

        Booking booking = new Booking();

        booking.setFlightNo(flightNo);
        booking.setCustomerName(customerName);
        booking.setSeatsBooked(seats);
        booking.setTotalPrice(flight.getPrice().multiply(BigDecimal.valueOf(seats)));
        booking.setBookingDate(new Date());

        em.persist(booking);

        return true;
    }
}

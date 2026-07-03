package com.fpt.sb;

import com.fpt.entity.Flight;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface FlightFacadeLocal {

    List<Flight> searchFlights(String departure, String destination);

    boolean bookTicket(String flightNo, String customerName, int seats);
}

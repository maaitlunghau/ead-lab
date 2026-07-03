package com.fpt.mb;

import com.fpt.entity.Flight;
import com.fpt.sb.FlightFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("flightBean")
@SessionScoped
public class FlightBean implements Serializable {

    @EJB
    private FlightFacadeLocal flightFacade;

    private String customerName;
    private String departure;
    private String destination;
    private List<Flight> flights;
    private String selectedFlightNo;
    private int seatsToBook;

    @PostConstruct
    public void init() {
        seatsToBook = 1;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public String getSelectedFlightNo() {
        return selectedFlightNo;
    }

    public void setSelectedFlightNo(String selectedFlightNo) {
        this.selectedFlightNo = selectedFlightNo;
    }

    public int getSeatsToBook() {
        return seatsToBook;
    }

    public void setSeatsToBook(int seatsToBook) {
        this.seatsToBook = seatsToBook;
    }

    public String search() {
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .put("customer", customerName.trim());

        flights = flightFacade.searchFlights(departure, destination);

        return "booking?faces-redirect=true";
    }

    public String bookTicket() {
        if (seatsToBook <= 0) {
            showErrorMessage("Seats to book must be greater than 0.");
            return null;
        }

        boolean success = flightFacade.bookTicket(selectedFlightNo, customerName, seatsToBook);

        if (success) {
            flights = flightFacade.searchFlights(departure, destination);
            showInfoMessage("Booking successful!");

            return "booking?faces-redirect=true";
        } else {
            showErrorMessage("Booking failed. Flight not found or insufficient seats.");
            return null;
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "search?faces-redirect=true";
    }

    private void showInfoMessage(String message) {
        FacesContext ctx = FacesContext.getCurrentInstance();

        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void showErrorMessage(String message) {
        FacesContext ctx = FacesContext.getCurrentInstance();

        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}

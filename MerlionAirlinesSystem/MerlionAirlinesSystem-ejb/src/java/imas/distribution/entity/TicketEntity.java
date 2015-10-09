/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.distribution.entity;

import imas.planning.entity.FlightEntity;
import imas.planning.entity.SeatEntity;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Scarlett
 */
@Entity
public class TicketEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Double baggageWeight;
    private Boolean premiumMeal;
    private Boolean exclusiveService;
    private Boolean insurance;
    private Boolean flightWiFi;
    
    @ManyToOne
    private SeatEntity seat;

    private FlightEntity flight;

    private String seatClass; // can only be one of: First Class, Business Class, Premium Economy Class, Economy Class

    private String bookingClassName; // booking class Name. 
    // Can only be one of: 
    // First Class, Business Class, Premium Economy Class,
    // Full Service Economy, Economy Plus, Standard Economy, Economy Save, Economy Super Save
    // Economy Class Agency

    private double price;

    private boolean issued; // if the ticket is issued at the check in counter

    public TicketEntity() {
    }

    public TicketEntity(FlightEntity flight, String seatClass, String bookingClassName, double price) {
        this.flight = flight;
        this.seatClass = seatClass;
        this.bookingClassName = bookingClassName;
        this.price = price;
        this.issued = false;
    }

    public TicketEntity(FlightEntity flight, String seatClass, String bookingClassName, double price, SeatEntity seat) {
        this.flight = flight;
        this.seatClass = seatClass;
        this.bookingClassName = bookingClassName;
        this.price = price;
        this.issued = false;
        this.seat = seat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SeatEntity getSeat() {
        return seat;
    }

    public void setSeat(SeatEntity seat) {
        this.seat = seat;
    }

    public FlightEntity getFlight() {
        return flight;
    }

    public void setFlight(FlightEntity flight) {
        this.flight = flight;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public String getBookingClassName() {
        return bookingClassName;
    }

    public void setBookingClassName(String bookingClassName) {
        this.bookingClassName = bookingClassName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isIssued() {
        return issued;
    }

    public void setIssued(boolean issued) {
        this.issued = issued;
    }

    public Double getBaggageWeight() {
        return baggageWeight;
    }

    public void setBaggageWeight(Double baggageWeight) {
        this.baggageWeight = baggageWeight;
    }

    public Boolean getPremiumMeal() {
        return premiumMeal;
    }

    public void setPremiumMeal(Boolean premiumMeal) {
        this.premiumMeal = premiumMeal;
    }

    public Boolean getExclusiveService() {
        return exclusiveService;
    }

    public void setExclusiveService(Boolean exclusiveService) {
        this.exclusiveService = exclusiveService;
    }

    public Boolean getInsurance() {
        return insurance;
    }

    public void setInsurance(Boolean insurance) {
        this.insurance = insurance;
    }

    public Boolean getFlightWiFi() {
        return flightWiFi;
    }

    public void setFlightWiFi(Boolean flightWiFi) {
        this.flightWiFi = flightWiFi;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TicketEntity)) {
            return false;
        }
        TicketEntity other = (TicketEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "imas.inventory.entity.TicketEntity[ id=" + id + " ]";
    }

}

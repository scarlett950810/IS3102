/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.planning.entity;

import imas.common.entity.CabinCrewEntity;
import imas.common.entity.PilotEntity;
import imas.distribution.entity.TicketEntity;
import imas.inventory.entity.BookingClassEntity;
import imas.inventory.entity.BookingClassRuleSetEntity;
import imas.inventory.entity.YieldManagementRuleEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Lei
 */

@XmlRootElement
@XmlType
@Entity
public class FlightEntity implements Serializable, Comparable<FlightEntity> {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String flightNo;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date departureDate;
    private String departureDateConverted;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date arrivalDate;
    private String arrivalDateConverted;
    @OneToOne(cascade = CascadeType.PERSIST)
    private FlightEntity reverseFlight;
    private Integer operatingYear;
    private String weekDay;
    @OneToMany(mappedBy = "flight")
    private List<BookingClassEntity> bookingClasses;
    @OneToMany(mappedBy = "flight")
    private List<TicketEntity> tickets;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date   actualDepartureDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date   actualArrivalDate;
    private String emergencyOfAtcViolation;//9
    private String mechanicalFailures;//10
    private String fuelDumping;//11
    private String passengerSpecialStatus;//12
    private String crewSpecialStatus;//13
    private String passengerMisconduct;//14
    private String hazmatIssue;//15
    private String diversions;//16
    private String highSpeedAborts;//17
    private String lightningStrikers;//18
    private String nearAirCollisions;//19
    private String others;//20
    // web check in open and close is purely decide by how much time to departure. web check in opens 24 hours before and closes 1 hour before departure.
    // counter open is purely decided by how much time to departure. counter 
    private boolean counterCheckInClosed;
    private boolean departured;
    private Double costPerSeatPerMile;
    private Double revenue;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date estimateDepartureDate;//ture is delayed

    @ManyToOne
    private AircraftEntity aircraft;

    //route
    @ManyToOne
    private RouteEntity route;

    // private FlightRecordEntity flightRecord;
    @OneToMany(mappedBy = "flightRecords")
    private List<FlightRecordEntity> flightRecords;

    @ManyToMany(mappedBy = "cabinCrewFligths")
    private List<CabinCrewEntity> cabinCrews;

    @ManyToMany(mappedBy = "pilotFlights")
    private List<PilotEntity> pilots;
    @OneToMany(mappedBy = "flight")
    private List<BookingClassRuleSetEntity> bookingClassRuleSets;

    @OneToMany(mappedBy = "flight")
    private List<YieldManagementRuleEntity> yieldManagementRules;

    public FlightEntity() {
        this.bookingClassRuleSets = new ArrayList<>();
        this.yieldManagementRules = new ArrayList<>();
        this.counterCheckInClosed = false;
        this.departured = false;

    }
    //test
    public FlightEntity(String test) {
        this.flightNo = test;
    }

    public FlightEntity(Integer yearSelected) {
        this.bookingClassRuleSets = new ArrayList<>();
        this.yieldManagementRules = new ArrayList<>();
        this.counterCheckInClosed = false;
        this.departured = false;
        this.operatingYear = yearSelected;

    }

    public FlightEntity(String flightNo, AircraftEntity aircraft, RouteEntity route) {
        this.bookingClassRuleSets = new ArrayList<>();
        this.yieldManagementRules = new ArrayList<>();
        this.counterCheckInClosed = false;
        this.departured = false;

        this.flightNo = flightNo;
        this.aircraft = aircraft;
        this.route = route;

    }

    public Integer getOperatingYear() {
        return operatingYear;
    }

    public void setOperatingYear(Integer operatingYear) {
        this.operatingYear = operatingYear;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketEntity> tickets) {
        this.tickets = tickets;
    }

    public Long getId() {
        return id;
    }

    public Date getEstimateDepartureDate() {
        return estimateDepartureDate;
    }

    public void setEstimateDepartureDate(Date estimateDepartureDate) {
        this.estimateDepartureDate = estimateDepartureDate;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public FlightEntity getReverseFlight() {
        return reverseFlight;
    }

    public void setReverseFlight(FlightEntity reverseFlight) {
        this.reverseFlight = reverseFlight;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Date getActualDepartureDate() {
        return actualDepartureDate;
    }

    public void setActualDepartureDate(Date actualDepartureDate) {
        this.actualDepartureDate = actualDepartureDate;
    }

    public Date getActualArrivalDate() {
        return actualArrivalDate;
    }

    public void setActualArrivalDate(Date actualArrivalDate) {
        this.actualArrivalDate = actualArrivalDate;
    }

    public List<CabinCrewEntity> getCabinCrews() {
        return cabinCrews;
    }

    public void setCabinCrews(List<CabinCrewEntity> cabinCrews) {
        this.cabinCrews = cabinCrews;
    }

    public List<PilotEntity> getPilots() {
        return pilots;
    }

    public void setPilots(List<PilotEntity> pilots) {
        this.pilots = pilots;
    }

    public String getEmergencyOfAtcViolation() {
        return emergencyOfAtcViolation;
    }

    public void setEmergencyOfAtcViolation(String emergencyOfAtcViolation) {
        this.emergencyOfAtcViolation = emergencyOfAtcViolation;
    }

    public String getMechanicalFailures() {
        return mechanicalFailures;
    }

    public void setMechanicalFailures(String mechanicalFailures) {
        this.mechanicalFailures = mechanicalFailures;
    }

    public String getFuelDumping() {
        return fuelDumping;
    }

    public void setFuelDumping(String fuelDumping) {
        this.fuelDumping = fuelDumping;
    }

    public String getPassengerSpecialStatus() {
        return passengerSpecialStatus;
    }

    public void setPassengerSpecialStatus(String passengerSpecialStatus) {
        this.passengerSpecialStatus = passengerSpecialStatus;
    }

    public String getCrewSpecialStatus() {
        return crewSpecialStatus;
    }

    public void setCrewSpecialStatus(String crewSpecialStatus) {
        this.crewSpecialStatus = crewSpecialStatus;
    }

    public String getPassengerMisconduct() {
        return passengerMisconduct;
    }

    public void setPassengerMisconduct(String passengerMisconduct) {
        this.passengerMisconduct = passengerMisconduct;
    }

    public String getHazmatIssue() {
        return hazmatIssue;
    }

    public void setHazmatIssue(String hazmatIssue) {
        this.hazmatIssue = hazmatIssue;
    }

    public String getDiversions() {
        return diversions;
    }

    public void setDiversions(String diversions) {
        this.diversions = diversions;
    }

    public String getHighSpeedAborts() {
        return highSpeedAborts;
    }

    public void setHighSpeedAborts(String highSpeedAborts) {
        this.highSpeedAborts = highSpeedAborts;
    }

    public String getLightningStrikers() {
        return lightningStrikers;
    }

    public void setLightningStrikers(String lightningStrikers) {
        this.lightningStrikers = lightningStrikers;
    }

    public String getNearAirCollisions() {
        return nearAirCollisions;
    }

    public void setNearAirCollisions(String nearAirCollisions) {
        this.nearAirCollisions = nearAirCollisions;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public RouteEntity getRoute() {
        return route;
    }

    public void setRoute(RouteEntity route) {
        this.route = route;
    }

    public List<FlightRecordEntity> getFlightRecords() {
        return flightRecords;
    }

    public void setFlightRecords(List<FlightRecordEntity> flightRecords) {
        this.flightRecords = flightRecords;
    }

    public AircraftEntity getAircraft() {
        return aircraft;
    }

    public void setAircraft(AircraftEntity aircraft) {
        this.aircraft = aircraft;
    }

    public boolean isDepartured() {
        return departured;
    }

    public void setDepartured(boolean departured) {
        this.departured = departured;
    }

    public boolean isCounterCheckInClosed() {
        return counterCheckInClosed;
    }

    public void setCounterCheckInClosed(boolean counterCheckInClosed) {
        this.counterCheckInClosed = counterCheckInClosed;
    }

    public List<BookingClassEntity> getBookingClasses() {
        return bookingClasses;
    }

    public void setBookingClasses(List<BookingClassEntity> bookingClasses) {
        this.bookingClasses = bookingClasses;
    }

    public List<BookingClassRuleSetEntity> getBookingClassRuleSetEntities() {
        return bookingClassRuleSets;
    }

    public void setBookingClassRuleSetEntities(List<BookingClassRuleSetEntity> bookingClassRuleSetEntities) {
        this.bookingClassRuleSets = bookingClassRuleSetEntities;
    }

    public List<BookingClassRuleSetEntity> getBookingClassRuleSets() {
        return bookingClassRuleSets;
    }

    public void setBookingClassRuleSets(List<BookingClassRuleSetEntity> bookingClassRuleSets) {
        this.bookingClassRuleSets = bookingClassRuleSets;
    }

    public List<YieldManagementRuleEntity> getYieldManagementRules() {
        return yieldManagementRules;
    }

    public void setYieldManagementRules(List<YieldManagementRuleEntity> yieldManagementRules) {
        this.yieldManagementRules = yieldManagementRules;
    }

    public Double getCostPerSeatPerMile() {
        return costPerSeatPerMile;
    }

    public void setCostPerSeatPerMile(Double costPerSeatPerMile) {
        this.costPerSeatPerMile = costPerSeatPerMile;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        if (isDepartured()) {
            this.revenue = revenue;
        }
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
        if (!(object instanceof FlightEntity)) {
            return false;
        }
        FlightEntity other = (FlightEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        } else if (this.id == null && other.id == null) {
            return this.arrivalDate.equals(other.arrivalDate) && this.departureDate.equals(other.departureDate) && this.flightNo.equals(other.flightNo) && this.route.equals(other.route); // return true or false
        }

        return true;
    }

    @Override
    public String toString() {
        if (route == null || departureDate == null || aircraft == null) {
            return "Flight id " + id;
        } else {
            return route.toString() + " at " + departureDate + " by " + aircraft.toString();
        }

    }

    @Override
    public int compareTo(FlightEntity o) {
        return this.departureDate.compareTo(o.departureDate);
   //     return o.departureDate.compareTo(this.departureDate);

    }

    private String convertTimezone(Date date, String countryName) {
        
        DateTime original = new DateTime(date.getTime());
        DateTimeZone dtz = DateTimeZone.getDefault();
        original.withZone(dtz);

        Set<String> tzIds = DateTimeZone.getAvailableIDs();
        for (String timeZoneId : tzIds) {
            if (timeZoneId.contains(countryName)) {
                dtz = DateTimeZone.forID(timeZoneId);
                break;
            }
        }
        DateTime dt = original.toDateTime(dtz);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MMM dd yyyy HH:mm:ss zzz");
        return dtfOut.print(dt);

    }

    public String getDepartureDateConverted() {
        if (departureDate == null) {
            return "";
        } else {
            return this.convertTimezone(this.departureDate, this.route.getOriginAirport().getNationName());
        }
    }

    public void setDepartureDateConverted(String departureDateConverted) {
        this.departureDateConverted = departureDateConverted;
    }

    public String getArrivalDateConverted() {
        if (departureDate == null) {
            return "";
        } else {
            return this.convertTimezone(this.arrivalDate, this.route.getDestinationAirport().getNationName());
        }
    }

    public void setArrivalDateConverted(String arrivalDateConverted) {
        this.arrivalDateConverted = arrivalDateConverted;
    }

}

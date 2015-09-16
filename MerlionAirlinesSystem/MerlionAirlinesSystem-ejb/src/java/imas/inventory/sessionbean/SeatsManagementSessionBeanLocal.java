/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.inventory.sessionbean;

import imas.planning.entity.FlightEntity;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Scarlett
 */
@Local
public interface SeatsManagementSessionBeanLocal {

    public List<FlightEntity> getFlightsWithoutBookingClass();

    public double computeHistoricalShowRate();

    public void insertData();

    public int getFirstClassCapacity(FlightEntity flight);

    public int getBusinessClassCapacity(FlightEntity flight);
                  
    public int getPremiumEconomyClassCapacity(FlightEntity flight);
    
    public int getEconomyClassCapacity(FlightEntity flight);

    public void generateFirstClassBookingClassEntity(FlightEntity flight, double price, int quota);

    public void generateBusinessClassBookingClassEntity(FlightEntity flight, double price, int quota);

    public void generatePremiumEconomyClassBookingClassEntity(FlightEntity flight, double price, int quota);

    public void generateEconomyClassXBookingClassEntity(FlightEntity flight, double price, int quota);

    public void generateEconomyClassYBookingClassEntity(FlightEntity flight, double price, int quota);

    public void generateEconomyClassZBookingClassEntity(FlightEntity flight, double price, int quota);

    public void generateEconomyClassTBookingClassEntity(FlightEntity flight, double price, int quota);

    public void getBaseFare();

   
}

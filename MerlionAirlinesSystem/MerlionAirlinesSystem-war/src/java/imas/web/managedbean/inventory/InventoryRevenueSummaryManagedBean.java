/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.web.managedbean.inventory;

import imas.distribution.sessionbean.FlightLookupSessionBeanLocal;
import imas.inventory.sessionbean.CostManagementSessionBean;
import imas.inventory.sessionbean.CostManagementSessionBeanLocal;
import imas.inventory.sessionbean.InventoryRevenueManagementSessionBeanLocal;
import imas.planning.entity.RouteEntity;
import imas.planning.sessionbean.RouteSessionBeanLocal;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.persistence.PostRemove;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.LinearAxis;

/**
 *
 * @author Scarlett
 */
@Named(value = "inventoryRevenueSummaryManagedBean")
@ViewScoped
public class InventoryRevenueSummaryManagedBean implements Serializable {

    @EJB
    private InventoryRevenueManagementSessionBeanLocal inventoryRevenueManagementSessionBean;

    @EJB
    private RouteSessionBeanLocal routeSession;

    private int topXOnly;
    private List<RouteEntity> routeList;
    private RouteEntity selectedRoute;
    private Date routeFromDate;
    private Date routeFromMaxDate;
    private Date routeToDate;
    private Date routeToMinDate;
    private Date today;
    private CartesianChartModel routeDetailsByTimelineCombinedModel;
    private CartesianChartModel routeDetailsByBookingClassCombinedModel;
    private List<String[]> tableData;

    public InventoryRevenueSummaryManagedBean() {
    }

    @PostConstruct
    public void init() {

        routeToMinDate = this.getToday();
        routeToDate = this.getToday();
        routeFromDate = addDays(routeToDate, -365);
        routeFromMaxDate = routeToDate;
        topXOnly = 10;
        this.routeList = routeSession.retrieveAllRoutes();
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("routeList", this.routeList);
        routeDetailsByTimelineCombinedModel = new BarChartModel();
        routeDetailsByBookingClassCombinedModel = new BarChartModel();

        if (!routeList.isEmpty()) {
            selectedRoute = routeList.get(0);
            updateGraphAndTable();
        }

    }

    @PostRemove
    public void destroy() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("routeList");
    }

    public int getTopXOnly() {
        return topXOnly;
    }

    public void setTopXOnly(int topXOnly) {
        this.topXOnly = topXOnly;
    }

    public RouteEntity getSelectedRoute() {
        return selectedRoute;
    }

    public void setSelectedRoute(RouteEntity selectedRoute) {
        this.selectedRoute = selectedRoute;
    }

    public List<RouteEntity> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<RouteEntity> routeList) {
        this.routeList = routeList;
    }

    public Date getRouteFromDate() {
        return routeFromDate;
    }

    public void setRouteFromDate(Date routeFromDate) {
        this.routeFromDate = routeFromDate;
    }

    public Date getRouteToDate() {
        return routeToDate;
    }

    public void setRouteToDate(Date routeToDate) {
        this.routeToDate = routeToDate;
    }

    public Date getRouteFromMaxDate() {
        return routeFromMaxDate;
    }

    public void setRouteFromMaxDate(Date routeFromMaxDate) {
        this.routeFromMaxDate = routeFromMaxDate;
    }

    public Date getRouteToMinDate() {
        return routeToMinDate;
    }

    public void setRouteToMinDate(Date routeToMinDate) {
        this.routeToMinDate = routeToMinDate;
    }

    public void onRouteFromDateSelect(SelectEvent event) {
        routeToMinDate = routeFromDate;
        if (selectedRoute != null) {
            System.out.println("from date changed");
            updateGraphAndTable();
        }
    }

    public void onRouteToDateSelect(SelectEvent event) {
        routeFromMaxDate = routeToDate;
        if (selectedRoute != null) {
            System.out.println("to date changed");
            updateGraphAndTable();
        }
    }

    public Date getToday() {
        return new Date();
    }

    public void setToday(Date today) {
        this.today = today;
    }

    public CartesianChartModel getRouteDetailsByTimelineCombinedModel() {
        return routeDetailsByTimelineCombinedModel;
    }

    public void setRouteDetailsByTimelineCombinedModel(CartesianChartModel routeDetailsByTimelineCombinedModel) {
        this.routeDetailsByTimelineCombinedModel = routeDetailsByTimelineCombinedModel;
    }

    public CartesianChartModel getRouteDetailsByBookingClassCombinedModel() {
        return routeDetailsByBookingClassCombinedModel;
    }

    public void setRouteDetailsByBookingClassCombinedModel(CartesianChartModel routeDetailsByBookingClassCombinedModel) {
        this.routeDetailsByBookingClassCombinedModel = routeDetailsByBookingClassCombinedModel;
    }

    public List<String[]> getTableData() {
        return tableData;
    }

    public void setTableData(List<String[]> tableData) {
        this.tableData = tableData;
    }

    private Date addDays(Date original, int dayNo) {
        Calendar c = Calendar.getInstance();
        c.setTime(original);
        c.add(Calendar.DATE, dayNo);

        return c.getTime();
    }

    private Date addMonths(Date original, int monthNo) {
        Calendar c = Calendar.getInstance();
        c.setTime(original);
        c.add(Calendar.MONTH, monthNo);

        return c.getTime();
    }

    public void onRouteChange() {
        if (selectedRoute != null) {
            updateGraphAndTable();
        }
    }

    public void updateGraphAndTable() {
        
        tableData = new ArrayList<>();
        
        routeDetailsByTimelineCombinedModel = new BarChartModel();
        routeDetailsByTimelineCombinedModel.setTitle("Revenue, Cost and Profit Margin by Month for " + selectedRoute.getRouteName());
        routeDetailsByTimelineCombinedModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
        routeDetailsByTimelineCombinedModel.setLegendPosition("e");
        routeDetailsByTimelineCombinedModel.setShowPointLabels(false);
        routeDetailsByTimelineCombinedModel.setMouseoverHighlight(false);
        routeDetailsByTimelineCombinedModel.setAnimate(true);

        BarChartSeries revenueSeries = new BarChartSeries();
        revenueSeries.setLabel("Revenue");
        BarChartSeries costSeries = new BarChartSeries();
        costSeries.setLabel("Cost");
        LineChartSeries profitMarginSeries = new LineChartSeries();
        profitMarginSeries.setLabel("Profit Margin");

        Date start = routeFromDate;
        Date end;
        System.out.println("selected route = " + selectedRoute);
        while (start.before(routeToDate)) {
            end = addMonths(start, 1);
            if (end.after(routeToDate)) {
                end = routeToDate;
            }

            double revenue = inventoryRevenueManagementSessionBean.getRouteTotalRevenueDuringDuration(selectedRoute, start, end);
            double cost = inventoryRevenueManagementSessionBean.getRouteTotalCostDuringDuration(selectedRoute, start, end);

            double pm = 0.0;
            if (cost > 0) {
                pm = (revenue - cost) / revenue;
            }
             
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM YYYY");
            String label = simpleDateFormat.format(start);

            String[] rowData = new String[4];
            rowData[0] = label;
            rowData[1] = Double.toString(CostManagementSessionBean.round(cost, 2));
            rowData[2] = Double.toString(CostManagementSessionBean.round(revenue, 2));
            rowData[3] = Double.toString(CostManagementSessionBean.round(pm * 100,2)) + "%";
            tableData.add(rowData);
            
            revenueSeries.set(label, revenue);
            costSeries.set(label, cost);
            profitMarginSeries.set(label, pm * 100);

            start = end;
        }

        routeDetailsByTimelineCombinedModel.addSeries(revenueSeries);
        revenueSeries.setYaxis(AxisType.Y);
        routeDetailsByTimelineCombinedModel.addSeries(costSeries);
        costSeries.setYaxis(AxisType.Y3);
        routeDetailsByTimelineCombinedModel.addSeries(profitMarginSeries);
        profitMarginSeries.setYaxis(AxisType.Y2);

        Axis yAxis = routeDetailsByTimelineCombinedModel.getAxis(AxisType.Y);
        yAxis.setLabel("SG $");
        yAxis.setMin(0);
        Axis y2Axis = new LinearAxis("%");
        y2Axis.setMin(-0.5);
        routeDetailsByTimelineCombinedModel.getAxes().put(AxisType.Y2, y2Axis);
        routeDetailsByTimelineCombinedModel.getAxes().put(AxisType.Y3, y2Axis);

    }

}

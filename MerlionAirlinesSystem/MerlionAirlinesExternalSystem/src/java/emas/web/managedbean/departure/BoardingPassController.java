/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emas.web.managedbean.departure;

import imas.distribution.entity.TicketEntity;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;

/**
 *
 * @author Howard
 */
@WebServlet(name = "BoardingPassController", urlPatterns = {"/BoardingPassController", "/BoardingPassController?*"})
public class BoardingPassController extends HttpServlet {

    @Resource(name = "merlionAirlineDataSource")
    private DataSource merlionAirlineDataSource;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        generateBoardingPass(request, response);

    }

    private void generateBoardingPass(HttpServletRequest request, HttpServletResponse response) {
        try {
            TicketEntity ticket = (TicketEntity) request.getSession(true).getAttribute("ticket"); //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ticket");
//            String referenceNumber = request.getParameter("referenceNumber");
            InputStream reportStream = getServletConfig().getServletContext().getResourceAsStream("/jasperReports/BoardingPass.jasper");

            response.setContentType("application/pdf");
            ServletOutputStream outputStream = response.getOutputStream();
            HashMap parameters = new HashMap();

            Calendar cal = Calendar.getInstance(); 
            cal.setTime(ticket.getFlight().getDepartureDate()); 
            cal.add(Calendar.MINUTE, -30);
            Date boardingTime = cal.getTime();

            parameters.put("IMAGEPATH", "https://localhost:8181/MerlionAirlinesExternalSystem/resources/img/NEW_LOGO.png");
            parameters.put("referenceNumber", ticket.getReferenceNumber());
            parameters.put("name", ticket.getPassenger().getTitle() + " " + ticket.getPassenger().getFirstName() + " " + ticket.getPassenger().getLastName());
            parameters.put("departDate", new SimpleDateFormat("yyyy-MM-dd").format(ticket.getFlight().getDepartureDate()));
            parameters.put("seatNo", ticket.getSeat().getSeatNo());
            parameters.put("boardingTime", new SimpleDateFormat("hh:mm").format(boardingTime));
            parameters.put("destinationAirport", ticket.getFlight().getRoute().getDestinationAirport().getCityName()+","+ticket.getFlight().getRoute().getDestinationAirport().getAirportName()+"("+ ticket.getFlight().getRoute().getDestinationAirport().getAirportCode()+")");
            parameters.put("originAirport", ticket.getFlight().getRoute().getOriginAirport().getCityName()+","+ticket.getFlight().getRoute().getOriginAirport().getAirportName()+"("+ ticket.getFlight().getRoute().getOriginAirport().getAirportCode()+")");
            parameters.put("flightNo", ticket.getFlight().getFlightNo());
            
//            JasperRunManager.runReportToPdf(reportStream, parameters, merlionAirlineDataSource.getConnection());
            JasperRunManager.runReportToPdfStream(reportStream, outputStream,
                    parameters, merlionAirlineDataSource.getConnection());
            outputStream.flush();

        } catch (JRException jrex) {
            System.out.println("********** Jasperreport Exception");
            jrex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

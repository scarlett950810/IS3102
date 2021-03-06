/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.web.managedbean.planning;

import imas.planning.entity.AircraftEntity;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Scarlett
 */
@FacesConverter(value = "aircraftConverter")
public class AircraftConverter  implements Converter {
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {
            try {
                
                List<AircraftEntity> aircraftEntities = (List<AircraftEntity>)fc.getExternalContext().getSessionMap().get("aircraftList");
                
                Long aircraftEntityId = Long.valueOf(Long.parseLong(value));
                
                System.err.println("PRINT"+aircraftEntities);
                for(AircraftEntity aircraftEntity:aircraftEntities)
                {
                    if(aircraftEntity.getId().equals(aircraftEntityId))
                    {
                        return aircraftEntity;
                    }
                }
                
                return null;
            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid aircraft."));
            }
        } else {
            return null;
        }
    }
    
    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {    
            return String.valueOf(((AircraftEntity) object).getId());
        } else {
            return null;
        }
    }
    
}

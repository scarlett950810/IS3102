/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.crm.sessionbean;

import imas.crm.entity.MemberEntity;
import javax.ejb.Stateless;

/**
 *
 * @author wutong
 */
@Stateless
public class CustomerAccountManagementSessionBean implements CustomerAccountManagementSessionBeanLocal {

    @Override
    public void createCustomer(MemberEntity newCustomer) {
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fpt.sb;

import com.fptentity.Employee;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 *
 * @author hp
 */
@Stateless
public class EmployeeFacade extends AbstractFacade<Employee> implements EmployeeFacadeLocal {

    @PersistenceContext(unitName = "EmployeePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmployeeFacade() {
        super(Employee.class);
    }

    @Override
    public boolean checkLogin(String username, String password) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.empCode = :empCode and e.password = :password");
        query.setParameter("empCode", username.trim());
        query.setParameter("password", password.trim());
        
        if(query.getResultList().size() > 0){
           return true;
        }
        return false;
    }

    @Override
    public boolean exists(String empCode) {
        if (empCode == null || empCode.trim().isEmpty()) {
            return false;
        }

        return em.find(Employee.class, empCode.trim()) != null;
    }
    
}

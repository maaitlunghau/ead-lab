/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.fpt.sb;

import com.fptentity.Employee;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author hp
 */
@Local
public interface EmployeeFacadeLocal {

    void create(Employee employee);

    void edit(Employee employee);

    void remove(Employee employee);

    Employee find(Object id);

    List<Employee> findAll();

    List<Employee> findRange(int[] range);

    int count();
    
    //bo sung
    boolean checkLogin(String username, String password);

    boolean exists(String empCode);
}

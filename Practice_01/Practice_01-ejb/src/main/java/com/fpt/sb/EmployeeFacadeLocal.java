package com.fpt.sb;

import com.fptentity.Employee;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface EmployeeFacadeLocal {

    void create(Employee employee);
    void edit(Employee employee);
    void remove(Employee employee);
    Employee find(Object id);
    List<Employee> findAll();
    List<Employee> findRange(int[] range);
    int count();

    boolean checkLogin(String username, String password);
    boolean exists(String empCode);
}

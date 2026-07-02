package com.fpt.mb;

import com.fpt.sb.EmployeeFacadeLocal;
import com.fptentity.Employee;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("employeeManagedBean")
@SessionScoped
public class EmployeeManagedBean implements Serializable, Validator<Object> {

    // 1. Properties

    @EJB
    private EmployeeFacadeLocal employeeFacade;

    private Employee employee;
    private String username;
    private String password;

    // 2. Init

    @PostConstruct
    public void init() {
        employee = new Employee();
    }

    // 3. Getters / Setters

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 4. Action Methods

    public String login() {
        if (employeeFacade.checkLogin(username, password)) {
            // Lưu username vào session để AuthFilter kiểm tra
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("loggedInUser", username);
            showInfoMessage("Login successfully.");
            return "view?faces-redirect=true";
        }
        showErrorMessage("Invalid username or password.");
        return null;
    }

    public List<Employee> view() {
        return employeeFacade.findAll();
    }

    public String insert() {
        try {
            Employee newEmployee = buildEmployeeForInsert();

            if (employeeFacade.exists(newEmployee.getEmpCode())) {
                showErrorMessage("Employee code already exists.");
                return null;
            }

            employeeFacade.create(newEmployee);
            employee = new Employee();
            showInfoMessage("Insert employee successfully.");
            return "view?faces-redirect=true";
        } catch (Exception ex) {
            showErrorMessage("Cannot insert employee. Please try again.");
            return null;
        }
    }

    // 5. Custom Validator — so sánh confirmPassword với password

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        String confirmedPassword = value == null ? "" : value.toString();
        Object targetId = component.getAttributes().get("checkPass");
        UIInput passwordComponent = (UIInput) component.findComponent(targetId.toString());
        String password = passwordComponent.getValue() == null
                          ? "" : passwordComponent.getValue().toString();

        if (!confirmedPassword.equals(password)) {
            throw new ValidatorException(
                new FacesMessage("Confirmed password does not match.")
            );
        }
    }

    // 6. Utilities

    private Employee buildEmployeeForInsert() {
        Employee e = new Employee();
        e.setEmpCode(employee.getEmpCode().trim());
        e.setPassword(employee.getPassword().trim());
        e.setName(employee.getName().trim());
        e.setAge(employee.getAge());
        return e;
    }

    private void showInfoMessage(String message) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void showErrorMessage(String message) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}

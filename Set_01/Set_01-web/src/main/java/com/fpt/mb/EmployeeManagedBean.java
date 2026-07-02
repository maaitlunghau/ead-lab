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
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;

@Named(value = "employeeManagedBean")
@SessionScoped
public class EmployeeManagedBean implements Serializable, Validator<Object> {

    // 4.1. Properties
    // 4.1.1. Call Enterprise Bean
    @EJB
    private EmployeeFacadeLocal employeeFacade;

    // 4.1.2. Upload directory
    private final String UPLOAD_DIRECTORY = "D:/uploads/";

    // 4.1.3. Properties
    private Employee employee;
    private String username;
    private String password;
    private transient Part part;

    // 4.1.4. init() method
    @PostConstruct
    public void init() {
        employee = new Employee();
    }

    // 4.2. Getters and Setters
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    // 4.3. CRUD
    // view() method
    public List<Employee> view() {
        return employeeFacade.findAll();
    }

    // insert() method
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
            showErrorMessage("Cannot insert employee. Please check the information and try again.");
            return null;
        }
    }

    // 4.4. Utilities
    // showInfoMessage() method
    private void showInfoMessage(String message) {
        showMessage(FacesMessage.SEVERITY_INFO, message);
    }

    // showErrorMessage() method
    private void showErrorMessage(String message) {
        showMessage(FacesMessage.SEVERITY_ERROR, message);
    }

    // showMessage() method
    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(
                null,
                new FacesMessage(severity, message, null)
        );
    }

    // buildEmployeeForInsert() method
    private Employee buildEmployeeForInsert() {
        Employee newEmployee = new Employee();
        newEmployee.setEmpCode(employee.getEmpCode().trim());
        newEmployee.setPassword(employee.getPassword().trim());
        newEmployee.setName(employee.getName().trim());
        newEmployee.setAge(employee.getAge());
        return newEmployee;
    }

    // getFileName() method
    private String getFileName(Part part) {
        String header = part.getHeader("content-disposition");

        for (String token : header.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 1)
                        .trim()
                        .replace("\"", "");
            }
        }

        return "";
    }

    // getFileExtension() method
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");

        if (lastDot >= 0) {
            return fileName.substring(lastDot);
        }

        return "";
    }

    // uploadFile() method
    private String uploadFile() throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String originalName = getFileName(part);
        String extension = getFileExtension(originalName);
        String fileName = System.currentTimeMillis() + extension;

        File folder = new File(UPLOAD_DIRECTORY);
        folder.mkdirs();

        File file = new File(folder, fileName);

        try (InputStream input = part.getInputStream()) {
            Files.copy(input, file.toPath());
        }

        return fileName;
    }

    // login() method
    public String login() {
        if (employeeFacade.checkLogin(username, password)) {
            showInfoMessage("Login successfully.");
            return "view?faces-redirect=true";
        }

        showErrorMessage("Invalid username or password.");
        return null;
    }

    // validate() method implements Validator
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String confirmedPassword = value == null ? "" : value.toString();
        Object targetId = component.getAttributes().get("checkPass");
        UIInput passwordComponent = targetId == null
                ? null
                : (UIInput) component.findComponent(targetId.toString());
        Object passwordValue = passwordComponent == null ? null : passwordComponent.getValue();
        String password = passwordValue == null ? "" : passwordValue.toString();

        if (!confirmedPassword.equals(password)) {
            throw new ValidatorException(
                    new FacesMessage("Confirmed password does not match.")
            );
        }
    }
}

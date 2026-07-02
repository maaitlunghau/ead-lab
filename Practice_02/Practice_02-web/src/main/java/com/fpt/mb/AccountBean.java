package com.fpt.mb;

import com.fpt.entity.Accounts;
import com.fpt.sb.AccountsFacadeLocal;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Named("accountBean")
@SessionScoped
public class AccountBean implements Serializable, Validator<Object> {

    // 1. Properties

    @EJB
    private AccountsFacadeLocal accountsFacade;

    private final String IMAGE_FOLDER = "images/accs";

    private List<Accounts> accounts;
    private Accounts account;
    private Part part;
    private String rePass;

    // 2. Init

    @PostConstruct
    public void init() {
        account = new Accounts();
        account.setRole("user");
        select();
    }

    // 3. Getters / Setters

    public List<Accounts> getAccounts() { return accounts; }
    public void setAccounts(List<Accounts> accounts) { this.accounts = accounts; }

    public Accounts getAccount() { return account; }
    public void setAccount(Accounts account) { this.account = account; }

    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }

    public String getRePass() { return rePass; }
    public void setRePass(String rePass) { this.rePass = rePass; }

    // 4. Action Methods

    public void select() {
        accounts = accountsFacade.findAll();
    }

    public String login() {
        Accounts result = accountsFacade.checkLogin(account.getUsername(), account.getPassword());
        if (result != null && "admin".equalsIgnoreCase(result.getRole())) {
            account = result;
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("loggedInUser", result.getUsername());
            showMessage("Login successfully!");
            return "account-list?faces-redirect=true";
        }
        showMessage("Invalid username or password!");
        return null;
    }

    public String create() {
        account = new Accounts();
        account.setRole("user");
        rePass = null;
        part = null;
        return "account-create?faces-redirect=true";
    }

    public String insert() {
        try {
            if (part != null && part.getSize() > 0) {
                String fileName = upload(part);
                account.setImage(fileName);
            }

            if (account.getRole() == null || account.getRole().trim().isEmpty()) {
                account.setRole("user");
            }

            accountsFacade.create(account);
            select();
            account = new Accounts();
            account.setRole("user");
            rePass = null;
            part = null;
            showMessage("Insert successfully!");
            return "account-list?faces-redirect=true";
        } catch (Exception e) {
            showMessage("Insert failed: " + e.getMessage());
            return null;
        }
    }

    // 5. Custom Validator — confirm password

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        String confirmedPassword = value == null ? "" : value.toString();
        Object targetId = component.getAttributes().get("checkPass");
        UIInput passwordComponent = targetId == null
                ? null : (UIInput) component.findComponent(targetId.toString());
        Object passwordValue = passwordComponent == null ? null : passwordComponent.getValue();
        String password = passwordValue == null ? "" : passwordValue.toString();

        if (!confirmedPassword.equals(password)) {
            throw new ValidatorException(
                new FacesMessage("Confirmed password does not match."));
        }
    }

    // 6. Utilities

    private void showMessage(String message) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(message));
    }

    private String getFileName(Part part) {
        if (part == null || part.getHeader("content-disposition") == null) return "unknown";
        String header = part.getHeader("content-disposition");
        for (String token : header.split(";")) {
            if (token.trim().startsWith("filename")) {
                String fileName = token.substring(token.indexOf("=") + 1)
                        .trim().replace("\"", "");
                return Paths.get(fileName).getFileName().toString();
            }
        }
        return "unknown";
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return "";
    }

    private String upload(Part part) throws IOException {
        String realPath = FacesContext.getCurrentInstance()
                .getExternalContext().getRealPath("/");
        Path uploadPath = Paths.get(realPath, IMAGE_FOLDER);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = getFileName(part);
        String extension = getFileExtension(originalName);
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(fileName);

        try (InputStream input = part.getInputStream()) {
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }
}

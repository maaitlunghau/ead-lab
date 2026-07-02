
package com.fpt.mb;

import com.fpt.entity.Accounts;
import com.fpt.sb.AccountsFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
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

/**
 *
 * @author hp
 */
@Named(value = "accountBean")
@SessionScoped
public class AccountBean implements Serializable, Validator<Object> {

    //1.properties
    
    //1.1 Call enterprise bean
    @EJB
    private AccountsFacadeLocal accountsFacade;
    
    //1.2 upload image folder
    private final String IMAGE_FOLDER = "images/accs";
    
    //1.3 data source Entity instance
    private List<Accounts> accounts;
    private Accounts acount;
    
    private Part part;
    private String rePass;
    
    //1.4 init method
    @PostConstruct
    public void init(){
        acount = new Accounts();
        acount.setRole("user");
        select();
    }
    
    //2. getters/setters
    public List<Accounts> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Accounts> accounts) {
        this.accounts = accounts;
    }

    public Accounts getAcount() {
        return acount;
    }

    public void setAcount(Accounts acount) {
        this.acount = acount;
    }

    public Accounts getAccount() {
        return acount;
    }

    public void setAccount(Accounts account) {
        this.acount = account;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public String getRePass() {
        return rePass;
    }

    public void setRePass(String rePass) {
        this.rePass = rePass;
    }
    
    //3.CRUD
    //3.1 select data
    public void select() {
        accounts = accountsFacade.findAll();
    }
    
    //3.2 insert()
    public String create() {
        acount = new Accounts();
        acount.setRole("user");
        rePass = null;
        part = null;
        return "account-create?faces-redirect=true";
    }

    public String insert() {
        try {
            // image processing
            if (part != null && part.getSize() > 0) {
                String fileName = upload(part);
                acount.setImage(fileName);
            }

            if (acount.getRole() == null || acount.getRole().trim().isEmpty()) {
                acount.setRole("user");
            }

            accountsFacade.create(acount);
            showMessage("Insert successfully!");
            select();
            acount = new Accounts();
            acount.setRole("user");
            rePass = null;
            part = null;
            return "account-list?faces-redirect=true";
        } catch (Exception e) {
            showMessage("Insert fail: " + e.getMessage());
            return null;
        }
    }
    
    //4. Utilities
    //4.1 showMessage()
    private void showMessage(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, new FacesMessage(message));
    }
    
    //4.2 select()
    public String login() {
        Accounts result = accountsFacade.checkLogin(acount.getUsername(), acount.getPassword());
        if (result != null && "admin".equalsIgnoreCase(result.getRole())) {
            acount = result;
            showMessage("Login successfully!");
            return "account-list?faces-redirect=true";
        }

        showMessage("Invalid username or password!");
        return null;
    }
    
    //File
     //4.1 getFileName() method
    private String getFileName(Part part){
      if (part == null || part.getHeader("content-disposition") == null) {
          return "unknown";
      }
      String header = part.getHeader("content-disposition");
      for(String token : header.split(";")){
        if(token.trim().startsWith("filename")){
          String fileName = token.substring(token.indexOf("=")+ 1)
                  .trim()
                  .replace("\"", "");
          return Paths.get(fileName).getFileName().toString();
        }
      }
      return "unknown";
    }
    //4.2 getFileExtension()
    private String getFileExtension(String fileName){
      int lastDot = fileName.lastIndexOf(".");
      
      if(lastDot > 0 && lastDot < fileName.length() - 1){
        return fileName.substring(lastDot);
      }
      return "";
    }
    //4.3 upload() method
    private String upload(Part part) throws IOException{
      String realPath = FacesContext.getCurrentInstance()
              .getExternalContext()
              .getRealPath("/");
      Path uploadPath = Paths.get(realPath, IMAGE_FOLDER);
      if(!Files.exists(uploadPath)){
          Files.createDirectories(uploadPath);
      }
      
      String originalFileName = getFileName(part);
      String extension = getFileExtension(originalFileName);
      String fileName = UUID.randomUUID().toString() + extension;
      Path filePath = uploadPath.resolve(fileName);
      
      try(InputStream input = part.getInputStream()){
          Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
      }
      return fileName;
    }

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

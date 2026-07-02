# Set_01 — Phân tích & Tài liệu kỹ thuật

> Tài liệu này được tạo sau khi phân tích toàn bộ source code và deploy thành công Set_01.
> Đọc file này thay vì phân tích lại từ đầu trong các session tiếp theo.

---

## 1. Tổng quan dự án

| Thuộc tính | Giá trị |
|---|---|
| Tên dự án | Set_01 (EAD Practice) |
| Kiến trúc | Jakarta EE 10 — EAR (3-tier) |
| Application Server | Payara 7 |
| Database | Microsoft SQL Server 2022 (Docker) |
| Ngôn ngữ | Java 25 |
| Build tool | Maven |
| UI Framework | JSF (Facelets) + Bootstrap 5.3 |

**Chức năng chính:**
- Đăng nhập bằng EmpCode + Password
- Xem danh sách nhân viên
- Thêm nhân viên mới (có validate + xác nhận mật khẩu)

---

## 2. Cấu trúc thư mục

```
Set_01/
├── pom.xml                          ← Parent POM (groupId: com.fpt, version: 1.0)
├── scriptDB.sql                     ← Script tạo DB SQL Server
│
├── Set_01-ejb/                      ← Module EJB (business logic)
│   ├── pom.xml
│   └── src/main/java/
│       ├── com/fptentity/
│       │   └── Employee.java        ← JPA Entity
│       └── com/fpt/sb/
│           ├── AbstractFacade.java  ← Generic CRUD base class
│           ├── EmployeeFacadeLocal.java  ← EJB Local Interface
│           └── EmployeeFacade.java  ← @Stateless Session Bean
│
├── Set_01-web/                      ← Module Web (presentation)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fpt/mb/
│       │   └── EmployeeManagedBean.java  ← JSF Controller (@Named @SessionScoped)
│       ├── resources/META-INF/
│       │   └── persistence.xml      ← JPA config → jta-data-source: jdbc/ead
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml          ← FacesServlet, welcome-file: login.xhtml
│           │   ├── beans.xml        ← CDI activation
│           │   └── layout/
│           │       └── employee-layout.xhtml  ← Master layout (navbar + Bootstrap)
│           ├── resources/css/
│           │   └── employee.css
│           ├── login.xhtml          ← Trang đăng nhập
│           ├── view.xhtml           ← Danh sách nhân viên
│           ├── insert.xhtml         ← Thêm nhân viên
│           └── index.html           ← Redirect đơn giản
│
└── Set_01-ear/                      ← Module EAR (đóng gói)
    ├── pom.xml
    └── target/
        └── Set_01-ear-1.0.ear      ← File deploy lên Payara
```

---

## 3. Kiến trúc 3 tầng

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│              Set_01-web.war (JSF Facelets)               │
│                                                          │
│  login.xhtml ──► view.xhtml ──► insert.xhtml            │
│       │               │               │                  │
│       └───────────────┴───────────────┘                  │
│                       │                                  │
│              EmployeeManagedBean                         │
│          (@Named @SessionScoped CDI)                     │
└─────────────────────┬───────────────────────────────────┘
                      │ @EJB injection
┌─────────────────────▼───────────────────────────────────┐
│                   BUSINESS LOGIC LAYER                   │
│              Set_01-ejb.jar (EJB)                        │
│                                                          │
│   EmployeeFacadeLocal (interface @Local)                 │
│          │                                               │
│   EmployeeFacade (@Stateless)                            │
│          │ extends AbstractFacade<Employee>              │
│          │ @PersistenceContext(unitName="EmployeePU")    │
└─────────────────────┬───────────────────────────────────┘
                      │ JPA / EclipseLink
┌─────────────────────▼───────────────────────────────────┐
│                    DATA LAYER                            │
│        SQL Server 2022 — DB: EAD_Set01                  │
│              Table: dbo.Employee                         │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Chi tiết từng class

### 4.1. `Employee.java` — JPA Entity
- **Package:** `com.fptentity`
- **Table:** `dbo.Employee`
- **Fields:**

| Field | Column | Type | Constraint |
|---|---|---|---|
| `empCode` | `EmpCode` | `VARCHAR(10)` | `@Id` PRIMARY KEY |
| `password` | `Password` | `VARCHAR(20)` | NOT NULL |
| `name` | `Name` | `VARCHAR(40)` | NOT NULL |
| `age` | `Age` | `INT` | NOT NULL |

- **NamedQueries:** `Employee.findAll`, `Employee.findByEmpCode`, `Employee.findByPassword`, `Employee.findByName`, `Employee.findByAge`

---

### 4.2. `AbstractFacade<T>` — Generic CRUD
- **Package:** `com.fpt.sb`
- Cung cấp các method CRUD chuẩn cho mọi entity:

| Method | Mô tả |
|---|---|
| `create(T)` | `em.persist(entity)` |
| `edit(T)` | `em.merge(entity)` |
| `remove(T)` | `em.remove(em.merge(entity))` |
| `find(Object id)` | `em.find(entityClass, id)` |
| `findAll()` | CriteriaQuery SELECT tất cả |
| `findRange(int[])` | Phân trang: setFirstResult + setMaxResults |
| `count()` | COUNT(*) |

---

### 4.3. `EmployeeFacadeLocal` — EJB Local Interface
- **Package:** `com.fpt.sb`
- Kế thừa tất cả method từ `AbstractFacade` + bổ sung:

| Method | Mô tả |
|---|---|
| `checkLogin(username, password)` | Xác thực đăng nhập |
| `exists(empCode)` | Kiểm tra EmpCode đã tồn tại chưa |

---

### 4.4. `EmployeeFacade` — Stateless Session Bean
- **Package:** `com.fpt.sb`
- `@Stateless` + `extends AbstractFacade<Employee>` + `implements EmployeeFacadeLocal`
- `@PersistenceContext(unitName = "EmployeePU")` inject `EntityManager em`

**`checkLogin()`:**
```java
// JPQL truy vấn theo empCode VÀ password
SELECT e FROM Employee e WHERE e.empCode = :empCode AND e.password = :password
// Trả về true nếu kết quả > 0 dòng
```

**`exists()`:**
```java
// em.find() trả về null nếu không tồn tại
return em.find(Employee.class, empCode.trim()) != null;
```

---

### 4.5. `EmployeeManagedBean` — JSF Controller
- **Package:** `com.fpt.mb`
- `@Named("employeeManagedBean")` `@SessionScoped` `implements Validator<Object>`

**Properties:**

| Field | Type | Mô tả |
|---|---|---|
| `employeeFacade` | `EmployeeFacadeLocal` | `@EJB` injected |
| `employee` | `Employee` | Object bind với form insert |
| `username` | `String` | Bind với login form |
| `password` | `String` | Bind với login form |
| `part` | `Part` | File upload (transient) |
| `UPLOAD_DIRECTORY` | `String` | `"D:/uploads/"` (hardcoded) |

**Action methods:**

| Method | Trả về | Luồng |
|---|---|---|
| `login()` | `String` (navigation) | Gọi `checkLogin()` → redirect `view` hoặc show error |
| `view()` | `List<Employee>` | Gọi `findAll()` — dùng trực tiếp trong EL |
| `insert()` | `String` (navigation) | Build entity → check exists → persist → redirect `view` |
| `validate()` | `void` | Custom JSF Validator: so sánh confirmPassword với password |

---

## 5. Luồng hoạt động (User Flow)

### 5.1. Đăng nhập
```
[Browser: GET /Set_01-web/]
    │
    ▼ (web.xml: welcome-file = login.xhtml)
[login.xhtml]
    │ User nhập EmpCode + Password → nhấn Login
    │
    ▼ h:commandButton action="#{employeeManagedBean.login}"
[EmployeeManagedBean.login()]
    │
    ▼ employeeFacade.checkLogin(username, password)
    │   → JPQL query SQL Server
    │
    ├── true  → FacesMessage INFO + redirect "view?faces-redirect=true"
    └── false → FacesMessage ERROR + stay on login.xhtml
```

### 5.2. Xem danh sách
```
[view.xhtml]
    │ #{employeeManagedBean.view()} — gọi mỗi lần render
    │
    ▼ EmployeeManagedBean.view() → employeeFacade.findAll()
    │   → CriteriaQuery SELECT * FROM Employee
    │
    ▼ h:dataTable render: EmpCode | Name | Age
```

### 5.3. Thêm nhân viên
```
[insert.xhtml]
    │ User nhập EmpCode, Name, Password, ConfirmPassword, Age
    │
    │ JSF Validation phase:
    │   ├── f:validateLongRange(min=18) trên Age
    │   └── employeeManagedBean.validate() — so sánh confirmPassword ≠ password → ValidatorException
    │
    ▼ h:commandButton action="#{employeeManagedBean.insert}"
[EmployeeManagedBean.insert()]
    │
    ├── buildEmployeeForInsert() — trim tất cả string fields
    ├── employeeFacade.exists(empCode) == true → show error, return null (stay)
    ├── employeeFacade.create(newEmployee) → em.persist()
    └── redirect "view?faces-redirect=true"
```

---

## 6. Cấu hình JSF & CDI

**web.xml:**
- `FacesServlet` map với `*.xhtml`
- `jakarta.faces.PROJECT_STAGE = Development`
- `welcome-file: login.xhtml`
- Session timeout: 30 phút

**beans.xml:** CDI enabled (file rỗng — bean-discovery-mode mặc định)

**persistence.xml:**
```xml
<persistence-unit name="EmployeePU" transaction-type="JTA">
    <jta-data-source>jdbc/ead</jta-data-source>
</persistence-unit>
```

---

## 7. Schema Database

```sql
-- Database: EAD_Set01
CREATE TABLE dbo.Employee (
    EmpCode  VARCHAR(10)  NOT NULL,
    Password VARCHAR(20)  NOT NULL,
    Name     VARCHAR(40)  NOT NULL,
    Age      INT          NOT NULL,
    CONSTRAINT PK_Employee PRIMARY KEY (EmpCode),
    CONSTRAINT CK_Employee_Age CHECK (Age >= 18)
);

-- Dữ liệu mẫu (password tất cả: 123456)
-- E01: Bill Gates (60), E02: Barack Obama (55), E03: Rafael Nadal (30)
-- E04: Taylor Swift (36), E05: Cristiano Ronaldo (41)
```

---

## 8. Payara JDBC Configuration

| Thuộc tính | Giá trị |
|---|---|
| Connection Pool | `EAD_Pool` |
| JDBC Resource | `jdbc/ead` |
| Datasource class | `com.microsoft.sqlserver.jdbc.SQLServerDataSource` |
| Server | `localhost` |
| Port | `1433` |
| Database | `EAD_Set01` |
| User | `SA` |
| Password | `admin@123` |

---

## 9. Khởi động & Deploy

### Bước 1 — Kiểm tra Payara

```bash
# Kiểm tra Payara đang chạy chưa
ps aux | grep payara | grep -v grep

# Nếu chưa chạy → start Payara
/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin start-domain domain1
```

### Bước 2 — Start SQL Server (Docker)

```bash
# Kiểm tra container
docker ps -a | grep sqlserver

# Start container (tên: sqlserver, image: mssql/server:2022-latest)
docker start sqlserver

# Chờ ready (~5-10 giây)
docker exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'admin@123' -Q "SELECT 1" -No
```

### Bước 3 — Tạo database (nếu chưa có)

```bash
docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'admin@123' -No \
  -i /path/to/scriptDB.sql

# Hoặc chạy trực tiếp từ repo:
# File: /Users/maaitlunghau/Documents/SelfStudy/ead-practice/Set_01/scriptDB.sql
```

### Bước 4 — Kiểm tra Payara JDBC Pool

```bash
ASADMIN=/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin

# Kiểm tra pool đang dùng database nào
"$ASADMIN" get "resources.jdbc-connection-pool.EAD_Pool.property.databaseName"

# Nếu không phải EAD_Set01 → update
"$ASADMIN" set "resources.jdbc-connection-pool.EAD_Pool.property.databaseName=EAD_Set01"

# Ping test connection
"$ASADMIN" ping-connection-pool EAD_Pool
```

### Bước 5 — Build project (nếu có thay đổi source)

```bash
cd /Users/maaitlunghau/Documents/SelfStudy/ead-practice/Set_01
mvn clean package
```

### Bước 6 — Deploy EAR

```bash
ASADMIN=/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin
EAR=/Users/maaitlunghau/Documents/SelfStudy/ead-practice/Set_01/Set_01-ear/target/Set_01-ear-1.0.ear

"$ASADMIN" deploy --force=true "$EAR"
```

### Bước 7 — Verify

```bash
# Kiểm tra app đã deploy
/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin list-applications

# Test HTTP
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/Set_01-web/
# Kỳ vọng: 200
```

---

## 10. URL & Tài khoản

| Mục | Giá trị |
|---|---|
| URL ứng dụng | `http://localhost:8080/Set_01-web/` |
| Trang đăng nhập | `http://localhost:8080/Set_01-web/login.xhtml` |
| Payara Admin Console | `http://localhost:4848` |
| EmpCode mẫu | `E01` đến `E05` |
| Password mẫu | `123456` |

---

## 11. Lưu ý quan trọng

- `UPLOAD_DIRECTORY` trong `EmployeeManagedBean` hardcode là `"D:/uploads/"` — đây là path Windows, không hoạt động trên macOS. Tính năng upload file chưa được implement trong form (không có field upload trong `insert.xhtml`).
- `view()` được gọi **nhiều lần** mỗi lần render `view.xhtml` (mỗi `h:panelGroup` + `h:dataTable` đều gọi riêng) — không cache, query thẳng vào DB mỗi lần.
- `EmployeeManagedBean` implements `Validator<Object>` để dùng làm inline validator cho field confirmPassword thay vì tạo class Validator riêng.
- Pool `EAD_Pool` được dùng chung với Set_02 — khi chuyển giữa hai dự án phải update `databaseName` trong pool.

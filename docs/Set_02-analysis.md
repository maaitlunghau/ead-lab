# Set_02 — Phân tích & Tài liệu kỹ thuật

> Tài liệu này được tạo sau khi phân tích toàn bộ source code và deploy thành công Set_02.
> Đọc file này thay vì phân tích lại từ đầu trong các session tiếp theo.

---

## 1. Tổng quan dự án

| Thuộc tính | Giá trị |
|---|---|
| Tên dự án | Set_02 (EAD Practice) |
| Kiến trúc | Jakarta EE 10 — EAR (3-tier) |
| Application Server | Payara 7 |
| Database | Microsoft SQL Server 2022 (Docker) |
| Ngôn ngữ | Java 25 |
| Build tool | Maven |
| UI Framework | JSF (Facelets) + Bootstrap 5.3 |

**Chức năng chính:**
- Đăng nhập theo **role**: chỉ tài khoản có `role = admin` mới được vào
- Xem danh sách tài khoản (hiển thị cả ảnh đại diện)
- Tạo tài khoản mới với **upload ảnh** thực sự (lưu vào thư mục trong WAR)

**Điểm khác biệt cốt lõi so với Set_01:**

| | Set_01 | Set_02 |
|---|---|---|
| Entity | `Employee` (empCode PK string) | `Accounts` (accId PK auto-increment) |
| Login | Trả `boolean` | Trả `Accounts` object → lưu vào session |
| Phân quyền | Không | Có — chỉ `admin` được đăng nhập |
| Upload ảnh | Code có nhưng không dùng | Implement đầy đủ (UUID filename) |
| Cache data | Không (query mỗi lần render) | Có (`select()` gọi ở `@PostConstruct`) |
| JDBC Persistence Unit | `EmployeePU` | `AccountPU` |
| Database | `EAD_Set01` | `EAD_Set02` |

---

## 2. Cấu trúc thư mục

```
Set_02/
├── pom.xml                            ← Parent POM (groupId: com.fpt, version: 1.0)
├── scriptDB.sql                       ← Script tạo DB SQL Server
│
├── Set_02-ejb/                        ← Module EJB (business logic)
│   ├── pom.xml
│   └── src/main/java/
│       ├── com/fpt/entity/
│       │   └── Accounts.java          ← JPA Entity (@XmlRootElement, Bean Validation)
│       └── com/fpt/sb/
│           ├── AbstractFacade.java    ← Generic CRUD base class (giống Set_01)
│           ├── AccountsFacadeLocal.java  ← EJB Local Interface
│           └── AccountsFacade.java    ← @Stateless Session Bean
│
├── Set_02-web/                        ← Module Web (presentation)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fpt/mb/
│       │   └── AccountBean.java       ← JSF Controller (@Named @SessionScoped)
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml            ← FacesServlet + multipart-config (10MB)
│           │   ├── beans.xml          ← CDI activation
│           │   └── layout/
│           │       └── account-layout.xhtml  ← Master layout
│           ├── resources/css/
│           │   └── account.css
│           ├── login.xhtml            ← Trang đăng nhập
│           ├── account-list.xhtml     ← Danh sách tài khoản + ảnh
│           ├── account-create.xhtml   ← Tạo tài khoản + upload ảnh
│           └── index.html
│
└── Set_02-ear/                        ← Module EAR (đóng gói)
    └── target/
        └── Set_02-ear-1.0.ear        ← File deploy lên Payara
```

---

## 3. Kiến trúc 3 tầng

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│               Set_02-web.war (JSF Facelets)                  │
│                                                              │
│  login.xhtml ──► account-list.xhtml ──► account-create.xhtml│
│       │                  │                      │            │
│       └──────────────────┴──────────────────────┘            │
│                          │                                   │
│                    AccountBean                               │
│             (@Named @SessionScoped CDI)                      │
└─────────────────────────┬───────────────────────────────────┘
                          │ @EJB injection
┌─────────────────────────▼───────────────────────────────────┐
│                   BUSINESS LOGIC LAYER                       │
│              Set_02-ejb.jar (EJB)                            │
│                                                              │
│   AccountsFacadeLocal (interface @Local)                     │
│          │                                                   │
│   AccountsFacade (@Stateless)                                │
│          │ extends AbstractFacade<Accounts>                  │
│          │ @PersistenceContext(unitName="AccountPU")         │
└─────────────────────────┬───────────────────────────────────┘
                          │ JPA / EclipseLink
┌─────────────────────────▼───────────────────────────────────┐
│                     DATA LAYER                               │
│         SQL Server 2022 — DB: EAD_Set02                      │
│               Table: dbo.accounts                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Chi tiết từng class

### 4.1. `Accounts.java` — JPA Entity
- **Package:** `com.fpt.entity`
- **Table:** `dbo.accounts`
- **Annotations đặc biệt:** `@XmlRootElement` (JAXB), Bean Validation (`@NotNull`, `@Size`)
- **Fields:**

| Field | Column | Type | Constraint |
|---|---|---|---|
| `accId` | `AccId` | `INT IDENTITY(1,1)` | `@Id @GeneratedValue(IDENTITY)` |
| `username` | `Username` | `VARCHAR(10)` | `@NotNull @Size(1,10)` UNIQUE |
| `password` | `Password` | `VARCHAR(20)` | `@NotNull @Size(1,20)` |
| `role` | `Role` | `VARCHAR(10)` | `@NotNull @Size(1,10)` CHECK IN ('admin','user') |
| `image` | `Image` | `VARCHAR(200)` | `@NotNull @Size(1,200)` |

- **NamedQueries:** `Accounts.findAll`, `findByAccId`, `findByUsername`, `findByPassword`, `findByRole`, `findByImage`

---

### 4.2. `AbstractFacade<T>` — Generic CRUD
- Giống hệt Set_01 — cung cấp `create`, `edit`, `remove`, `find`, `findAll`, `findRange`, `count`.

---

### 4.3. `AccountsFacadeLocal` — EJB Local Interface
- **Package:** `com.fpt.sb`
- Kế thừa CRUD từ `AbstractFacade` + bổ sung:

| Method | Trả về | Mô tả |
|---|---|---|
| `checkLogin(username, password)` | `Accounts` | Trả về object nếu tìm thấy, `null` nếu không |

> **Khác Set_01:** trả về `Accounts` object thay vì `boolean`, cho phép lưu toàn bộ thông tin user vào session.

---

### 4.4. `AccountsFacade` — Stateless Session Bean
- **Package:** `com.fpt.sb`
- `@Stateless` + `extends AbstractFacade<Accounts>` + `implements AccountsFacadeLocal`
- `@PersistenceContext(unitName = "AccountPU")`

**`checkLogin()`:**
```java
// TypedQuery — type-safe hơn Set_01
String sql = "SELECT a FROM Accounts a WHERE a.username = :username AND a.password = :password";
TypedQuery<Accounts> query = em.createQuery(sql, Accounts.class);
return query.getSingleResult();  // ném NoResultException nếu không tìm thấy → trả null
```

---

### 4.5. `AccountBean` — JSF Controller
- **Package:** `com.fpt.mb`
- `@Named("accountBean")` `@SessionScoped` `implements Validator<Object>`

**Properties:**

| Field | Type | Mô tả |
|---|---|---|
| `accountsFacade` | `AccountsFacadeLocal` | `@EJB` injected |
| `accounts` | `List<Accounts>` | Cache danh sách — load lúc `@PostConstruct` |
| `acount` | `Accounts` | Object bind với form (typo: thiếu 'c') |
| `rePass` | `String` | Confirm password field |
| `part` | `Part` | File upload |
| `IMAGE_FOLDER` | `String` | `"images/accs"` (relative trong WAR) |

> **Lưu ý:** Bean có 2 getter cho cùng field: `getAcount()` và `getAccount()` — cả hai đều trả về `this.acount`. View dùng `accountBean.account`.

**`@PostConstruct init()`:**
```java
acount = new Accounts();
acount.setRole("user");  // default role khi tạo mới
select();                // load danh sách ngay khi bean khởi tạo
```

**Action methods:**

| Method | Trả về | Luồng |
|---|---|---|
| `login()` | `String` | `checkLogin()` → check `role == admin` → redirect `account-list` hoặc show error |
| `select()` | `void` | `findAll()` → cập nhật `this.accounts` (cache) |
| `create()` | `String` | Reset form + redirect `account-create?faces-redirect=true` |
| `insert()` | `String` | Upload ảnh → `persist()` → `select()` → redirect `account-list` |
| `validate()` | `void` | Custom Validator: so sánh confirmPassword vs password |

---

## 5. Luồng hoạt động (User Flow)

### 5.1. Đăng nhập (chỉ admin)
```
[Browser: GET /Set_02-web/]
    │
    ▼ (welcome-file: login.xhtml)
[login.xhtml]
    │ User nhập Username + Password → nhấn Login
    │ bind: #{accountBean.account.username} / #{accountBean.account.password}
    │
    ▼ action="#{accountBean.login}"
[AccountBean.login()]
    │
    ▼ accountsFacade.checkLogin(username, password)
    │   → TypedQuery SQL Server → getSingleResult() hoặc null
    │
    ├── null          → showMessage("Invalid...") → stay login
    ├── role != admin → showMessage("Invalid...") → stay login
    └── role == admin → acount = result (lưu vào session)
                      → showMessage("Login successfully!")
                      → redirect "account-list?faces-redirect=true"
```

### 5.2. Xem danh sách tài khoản
```
[account-list.xhtml]
    │ #{accountBean.accounts} — đọc từ cache (load ở @PostConstruct)
    │
    ▼ h:dataTable render: AccId | Username | Password | Role | Image
    │   Image: <h:graphicImage value="images/accs/#{item.image}"/>
    │   (ảnh được serve từ thư mục deploy của WAR)
    │
    [Create new account] → accountBean.create() → redirect account-create
```

### 5.3. Tạo tài khoản + Upload ảnh
```
[account-create.xhtml]
    │ Form: Username, Role (radio: user/admin), Password, ConfirmPassword, Image (file)
    │ enctype="multipart/form-data" — bắt buộc cho file upload
    │
    │ JSF Validation phase:
    │   ├── required fields validation
    │   └── accountBean.validate() — confirmPassword == password
    │
    ▼ action="#{accountBean.insert}"
[AccountBean.insert()]
    │
    ├── upload(part):
    │     realPath = FacesContext.getRealPath("/")    ← thư mục WAR đang deploy
    │     uploadPath = realPath + "images/accs"
    │     fileName = UUID.randomUUID() + extension    ← tránh trùng tên
    │     Files.copy(input, filePath)
    │     return fileName (UUID string)
    │
    ├── acount.setImage(fileName)
    ├── accountsFacade.create(acount) → em.persist()
    ├── select()  ← refresh cache
    └── redirect "account-list?faces-redirect=true"
```

---

## 6. Cấu hình JSF & CDI

**web.xml — điểm khác biệt với Set_01:**
```xml
<!-- File upload config — Set_02 có, Set_01 không -->
<multipart-config>
    <max-file-size>10485760</max-file-size>      <!-- 10 MB -->
    <max-request-size>10485760</max-request-size> <!-- 10 MB -->
    <file-size-threshold>1048576</file-size-threshold> <!-- 1 MB -->
</multipart-config>
```
- Welcome file: `login.xhtml`
- FacesServlet map `*.xhtml`
- Session timeout: 30 phút

**persistence.xml:**
```xml
<persistence-unit name="AccountPU" transaction-type="JTA">
    <jta-data-source>jdbc/ead</jta-data-source>
</persistence-unit>
```

---

## 7. Schema Database

```sql
-- Database: EAD_Set02
CREATE TABLE dbo.accounts (
    AccId      INT IDENTITY(1,1) NOT NULL,
    Username   VARCHAR(10)  NOT NULL,
    [Password] VARCHAR(20)  NOT NULL,
    [Role]     VARCHAR(10)  NOT NULL CONSTRAINT DF_accounts_Role DEFAULT ('admin'),
    [Image]    VARCHAR(200) NOT NULL,

    CONSTRAINT PK_accounts        PRIMARY KEY (AccId),
    CONSTRAINT UQ_accounts_Username UNIQUE (Username),
    CONSTRAINT CK_accounts_Role   CHECK ([Role] IN ('admin', 'user'))
);

-- Dữ liệu mẫu (password tất cả: 123456)
-- admin/admin, john/user, mary/user, peter/user, sarah/user
-- Chỉ admin được đăng nhập vào app
```

---

## 8. Payara JDBC Configuration

| Thuộc tính | Giá trị |
|---|---|
| Connection Pool | `EAD_Pool` (dùng chung với Set_01) |
| JDBC Resource | `jdbc/ead` |
| Datasource class | `com.microsoft.sqlserver.jdbc.SQLServerDataSource` |
| Server | `localhost` |
| Port | `1433` |
| Database | `EAD_Set02` ← phải set đúng khi deploy Set_02 |
| User | `SA` |
| Password | `admin@123` |

> **Quan trọng:** `EAD_Pool` dùng chung cho cả Set_01 và Set_02. Khi chuyển giữa 2 project phải update `databaseName` trong pool.

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

# Chờ ready
docker exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'admin@123' -Q "SELECT 1" -No
```

### Bước 3 — Tạo database (nếu chưa có)

```bash
docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'admin@123' -No << 'EOF'
USE master;
GO
IF DB_ID(N'EAD_Set02') IS NULL
BEGIN
    CREATE DATABASE EAD_Set02;
END;
GO
USE EAD_Set02;
GO
IF OBJECT_ID(N'dbo.accounts', N'U') IS NOT NULL
    DROP TABLE dbo.accounts;
GO
CREATE TABLE dbo.accounts (
    AccId      INT IDENTITY(1,1) NOT NULL,
    Username   VARCHAR(10)  NOT NULL,
    [Password] VARCHAR(20)  NOT NULL,
    [Role]     VARCHAR(10)  NOT NULL CONSTRAINT DF_accounts_Role DEFAULT ('admin'),
    [Image]    VARCHAR(200) NOT NULL,
    CONSTRAINT PK_accounts PRIMARY KEY (AccId),
    CONSTRAINT UQ_accounts_Username UNIQUE (Username),
    CONSTRAINT CK_accounts_Role CHECK ([Role] IN ('admin', 'user'))
);
GO
INSERT INTO dbo.accounts (Username, [Password], [Role], [Image]) VALUES
    ('admin', '123456', 'admin', 'images/admin.jpg'),
    ('john',  '123456', 'user',  'images/john.jpg'),
    ('mary',  '123456', 'user',  'images/mary.jpg'),
    ('peter', '123456', 'user',  'images/peter.jpg'),
    ('sarah', '123456', 'user',  'images/sarah.jpg');
GO
EOF
```

### Bước 4 — Cấu hình Payara Pool

```bash
ASADMIN=/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin

# Đổi databaseName sang EAD_Set02
"$ASADMIN" set "resources.jdbc-connection-pool.EAD_Pool.property.databaseName=EAD_Set02"

# Test connection
"$ASADMIN" ping-connection-pool EAD_Pool
```

### Bước 5 — Build (nếu có thay đổi source)

```bash
cd /Users/maaitlunghau/Documents/SelfStudy/ead-practice/Set_02
mvn clean package
```

### Bước 6 — Deploy EAR

```bash
ASADMIN=/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin
EAR=/Users/maaitlunghau/Documents/SelfStudy/ead-practice/Set_02/Set_02-ear/target/Set_02-ear-1.0.ear

"$ASADMIN" deploy --force=true "$EAR"
```

### Bước 7 — Verify

```bash
# Kiểm tra app đã deploy
/Users/maaitlunghau/Documents/webserver/payara7/bin/asadmin list-applications

# Test HTTP
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/Set_02-web/
# Kỳ vọng: 200
```

---

## 10. URL & Tài khoản

| Mục | Giá trị |
|---|---|
| URL ứng dụng | `http://localhost:8080/Set_02-web/` |
| Trang đăng nhập | `http://localhost:8080/Set_02-web/login.xhtml` |
| Payara Admin Console | `http://localhost:4848` |
| Tài khoản admin | username: `admin` / password: `123456` |
| Tài khoản user (không login được) | john, mary, peter, sarah / `123456` |

---

## 11. Lưu ý quan trọng

- **Chỉ role `admin` được login** — `login()` check `"admin".equalsIgnoreCase(result.getRole())`. User thường bị từ chối dù đúng mật khẩu.
- **Upload ảnh lưu trong WAR deploy dir** — path thực tế: `{payara_domain}/applications/Set_02-ear-1.0/Set_02-web.war/images/accs/`. Ảnh bị mất khi redeploy.
- **Tên trường bị typo:** field trong bean là `acount` (thiếu `c`) nhưng getter public là `getAccount()` — view dùng `#{accountBean.account}` hoạt động bình thường.
- **Data cache trong session:** `accounts` list load 1 lần ở `@PostConstruct` và refresh sau mỗi `insert()`. Nếu mở nhiều tab/session khác nhau, dữ liệu có thể không đồng bộ ngay.
- **Pool dùng chung với Set_01** — sau khi deploy Set_02 xong, nếu muốn test lại Set_01 phải đổi `databaseName` về `EAD_Set01`.
- **`@XmlRootElement` trên entity** — chuẩn bị cho khả năng expose REST/SOAP, chưa dùng trong project này.

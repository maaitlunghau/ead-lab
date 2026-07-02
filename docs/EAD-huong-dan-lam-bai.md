# Hướng dẫn làm bài EAD — Jakarta EE EAR Project

> Tài liệu này tổng hợp toàn bộ quy trình xây dựng một project EAD hoàn chỉnh theo kiểu Set_01/Set_02.  
> Mục tiêu: đọc tài liệu này là đủ để tự làm được bài thi.

---

## Mục lục

1. [Tư duy kiến trúc — Tại sao lại có 3 module?](#1-tư-duy-kiến-trúc)
2. [Thứ tự làm bài thi](#2-thứ-tự-làm-bài-thi)
3. [Bước 1 — Tạo Maven EAR Project](#bước-1--tạo-maven-ear-project)
4. [Bước 2 — Tạo Database + Script SQL](#bước-2--tạo-database--script-sql)
5. [Bước 3 — Tạo JPA Entity](#bước-3--tạo-jpa-entity)
6. [Bước 4 — Cấu hình persistence.xml](#bước-4--cấu-hình-persistencexml)
7. [Bước 5 — Tạo AbstractFacade](#bước-5--tạo-abstractfacade)
8. [Bước 6 — Tạo EJB Local Interface](#bước-6--tạo-ejb-local-interface)
9. [Bước 7 — Tạo EJB Implementation](#bước-7--tạo-ejb-implementation)
10. [Bước 8 — Tạo ManagedBean](#bước-8--tạo-managedbean)
11. [Bước 9 — Tạo XHTML Views](#bước-9--tạo-xhtml-views)
12. [Bước 10 — Cấu hình web.xml & beans.xml](#bước-10--cấu-hình-webxml--beansxml)
13. [Bước 11 — Build & Deploy](#bước-11--build--deploy)
14. [Bảng tra cứu nhanh — Annotations](#bảng-tra-cứu-nhanh--annotations)
15. [Điểm khác biệt Set_01 vs Set_02](#điểm-khác-biệt-set_01-vs-set_02)

---

## 1. Tư duy kiến trúc

### Tại sao cần 3 module?

```
EAR (file deploy lên Payara)
 ├── EJB module (.jar)  ←  Business Logic + Database
 └── Web module (.war)  ←  Giao diện người dùng (HTML)
```

| Module | Làm gì | Không biết gì |
|---|---|---|
| **EJB** | Nói chuyện với Database qua JPA | Không biết HTML, không biết request |
| **Web** | Hiển thị giao diện JSF, nhận input user | Không gọi thẳng Database |
| **EAR** | Đóng gói cả 2 để deploy 1 lần | — |

**Quy tắc vàng:**
```
Browser → JSF (xhtml) → ManagedBean → EJB → JPA → Database
                ↑ Web module ↑        ↑ EJB module ↑
```

> **Web gọi EJB. EJB gọi Database. Web KHÔNG BAO GIỜ gọi thẳng Database.**

### Luồng dữ liệu qua từng tầng

```
1. User nhập form trên browser
2. JSF nhận input, bind vào property của ManagedBean
3. JSF chạy Validation phase (validate required, range, custom validator)
4. Nếu pass validation → JSF gọi Action method của ManagedBean
5. ManagedBean gọi method của EJB (qua interface @Local)
6. EJB dùng EntityManager (JPA) thực hiện query SQL Server
7. Kết quả trả về ManagedBean → ManagedBean điều hướng sang trang tiếp theo
```

---

## 2. Thứ tự làm bài thi

```
Bước 1  → Tạo Maven EAR project (3 module)
Bước 2  → Viết script SQL → chạy tạo DB
Bước 3  → Viết Entity class (map 1-1 với bảng DB)
Bước 4  → Viết persistence.xml
Bước 5  → Viết AbstractFacade (thường copy mẫu, không đổi)
Bước 6  → Viết EJB Local Interface (khai báo method)
Bước 7  → Viết EJB Implementation (@Stateless + business logic)
Bước 8  → Viết ManagedBean (controller JSF)
Bước 9  → Viết XHTML views (login → view → insert)
Bước 10 → Cấu hình web.xml, beans.xml
Bước 11 → Build → Deploy → Test
```

> **Lý do làm theo thứ tự này:** Entity phụ thuộc vào DB schema → EJB phụ thuộc vào Entity → ManagedBean phụ thuộc vào EJB → XHTML phụ thuộc vào ManagedBean.

---

## Bước 1 — Tạo Maven EAR Project

### Hỗ trợ theo IDE

| IDE | Cách tạo |
|---|---|
| **NetBeans** | `New Project → Maven → Enterprise Application` → wizard tự tạo 3 module |
| **IntelliJ Ultimate** | `New Project → Jakarta EE → EAR` |
| **IntelliJ Community (free)** | Không có wizard → dùng Hướng A hoặc B bên dưới |
| **VSCode** | Không có wizard → dùng Hướng A hoặc B bên dưới |

---

### Hướng A — Copy Set_01 làm template (nhanh nhất, khuyến khích cho thi)

```bash
cp -r /path/to/Set_01 /path/to/MyProject
```

Sau đó **chỉ cần đổi tên** trong 4 file `pom.xml`, giữ nguyên cấu trúc thư mục, rồi xóa code cũ và viết lại.

Những chỗ cần đổi trong POM khi rename `Set_01` → `MyProject`:

| File | Dòng cần sửa |
|---|---|
| `pom.xml` (parent) | `<artifactId>Set_01</artifactId>` → `<artifactId>MyProject</artifactId>` |
| `ejb/pom.xml` | `<artifactId>Set_01-ejb</artifactId>` và `<parent><artifactId>Set_01</artifactId>` |
| `web/pom.xml` | `<artifactId>Set_01-web</artifactId>`, `<parent>`, và dependency vào EJB |
| `ear/pom.xml` | `<artifactId>Set_01-ear</artifactId>`, `<parent>`, và 2 dependencies EJB/Web |

---

### Hướng B — Tạo thủ công từ đầu (không cần IDE wizard)

Tạo cấu trúc thư mục sau rồi tạo 4 file pom.xml theo template bên dưới:

```
MyProject/
├── pom.xml                          ← 1. Parent POM
├── MyProject-ejb/
│   ├── pom.xml                      ← 2. EJB POM
│   └── src/main/
│       ├── java/                    ← Entity + EJB classes
│       └── resources/META-INF/
│           └── persistence.xml
├── MyProject-web/
│   ├── pom.xml                      ← 3. Web POM
│   └── src/main/
│       ├── java/                    ← ManagedBean classes
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml
│           │   ├── beans.xml
│           │   └── layout/
│           │       └── layout.xhtml
│           ├── login.xhtml
│           ├── view.xhtml
│           └── insert.xhtml
└── MyProject-ear/
    └── pom.xml                      ← 4. EAR POM
```

#### 1. Parent POM (`MyProject/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.fpt</groupId>
    <artifactId>MyProject</artifactId>
    <version>1.0</version>
    <packaging>pom</packaging>         <!-- bắt buộc: pom -->
    <name>MyProject-parent</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jakartaee>10.0.0</jakartaee>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>jakarta.platform</groupId>
                <artifactId>jakarta.jakartaee-api</artifactId>
                <version>${jakartaee}</version>
                <scope>provided</scope>  <!-- Payara cung cấp, không cần đóng gói -->
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- Khai báo 3 module con -->
    <modules>
        <module>MyProject-ejb</module>
        <module>MyProject-web</module>
        <module>MyProject-ear</module>
    </modules>
</project>
```

#### 2. EJB POM (`MyProject-ejb/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.fpt</groupId>
        <artifactId>MyProject</artifactId>   <!-- khớp với parent -->
        <version>1.0</version>
    </parent>

    <groupId>com.fpt</groupId>
    <artifactId>MyProject-ejb</artifactId>
    <version>1.0</version>
    <packaging>ejb</packaging>              <!-- bắt buộc: ejb -->
    <name>MyProject-ejb-1.0</name>

    <dependencies>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>${jakartaee}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 3. Web POM (`MyProject-web/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.fpt</groupId>
        <artifactId>MyProject</artifactId>
        <version>1.0</version>
    </parent>

    <groupId>com.fpt</groupId>
    <artifactId>MyProject-web</artifactId>
    <version>1.0</version>
    <packaging>war</packaging>              <!-- bắt buộc: war -->
    <name>MyProject-web-1.0</name>

    <dependencies>
        <!-- Jakarta EE API -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>${jakartaee}</version>
            <scope>provided</scope>
        </dependency>

        <!-- QUAN TRỌNG: Web module phải depend vào EJB module -->
        <!-- Không có dòng này → ManagedBean không import được interface EJB -->
        <dependency>
            <groupId>com.fpt</groupId>
            <artifactId>MyProject-ejb</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 4. EAR POM (`MyProject-ear/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.fpt</groupId>
        <artifactId>MyProject</artifactId>
        <version>1.0</version>
    </parent>

    <groupId>com.fpt</groupId>
    <artifactId>MyProject-ear</artifactId>
    <version>1.0</version>
    <packaging>ear</packaging>             <!-- bắt buộc: ear -->
    <name>MyProject-ear-1.0</name>

    <dependencies>
        <!-- Jakarta EE API -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>${jakartaee}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Đóng gói EJB module -->
        <dependency>
            <groupId>com.fpt</groupId>
            <artifactId>MyProject-ejb</artifactId>
            <version>1.0</version>
            <type>ejb</type>               <!-- bắt buộc: type=ejb -->
        </dependency>

        <!-- Đóng gói Web module -->
        <dependency>
            <groupId>com.fpt</groupId>
            <artifactId>MyProject-web</artifactId>
            <version>1.0</version>
            <type>war</type>               <!-- bắt buộc: type=war -->
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-ear-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <version>8</version>   <!-- EAR version 8 = Jakarta EE 10 -->
                    <modules>
                        <webModule>
                            <groupId>com.fpt</groupId>
                            <artifactId>MyProject-web</artifactId>
                            <bundleFileName>MyProject-web.war</bundleFileName>
                            <!-- context-root mặc định = tên WAR file không có .war -->
                            <!-- Thêm <contextRoot>/MyApp</contextRoot> nếu muốn đổi -->
                        </webModule>
                        <ejbModule>
                            <groupId>com.fpt</groupId>
                            <artifactId>MyProject-ejb</artifactId>
                            <bundleFileName>MyProject-ejb.jar</bundleFileName>
                        </ejbModule>
                    </modules>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Sau khi tạo xong 4 file POM, build thử để kiểm tra cấu trúc hợp lệ:

```bash
cd MyProject
mvn clean package -pl MyProject-ejb   # Build EJB trước
mvn clean package                     # Build toàn bộ
```

---

### Mối quan hệ giữa 4 file POM

```
Parent POM (packaging=pom)
 ├── quản lý version Jakarta EE dùng chung
 ├── khai báo 3 module con
 │
 ├── EJB POM (packaging=ejb)
 │    └── depends on: jakarta.jakartaee-api (provided)
 │
 ├── Web POM (packaging=war)
 │    ├── depends on: jakarta.jakartaee-api (provided)
 │    └── depends on: MyProject-ejb (provided) ← để import interface
 │
 └── EAR POM (packaging=ear)
      ├── depends on: MyProject-ejb (type=ejb) ← đóng gói vào EAR
      └── depends on: MyProject-web (type=war) ← đóng gói vào EAR
```

**`scope=provided`** = dependency này đã có sẵn trên Payara, không cần đưa vào file .ear.  
**`type=ejb` / `type=war`** = báo cho Maven EAR plugin biết đây là module cần đóng gói.

---

**Điểm cần chú ý trong POM:**
- `Set_01-web/pom.xml` phải khai báo dependency vào `Set_01-ejb` → ManagedBean mới import được interface EJB
- `Set_01-ear/pom.xml` khai báo cả EJB và Web làm module đóng gói

---

## Bước 2 — Tạo Database + Script SQL

Làm **đầu tiên** vì Entity phải map đúng tên column với DB.

```sql
USE master;
GO

-- Tạo database nếu chưa có
IF DB_ID(N'EAD_Set01') IS NULL
    CREATE DATABASE EAD_Set01;
GO

USE EAD_Set01;
GO

-- Tạo bảng
CREATE TABLE dbo.Employee (
    EmpCode  VARCHAR(10) NOT NULL,
    Password VARCHAR(20) NOT NULL,
    Name     VARCHAR(40) NOT NULL,
    Age      INT         NOT NULL,
    CONSTRAINT PK_Employee PRIMARY KEY (EmpCode),
    CONSTRAINT CK_Employee_Age CHECK (Age >= 18)
);
GO

-- Dữ liệu mẫu
INSERT INTO dbo.Employee (EmpCode, Password, Name, Age) VALUES
    ('E01', '123456', 'Bill Gates', 60),
    ('E02', '123456', 'Barack Obama', 55);
GO
```

**Lưu ý Set_02:** PK là `INT IDENTITY(1,1)` — auto-increment, không cần nhập tay.

---

## Bước 3 — Tạo JPA Entity

**Vị trí:** `Set_01-ejb/src/main/java/com/fptentity/Employee.java`

```java
@Entity                          // Đánh dấu class này = 1 bảng trong DB
@Table(name = "Employee")        // Tên bảng trong DB
@NamedQueries({
    @NamedQuery(name = "Employee.findAll",
                query = "SELECT e FROM Employee e"),
    @NamedQuery(name = "Employee.findByEmpCode",
                query = "SELECT e FROM Employee e WHERE e.empCode = :empCode")
})
public class Employee implements Serializable {

    @Id                          // Đây là PRIMARY KEY
    @Column(name = "EmpCode")    // Map field Java → column DB (khi tên khác nhau)
    private String empCode;

    @Column(name = "Password")
    private String password;

    @Column(name = "Name")
    private String name;

    @Column(name = "Age")
    private int age;

    // Constructor rỗng (bắt buộc cho JPA)
    public Employee() {}

    // Getters + Setters
    // hashCode() + equals() dựa trên empCode
    // toString()
}
```

**Giải thích annotations:**

| Annotation | Ý nghĩa |
|---|---|
| `@Entity` | Class này đại diện cho 1 bảng DB |
| `@Table(name)` | Chỉ định tên bảng (nếu khác tên class) |
| `@Id` | Cột Primary Key |
| `@Column(name)` | Map sang tên column cụ thể trong DB |
| `@GeneratedValue(IDENTITY)` | PK auto-increment (Set_02 dùng) |
| `@NamedQuery` | Đặt tên sẵn cho câu JPQL hay dùng |

**JPQL vs SQL:**

| | SQL | JPQL |
|---|---|---|
| Viết trên | Tên bảng, tên column | Tên class Java, tên field Java |
| Ví dụ | `SELECT * FROM Employee` | `SELECT e FROM Employee e` |
| WHERE | `WHERE EmpCode = 'E01'` | `WHERE e.empCode = :empCode` |

---

## Bước 4 — Cấu hình persistence.xml

**Vị trí:** `Set_01-ejb/src/main/resources/META-INF/persistence.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
             xmlns="https://jakarta.ee/xml/ns/persistence">

    <persistence-unit name="EmployeePU" transaction-type="JTA">
        <jta-data-source>jdbc/ead</jta-data-source>
    </persistence-unit>

</persistence>
```

**3 điểm bắt buộc phải nhớ:**

| Thuộc tính | Giá trị | Phải khớp với |
|---|---|---|
| `name="EmployeePU"` | Tên persistence unit | `@PersistenceContext(unitName="EmployeePU")` trong EJB |
| `transaction-type="JTA"` | Payara tự quản lý transaction | — (cố định) |
| `jdbc/ead` | Tên JDBC Resource | Tên resource đã tạo trên Payara Admin Console |

---

## Bước 5 — Tạo AbstractFacade

**Vị trí:** `Set_01-ejb/src/main/java/com/fpt/sb/AbstractFacade.java`

> Thường copy từ mẫu. Hiếm khi phải sửa.

```java
public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // Subclass phải cung cấp EntityManager
    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public int count() {
        CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
    }
}
```

**Tại sao dùng Generic `<T>`?** → Viết 1 lần, dùng cho mọi Entity. `EmployeeFacade extends AbstractFacade<Employee>` → `T` = `Employee`.

---

## Bước 6 — Tạo EJB Local Interface

**Vị trí:** `Set_01-ejb/src/main/java/com/fpt/sb/EmployeeFacadeLocal.java`

```java
@Local                           // Giao tiếp trong cùng JVM (không qua mạng)
public interface EmployeeFacadeLocal {

    // Khai báo lại các method từ AbstractFacade
    void create(Employee employee);
    void edit(Employee employee);
    void remove(Employee employee);
    Employee find(Object id);
    List<Employee> findAll();
    int count();

    // Bổ sung method đặc thù cho bài này
    boolean checkLogin(String username, String password);
    boolean exists(String empCode);
}
```

**Tại sao cần interface?**
- Web module chỉ **nhìn thấy interface**, không nhìn thấy implementation
- Tách biệt rõ ràng: Web biết "gọi được gì" mà không cần biết "code như thế nào"
- `@Local` = giao tiếp nội bộ trong cùng ứng dụng

---

## Bước 7 — Tạo EJB Implementation

**Vị trí:** `Set_01-ejb/src/main/java/com/fpt/sb/EmployeeFacade.java`

```java
@Stateless                                            // ① EJB annotation
public class EmployeeFacade
        extends AbstractFacade<Employee>              // ② Kế thừa CRUD
        implements EmployeeFacadeLocal {              // ③ Implement interface

    @PersistenceContext(unitName = "EmployeePU")      // ④ Inject EntityManager
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;                                    // AbstractFacade cần cái này
    }

    public EmployeeFacade() {
        super(Employee.class);                        // ⑤ Truyền class type cho AbstractFacade
    }

    // === Implement các method đặc thù ===

    @Override
    public boolean checkLogin(String username, String password) {
        Query query = em.createQuery(
            "SELECT e FROM Employee e " +
            "WHERE e.empCode = :empCode AND e.password = :password"
        );
        query.setParameter("empCode", username.trim());
        query.setParameter("password", password.trim());
        return query.getResultList().size() > 0;
    }

    @Override
    public boolean exists(String empCode) {
        if (empCode == null || empCode.trim().isEmpty()) return false;
        return em.find(Employee.class, empCode.trim()) != null;
    }
}
```

**Giải thích từng điểm:**

| Điểm | Ý nghĩa |
|---|---|
| `@Stateless` | Mỗi request dùng 1 instance từ pool, xong trả về. Không giữ state. |
| `extends AbstractFacade<Employee>` | Được thừa kế create/edit/remove/find/findAll |
| `implements EmployeeFacadeLocal` | Phải cài đặt đầy đủ các method trong interface |
| `@PersistenceContext` | Payara inject sẵn EntityManager đã kết nối DB |
| `super(Employee.class)` | Cho AbstractFacade biết đang làm việc với class nào |

---

## Bước 8 — Tạo ManagedBean

**Vị trí:** `Set_01-web/src/main/java/com/fpt/mb/EmployeeManagedBean.java`

```java
@Named("employeeManagedBean")       // ① Tên dùng trong EL: #{employeeManagedBean.xxx}
@SessionScoped                      // ② 1 instance per user session
public class EmployeeManagedBean implements Serializable, Validator<Object> {

    // === 1. Properties ===

    @EJB                            // ③ Inject EJB qua interface (không new trực tiếp)
    private EmployeeFacadeLocal employeeFacade;

    private Employee employee;      // Bind với form insert
    private String username;        // Bind với form login (field riêng)
    private String password;        // Bind với form login (field riêng)

    // === 2. Khởi tạo ===

    @PostConstruct                  // ④ Chạy ngay sau khi bean được tạo
    public void init() {
        employee = new Employee();  // Tạo object rỗng cho form
    }

    // === 3. Getters / Setters (bắt buộc để JSF bind được) ===
    // getEmployee(), setEmployee(), getUsername(), setUsername()...

    // === 4. Action Methods ===

    public String login() {
        if (employeeFacade.checkLogin(username, password)) {
            showInfoMessage("Login successfully.");
            return "view?faces-redirect=true"; // Redirect sang view.xhtml
        }
        showErrorMessage("Invalid username or password.");
        return null; // Ở lại trang login
    }

    public List<Employee> view() {
        return employeeFacade.findAll(); // Gọi EJB, không gọi DB trực tiếp
    }

    public String insert() {
        try {
            // Trim dữ liệu trước khi lưu
            Employee newEmp = buildEmployeeForInsert();

            // Kiểm tra trùng EmpCode
            if (employeeFacade.exists(newEmp.getEmpCode())) {
                showErrorMessage("Employee code already exists.");
                return null;
            }

            employeeFacade.create(newEmp);
            employee = new Employee(); // Reset form
            showInfoMessage("Insert employee successfully.");
            return "view?faces-redirect=true";
        } catch (Exception ex) {
            showErrorMessage("Cannot insert employee.");
            return null;
        }
    }

    // === 5. Custom Validator (implements Validator<Object>) ===

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        String confirmedPassword = value == null ? "" : value.toString();

        // Lấy giá trị của field password qua component ID
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

    // === 6. Utility methods ===

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

    private Employee buildEmployeeForInsert() {
        Employee e = new Employee();
        e.setEmpCode(employee.getEmpCode().trim());
        e.setPassword(employee.getPassword().trim());
        e.setName(employee.getName().trim());
        e.setAge(employee.getAge());
        return e;
    }
}
```

**Tóm tắt vai trò ManagedBean:**

```
ManagedBean = Cầu nối giữa XHTML và EJB
- Giữ data (properties) để XHTML bind vào
- Xử lý action (login, insert, view) khi user nhấn button
- Điều hướng (return "view?faces-redirect=true")
- Validate input (implements Validator<Object>)
```

---

## Bước 9 — Tạo XHTML Views

### Template Layout (Master Page)

**Vị trí:** `WEB-INF/layout/employee-layout.xhtml`

```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:ui="jakarta.faces.facelets">
<h:head>
    <title><ui:insert name="title">Employee Portal</ui:insert></title>
    <!-- Bootstrap CSS -->
</h:head>
<h:body>
    <!-- Navbar -->
    <div class="container mt-4">
        <!-- Flash messages -->
        <h:messages styleClass="alert alert-info"/>

        <!-- Nội dung từng trang sẽ được inject vào đây -->
        <ui:insert name="content"/>
    </div>
</h:body>
</html>
```

---

### login.xhtml

```xml
<ui:composition template="./WEB-INF/layout/employee-layout.xhtml"
                xmlns:ui="jakarta.faces.facelets"
                xmlns:h="jakarta.faces.html">

    <ui:define name="title">Login</ui:define>

    <ui:define name="content">
        <h:form id="loginForm">
            <!-- Bind vào field riêng của bean (KHÔNG bind vào entity) -->
            <h:inputText id="username"
                         value="#{employeeManagedBean.username}"
                         required="true"
                         requiredMessage="Employee code is required."/>
            <h:message for="username"/>

            <h:inputSecret id="password"
                           value="#{employeeManagedBean.password}"
                           required="true"
                           requiredMessage="Password is required."/>
            <h:message for="password"/>

            <h:commandButton value="Login"
                             action="#{employeeManagedBean.login}"/>
        </h:form>
    </ui:define>
</ui:composition>
```

---

### view.xhtml

```xml
<ui:define name="content">
    <!-- Link sang trang insert -->
    <h:link outcome="insert" value="Add employee"/>

    <!-- Danh sách nhân viên -->
    <h:dataTable value="#{employeeManagedBean.view()}" var="item">
        <h:column>
            <f:facet name="header">Employee Code</f:facet>
            #{item.empCode}
        </h:column>
        <h:column>
            <f:facet name="header">Full Name</f:facet>
            #{item.name}
        </h:column>
        <h:column>
            <f:facet name="header">Age</f:facet>
            #{item.age}
        </h:column>
    </h:dataTable>
</ui:define>
```

---

### insert.xhtml — Điểm phức tạp nhất

```xml
<ui:define name="content">
    <h:form id="employeeForm">

        <!-- Bind vào #{employeeManagedBean.employee.xxx} (qua entity) -->
        <h:inputText id="empCode"
                     value="#{employeeManagedBean.employee.empCode}"
                     maxlength="10"
                     required="true"
                     requiredMessage="Employee code is required."/>
        <h:message for="empCode"/>

        <h:inputText id="name"
                     value="#{employeeManagedBean.employee.name}"
                     required="true"/>
        <h:message for="name"/>

        <!-- Password: thêm redisplay="true" để giữ lại giá trị sau lỗi -->
        <h:inputSecret id="password"
                       value="#{employeeManagedBean.employee.password}"
                       redisplay="true"
                       required="true"/>
        <h:message for="password"/>

        <!-- Confirm Password: gọi bean làm validator -->
        <h:inputSecret id="confirmPassword"
                       required="true"
                       validator="#{employeeManagedBean.validate}">
            <!-- Truyền ID của field password để validator tìm đến so sánh -->
            <f:attribute name="checkPass" value="password"/>
        </h:inputSecret>
        <h:message for="confirmPassword"/>

        <!-- Age: dùng validator built-in của JSF -->
        <h:inputText id="age"
                     value="#{employeeManagedBean.employee.age}"
                     required="true"
                     converterMessage="Age must be a whole number."
                     validatorMessage="Employee must be at least 18 years old.">
            <f:validateLongRange minimum="18"/>
        </h:inputText>
        <h:message for="age"/>

        <h:commandButton value="Save"
                         action="#{employeeManagedBean.insert}"/>
    </h:form>
</ui:define>
```

**Cách validator custom hoạt động:**

```
1. JSF Validation phase chạy trên field confirmPassword
2. Gọi employeeManagedBean.validate(context, component, value)
3. value = giá trị user nhập vào confirmPassword
4. component.getAttributes().get("checkPass") → trả về "password" (ID của field password)
5. component.findComponent("password") → lấy được UIInput của field password
6. Lấy getValue() của UIInput đó → so sánh với value
7. Nếu khác nhau → throw ValidatorException → JSF hiển thị lỗi
```

---

## Bước 10 — Cấu hình web.xml & beans.xml

### web.xml

**Vị trí:** `Set_01-web/src/main/webapp/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="6.0">

    <!-- Khai báo JSF Servlet -->
    <servlet>
        <servlet-name>FacesServlet</servlet-name>
        <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <!-- Mọi file .xhtml đều đi qua FacesServlet -->
    <servlet-mapping>
        <servlet-name>FacesServlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>

    <!-- Trang mặc định khi vào / -->
    <welcome-file-list>
        <welcome-file>login.xhtml</welcome-file>
    </welcome-file-list>

    <!-- Session timeout: 30 phút -->
    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>

    <!-- Set_02 thêm phần này để hỗ trợ file upload -->
    <!--
    <multipart-config>
        <max-file-size>10485760</max-file-size>
        <max-request-size>10485760</max-request-size>
    </multipart-config>
    -->

</web-app>
```

### beans.xml

**Vị trí:** `Set_01-web/src/main/webapp/WEB-INF/beans.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       bean-discovery-mode="annotated">
    <!-- File này bật CDI để @Named, @SessionScoped, @EJB hoạt động -->
</beans>
```

> `beans.xml` chỉ cần tồn tại là CDI được bật. Nội dung có thể để trống hoặc minimal như trên.

---

## Bước 11 — Build & Deploy

### Build project

```bash
# Đứng ở thư mục root của project (nơi có parent pom.xml)
cd /path/to/Set_01
mvn clean package
# → Tạo ra file Set_01-ear/target/Set_01-ear-1.0.ear
```

### Deploy lên Payara

```bash
ASADMIN=/path/to/payara7/bin/asadmin
EAR=/path/to/Set_01-ear/target/Set_01-ear-1.0.ear

# Đảm bảo pool trỏ đúng DB
"$ASADMIN" set "resources.jdbc-connection-pool.EAD_Pool.property.databaseName=EAD_Set01"
"$ASADMIN" ping-connection-pool EAD_Pool

# Deploy (--force=true để redeploy nếu đã có)
"$ASADMIN" deploy --force=true "$EAR"
```

### Verify

```bash
# Kiểm tra app đã deploy
"$ASADMIN" list-applications

# Test HTTP
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/Set_01-web/
# Kỳ vọng: 200
```

---

## Bảng tra cứu nhanh — Annotations

### EJB module

| Annotation | Dùng ở đâu | Ý nghĩa |
|---|---|---|
| `@Entity` | Entity class | Đây là bảng DB |
| `@Table(name)` | Entity class | Tên bảng trong DB |
| `@Id` | Field trong Entity | Đây là Primary Key |
| `@Column(name)` | Field trong Entity | Map sang tên column DB |
| `@GeneratedValue(IDENTITY)` | Field `@Id` | PK auto-increment |
| `@NamedQuery` | Entity class | Định nghĩa JPQL được đặt tên |
| `@Stateless` | EJB class | Session Bean stateless |
| `@PersistenceContext` | EntityManager field | Inject EntityManager từ container |
| `@Local` | EJB interface | Giao tiếp nội bộ (cùng JVM) |

### Web module

| Annotation | Dùng ở đâu | Ý nghĩa |
|---|---|---|
| `@Named("tên")` | ManagedBean class | Đặt tên để dùng trong EL `#{}` |
| `@SessionScoped` | ManagedBean class | 1 instance per user session |
| `@EJB` | Field trong ManagedBean | Inject EJB vào bean |
| `@PostConstruct` | Method trong ManagedBean | Chạy ngay sau khi bean được tạo |

### JSF tags hay dùng

| Tag | Ý nghĩa |
|---|---|
| `h:form` | Form HTML |
| `h:inputText` | Input text |
| `h:inputSecret` | Input password |
| `h:commandButton action="#{bean.method}"` | Button gọi action method |
| `h:message for="fieldId"` | Hiển thị lỗi của field |
| `h:messages` | Hiển thị tất cả messages |
| `h:dataTable value="#{bean.list}" var="item"` | Render bảng từ danh sách |
| `h:link outcome="pageName"` | Link điều hướng |
| `h:graphicImage value="path"` | Hiển thị ảnh |
| `f:validateLongRange minimum="18"` | Validate số nguyên có giới hạn |
| `f:attribute name="key" value="val"` | Truyền attribute vào component |
| `ui:composition template="..."` | Dùng layout template |
| `ui:define name="slot"` | Điền vào slot của template |
| `ui:insert name="slot"` | Khai báo slot trong template |

---

## Điểm khác biệt Set_01 vs Set_02

| Điểm | Set_01 | Set_02 |
|---|---|---|
| **Entity** | `Employee` — PK `VARCHAR` nhập tay | `Accounts` — PK `INT IDENTITY` tự tăng |
| **Login field** | Bind vào 2 property riêng (`username`, `password`) | Bind thẳng vào entity `#{accountBean.account.username}` |
| **checkLogin trả về** | `boolean` | `Accounts` object (lưu vào session) |
| **Phân quyền** | Không có | Chỉ `admin` được đăng nhập |
| **Upload ảnh** | Code có nhưng không dùng | Implement đầy đủ (UUID + lưu vào WAR dir) |
| **Data cache** | `view()` query DB mỗi lần render | `@PostConstruct` load 1 lần, refresh sau insert |
| **Persistence Unit** | `EmployeePU` | `AccountPU` |
| **Database** | `EAD_Set01` | `EAD_Set02` |
| **Context root** | `/Set_01-web` | `/Set_02` |

### Upload ảnh trong Set_02

```java
private String upload(Part part) throws IOException {
    // 1. Lấy đường dẫn thực tế của WAR đang chạy trên Payara
    String realPath = FacesContext.getCurrentInstance()
                                  .getExternalContext()
                                  .getRealPath("/");

    // 2. Tạo thư mục images/accs trong WAR nếu chưa có
    Path uploadPath = Paths.get(realPath, "images/accs");
    Files.createDirectories(uploadPath);

    // 3. Đặt tên file = UUID (tránh trùng)
    String extension = getFileExtension(getFileName(part));
    String fileName = UUID.randomUUID().toString() + extension;

    // 4. Ghi file
    try (InputStream input = part.getInputStream()) {
        Files.copy(input, uploadPath.resolve(fileName), REPLACE_EXISTING);
    }

    return fileName; // Lưu tên này vào DB
}
```

**View render ảnh:**
```xml
<h:graphicImage value="images/accs/#{item.image}"/>
```

> **Lưu ý:** ảnh bị mất khi redeploy vì lưu trong thư mục deploy của WAR.

---

## Lỗi hay gặp & Cách fix

| Lỗi | Nguyên nhân | Cách fix |
|---|---|---|
| `javax.persistence.NoResultException` | `getSingleResult()` không tìm thấy kết quả | Bắt exception và return null |
| `EmpCode already exists` không hiện | `exists()` check sai field | Kiểm tra lại tên field trong `em.find()` |
| Confirm password không validate | `f:attribute name="checkPass"` sai ID | Đảm bảo value khớp đúng với `id` của field password |
| 404 sau khi deploy | Context root sai | Kiểm tra log Payara để thấy context root thực tế |
| Pool ping fail | DB chưa start hoặc sai databaseName | Start Docker container, kiểm tra lại pool config |
| `@EJB` inject null | Web POM thiếu dependency vào EJB module | Thêm dependency trong `Set_01-web/pom.xml` |

---

*Tài liệu này tổng hợp từ phân tích project Set_01 và Set_02. Xem thêm: `Set_01-analysis.md`, `Set_02-analysis.md`.*

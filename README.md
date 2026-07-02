# EAD Practice — Enterprise Application Development

A personal repository for practicing and mastering **Enterprise Application Development (EAD)** assignments at **FPT Aptech**. Each set in this repository represents a complete, working solution built from scratch to reinforce hands-on skills with Jakarta EE technologies.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21+ / Jakarta EE 10 |
| Application Server | Payara 7 |
| Web Framework | JSF 4 (Facelets) |
| Business Logic | EJB 4 (Stateless Session Beans) |
| Persistence | JPA 3 (via EntityManager) |
| Database | Microsoft SQL Server 2022 |
| Build Tool | Apache Maven 3 |
| UI | Bootstrap 5 |

---

## Project Structure

Each `Set_XX` follows the same 3-module Maven EAR architecture:

```
Set_XX/
├── Set_XX-ejb/        # Business logic — JPA Entity, EJB Facade
├── Set_XX-web/        # Web layer — JSF ManagedBean, XHTML pages
├── Set_XX-ear/        # Packages ejb + war into a deployable .ear
├── scriptDB.sql       # Database schema + sample data (SQL Server)
└── pom.xml            # Parent Maven POM
```

---

## Assignment Sets

| Set | Topic | Login Rule | Key Feature |
|-----|-------|-----------|-------------|
| [Set_01](./Set_01/) | Employee Management | Any employee | Manual PK, Age ≥ 18 validation |
| [Set_02](./Set_02/) | Account Management | Admin only | Auto-increment PK, image upload, role-based |

---

## Prerequisites

Make sure the following are installed before running any project:

- **Java 21+**
- **Apache Maven 3.9+**
- **Payara 7** — configured with a JDBC pool named `EAD_Pool` and JDBC resource `jdbc/ead`
- **SQL Server 2022** — running on `localhost:1433` (Docker recommended)
- **SQL Server JDBC Driver** (`mssql-jdbc`) — placed in Payara's `domain1/lib/`

---

## Running a Project

### 1. Start SQL Server
```bash
docker start sqlserver
```

### 2. Start Payara
```bash
~/Documents/webserver/payara7/bin/asadmin start-domain
```

### 3. Switch database target
```bash
# For Set_01
asadmin set "resources.jdbc-connection-pool.EAD_Pool.property.databaseName=EAD_Set01"

# For Set_02
asadmin set "resources.jdbc-connection-pool.EAD_Pool.property.databaseName=EAD_Set02"
```

### 4. Build & Deploy
```bash
cd Set_01   # or Set_02
mvn clean package -q
asadmin deploy Set_01-ear/target/Set_01-ear-1.0.ear
```

### 5. Open in browser

| Set | URL | Credentials |
|-----|-----|------------|
| Set_01 | http://localhost:8080/Set_01-web/ | EmpCode: `E01` / Password: `123456` |
| Set_02 | http://localhost:8080/Set_02/ | Username: `admin` / Password: `123456` |

---

## Key Patterns

Every set follows the same layered pattern — once you understand one, you understand them all:

```
XHTML Page  →  ManagedBean (@Named @SessionScoped)
                    ↓  @EJB injection
             XxxFacade (@Stateless)
               extends AbstractFacade<T>
                    ↓  @PersistenceContext
             EntityManager  →  SQL Server DB
```

**Files to focus on per set:**

| File | Role |
|------|------|
| `XxxEntity.java` | Maps to a DB table via JPA `@Entity` |
| `AbstractFacade.java` | Generic CRUD (create, findAll, find, edit, remove) |
| `XxxFacadeLocal.java` | EJB local interface |
| `XxxFacade.java` | EJB implementation + custom queries |
| `XxxManagedBean.java` | JSF controller — handles login, view, insert |
| `login.xhtml` | Login form with JSF validation |
| `view/list.xhtml` | Data table listing all records |
| `insert/create.xhtml` | Form to add a new record |
| `persistence.xml` | Declares JPA persistence unit → `jdbc/ead` |
| `web.xml` | Maps `*.xhtml` to FacesServlet |

---

## About

**maaitlunghau**
Co-Founder · Engineering Lead — [MST Software](https://github.com/maaitlunghau) *(MMO Solution Technology)*
Student — FPT Aptech

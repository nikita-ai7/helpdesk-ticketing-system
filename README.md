\# Multi-Tenant Helpdesk \& Ticketing System



A full-stack helpdesk application built with \*\*Java 21, Spring Boot, Spring Security, PostgreSQL, React, and Vite\*\*. The system allows multiple organizations to manage support tickets while enforcing organization-level data isolation.



\## Features



\### Authentication and User Roles

\- HTTP Basic authentication with Spring Security.

\- Three roles: `ORG\_ADMIN`, `AGENT`, and `CUSTOMER`.

\- Authenticated users are associated with an organization.

\- Role-based access to ticket operations and internal support information.



\### Organization and Tenant Isolation

\- Supports multiple organizations.

\- Users and tickets are associated with the appropriate organization.

\- Users cannot access tickets belonging to another organization.

\- Customers can view their own tickets.

\- Ticket assignment is restricted to agents in the same organization.



\### Ticket Management

\- Create, view, update status, assign, and close tickets.

\- Ticket fields include subject, description, priority, status, requester, assigned agent, and timestamps.

\- Search and filter tickets by status, priority, and assigned agent.

\- Paginated ticket listing.



\### Comments and History

\- Add comments to tickets.

\- Support internal notes that are hidden from customers.

\- View ticket history for authorized staff.



\### API and Validation

\- RESTful API endpoints.

\- Request validation and appropriate HTTP status codes.

\- OpenAPI/Swagger API documentation.



\## Technology Stack



\*\*Backend\*\*

\- Java 21

\- Spring Boot

\- Spring Security

\- Spring Data JPA / Hibernate

\- PostgreSQL

\- Maven

\- JUnit 5



\*\*Frontend\*\*

\- React

\- Vite

\- JavaScript

\- HTML and CSS



\## Prerequisites



Install the following before running the project:



\- Java 21 or compatible JDK

\- Node.js and npm

\- Docker Desktop

\- Git (if cloning the repository)



\## Database Setup



The application uses PostgreSQL.



Ensure PostgreSQL is available at `localhost:5432`. The configured database and username are:



\- Database: `helpdesk`

\- Username: `devrag`



Set the database password as an environment variable in the PowerShell terminal used to run the backend. Do not commit database credentials to source control.





$securePassword = Read-Host "Enter DB password" -AsSecureString

$env:DB\_PASSWORD = \[System.Net.NetworkCredential]::new("", $securePassword).Password





The database must exist before starting the application. Hibernate is configured with `ddl-auto: update` for local development.



If using the existing PostgreSQL Docker container, ensure it is running and that the `helpdesk` database has been created.



\## Running the Backend



Open PowerShell and navigate to the backend directory:



cd backend





Set the database password in this same terminal if it has not already been set:





$securePassword = Read-Host "Enter DB password" -AsSecureString

$env:DB\_PASSWORD = \[System.Net.NetworkCredential]::new("", $securePassword).Password





Set the timezone for local execution:





$env:JAVA\_TOOL\_OPTIONS = "-Duser.timezone=Asia/Kolkata"





Run the Spring Boot application:

.\\mvnw.cmd spring-boot:run





The backend runs at:



`http://localhost:8080`



Keep this terminal open while using the application.



\## Running the Frontend



Open a separate PowerShell terminal from the project root:





cd frontend

npm install

npm run dev





Open the frontend in your browser:



`http://localhost:5173`



The Vite development server proxies API requests to the backend running on port 8080.



\## Demo Accounts



The application includes seeded demo organizations and users.



\*\*Demo password:\*\* `Password@123`



| Organization | Role | Email |

|---|---|---|

| Acme | Organization Admin | `admin@acme.test` |

| Acme | Support Agent | `agent@acme.test` |

| Acme | Customer | `customer@acme.test` |

| Globex | Organization Admin | `admin@globex.test` |

| Globex | Support Agent | `agent@globex.test` |

| Globex | Customer | `customer@globex.test` |



Use the appropriate organization and email when logging in. Demo credentials are for local assessment/testing only and must not be used in production.



\## API Documentation



Once the backend is running, open Swagger UI:



`http://localhost:8080/swagger-ui/index.html`



OpenAPI specification:



`http://localhost:8080/v3/api-docs`



\## Main API Endpoints



| Method | Endpoint | Purpose |

|---|---|---|

| GET | `/api/users/me` | Get the authenticated user's details |

| GET | `/api/users/agents` | List agents in the authenticated user's organization |

| POST | `/api/tickets` | Create a ticket |

| GET | `/api/tickets` | List, search, filter, and paginate tickets |

| GET | `/api/tickets/{ticketId}` | Get ticket details |

| PATCH | `/api/tickets/{ticketId}/status` | Update ticket status |

| PATCH | `/api/tickets/{ticketId}/assignee` | Assign a ticket |

| GET | `/api/tickets/{ticketId}/comments` | List ticket comments |

| POST | `/api/tickets/{ticketId}/comments` | Add a comment or internal note |

| GET | `/api/tickets/{ticketId}/history` | View ticket history for authorized staff |



All protected endpoints require authentication.



\## Running Tests



From the backend directory, run:





.\\mvnw.cmd test





Make sure `DB\_PASSWORD` is set in the current terminal when tests require the Spring application context and database connection.



\## Production Build



Build the frontend:





cd frontend

npm run build





The production frontend assets are generated in `frontend/dist/`.



\## Security and Tenant Isolation



The application uses the authenticated user's organization context when retrieving tickets and agents. Ticket access is checked against the user's organization, and customers are restricted to their own tickets. Internal notes are not exposed to customers.



For assessment verification, test with users from both Acme and Globex and confirm that cross-organization ticket access is denied.



\## Assumptions and Limitations



\- Demo accounts and seed data are provided for local assessment/testing.

\- The application is configured for local development rather than production deployment.

\- Database schema updates are managed by Hibernate for local development.

\- Authentication uses HTTP Basic; production deployments should use HTTPS and an appropriate production authentication strategy.

\- Any additional limitations or incomplete features should be documented before submission.



\## Project Structure



helpdesk/

|-- backend/        # Spring Boot REST API

|-- frontend/       # React application

|-- .gitignore

`-- README.md       # Project setup and documentation




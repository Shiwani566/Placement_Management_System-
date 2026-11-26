# PlaceMate – Smart Placement Management System

PlaceMate is a web-based Smart Placement Management System developed using Java and Spring Boot. It is designed to simplify and manage the campus placement process through a centralized platform.

The system provides separate dashboards for Students, Recruiters, and Administrators to manage placement activities efficiently.

## Project Status

**Deployed – Live Application**

## Features

### Student Module
- Student Registration and Login
- Student Profile Management
- View Placement Drives
- Automatic Eligibility Checking
- Apply for Placement Drives
- Track Application Status

### Recruiter Module
- Recruiter Login
- Recruiter Dashboard
- Manage Own Placement Drives
- View Applications
- Update Application Status

### Admin Module
- Admin Login
- Admin Dashboard
- Manage Students
- Manage Recruiters
- Manage Placement Drives
- Manage Applications
- View Reports and Statistics
- Enable and Disable Student Accounts

## Tech Stack

**Backend**
- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT Authentication
- Maven
- REST APIs

**Frontend**
- HTML
- CSS
- JavaScript

**Database**
- MySQL (Local Development)
- TiDB Cloud (Production)

**Deployment & Tools**
- Docker
- Render
- Git & GitHub
- Postman
- Swagger / OpenAPI
- IntelliJ IDEA

## Security
- JWT-based authentication
- BCrypt password encryption
- Role-based access control
- Protected REST APIs
- Student data access restrictions

## Eligibility Checking

The system checks student eligibility based on:
- Minimum CGPA
- Eligible Branch
- Graduation Year
- Maximum Backlogs

## Deployment

The application is deployed on Render using Docker.

The production database is hosted on TiDB Cloud.

**Live Application:** https://placement-management-system-5csz.onrender.com

## Author

Shiwani Gupta

B.Tech – Computer Science and Engineering
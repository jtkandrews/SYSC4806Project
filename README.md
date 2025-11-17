# Amazin Bookstore
<div align="center">
 <img width="429" height="181" alt="ChatGPT_Image_Nov_3__2025_at_02_47_27_PM-removebg-preview-3" src="https://github.com/user-attachments/assets/f96af1b1-8203-4a8f-be63-80bea02dc3e6" />
</div>


SYSC4806 Group Project - JS Svelte

[![CI - Maven](https://github.com/jtkandrews/SYSC4806Project/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/jtkandrews/SYSC4806Project/actions/workflows/deploy.yml)

# Running the Application Locally
In the terminal, run the following: \
`cd SvelteAmazin` 

`./mvnw package` 

`java -jar target/SvelteAmazin-0.0.1-SNAPSHOT.jar`



# Milestone 1 ![Milestone progress](https://img.shields.io/github/milestones/progress/jtkandrews/SYSC4806Project/1)

<details>
<summary>Summary of Milestone 1</summary>
 
## Objectives Completed
- [x] Basic Backend with Books and their details 
- [x] Svelte Frontend
- [x] Integrate with CI
- [x] Deployed on Azure
- [x] Setup Maven and Springboot Structure
- [x] Requirements outlined in [Wiki](https://github.com/jtkandrews/SYSC4806Project/wiki/Requirements)
- [x] Created Test Cases

## Plan for Sprint 2 
- [ ] Implement Checkout Feature with Inventory Tracking
- [ ] Add New Backend Features
- [ ] Expand Upon Unit Tests
- [ ] Experiment with Jaccard distance
- [ ] Expand on Requirements (if needed)
- [ ] Furthur UI Improvements
- [ ] Add new features accessible by UI

 ## Current State of the Project
Currently, the project has the bare-bones elements configured. As seen here, the GitHub repo has been established with CI/CD integrations. Additionally, the Azure configuration has been complete, and the current version of the webapp can be seen [here](https://sysc4806project.azurewebsites.net/). 

In terms of implementation, the backend is configured as a Spring Boot app which exposes a read-only GET endpoint to list current library of books. This information is displayed via Svelte on the frontend, which fetches the books and displays a stylized grid of said books. 

In regard to deployment, the frontend build is currently being copied into Spring Boot's static resources, from which a single JAR is created. We recognize this situation may not be ideal, and as such we have future plans to potentially address this process. 
<div align="center">
 <img width="800" alt="image" src="https://github.com/user-attachments/assets/1de3dd62-04cf-40aa-aed5-bf20039675b7" />
<img width="800" alt="image" src="https://github.com/user-attachments/assets/66d07112-e265-4e00-82de-1c39ffac60a8" />
</div>

## Database Schema

The system uses a single `BOOKS` table to store all book information.  
Each record represents one book listed in the bookstore.

| Column Name | Type | Description |
|--------------|------|-------------|
| `isbn` | VARCHAR(20) **PK** | Unique identifier for each book |
| `title` | VARCHAR(255) | Title of the book |
| `author` | VARCHAR(255) | Name of the author |
| `publisher` | VARCHAR(255) | Publisher of the book |
| `genre` | VARCHAR(255) | Book genre (e.g., Fiction, Science, Biography) |
| `price` | DOUBLE PRECISION | Price of the book |
| `inventory` | INTEGER | Number of copies available |
| `image_url` | VARCHAR(255) | URL of the book cover image |

## UML Model
<div align="center">
<img width="400"" alt="image" src="https://github.com/user-attachments/assets/b8d048d5-4284-46df-b492-5272a31d62ed" />
</div>

</details>

# Milestone 2 ![Milestone progress](https://img.shields.io/github/milestones/progress/jtkandrews/SYSC4806Project/2)

<details>
<summary>Summary of Milestone 2</summary>
 
## Objectives Completed
- [x] Checkout Feature Completed
- [x] Began development for book recommendations
- [x] Added a protected user and owner mode
- [x] Added the ability for the protected owner mode to add and edit book details

## Plan for Sprint 3
- [ ] Add User accounts
- [ ] Improve UI
- [ ] Finish book recomendations

 ## Current State of the Project
After Milestone 2 our project now has:
- Protected User and Owner roles
- Users can checkout books and Owners can edit book details, including stock and add books.
- A Checkout feature with inventory tracking (to be expanded upon for Milestone 3)
- A recommended list of books
The current version of the webapp can be seen [here](https://sysc4806project.azurewebsites.net/). 

In terms of implementation, the backend is configured as a Spring Boot app which exposes a read-only GET endpoint to list current library of books. This information is displayed via Svelte on the frontend, which fetches the books and displays a stylized grid of said books. 

In regard to deployment, the frontend build is currently being copied into Spring Boot's static resources, from which a single JAR is created. We recognize this situation may not be ideal, and as such we have future plans to potentially address this process. 
<div align="center">
<img width="600" alt="image" src="https://github.com/user-attachments/assets/0fdfdec2-f831-47f1-a543-a765aee05dc4" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/f59fe326-32e6-4d6f-9bc9-51a3085f3ee9" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/988e8080-e854-4994-8cfb-0e5e19bb4c3f" />
<img width="600" alt="image" src="https://github.com/user-attachments/assets/d500565f-350c-4b20-9b13-71c2cdd91125" />
</div>

## Database Schema

The system uses a single `BOOKS` table to store all book information.  
Each record represents one book listed in the bookstore.

| Column Name | Type | Description |
|--------------|------|-------------|
| `isbn` | VARCHAR(20) **PK** | Unique identifier for each book |
| `title` | VARCHAR(255) | Title of the book |
| `author` | VARCHAR(255) | Name of the author |
| `publisher` | VARCHAR(255) | Publisher of the book |
| `description` | VARCHAR(255) | Description of the book |
| `genre` | VARCHAR(255) | Book genre (e.g., Fiction, Science, Biography) |
| `price` | DOUBLE PRECISION | Price of the book |
| `inventory` | INTEGER | Number of copies available |
| `image_url` | VARCHAR(255) | URL of the book cover image |

## UML Model
<div align="center">
<img width="400" alt="Screenshot 2025-11-16 155534" src="https://github.com/user-attachments/assets/d739992f-f687-4661-b211-f795b2e8953d" />
</div>

</details>

# The Team
[Grant Phillips](https://github.com/grantphillips13) \
[Joseph Andrews](https://github.com/jtkandrews) \
[Zachary Dredge](https://github.com/zdredge) \
[Tharsan Sivathansan](https://github.com/tharsan18) \
[Anas Ayoubi](https://github.com/AnasAyoubi)



# Amazin Bookstore
<div align="center">
 <img width="429" height="181" alt="ChatGPT_Image_Nov_3__2025_at_02_47_27_PM-removebg-preview-3" src="https://github.com/user-attachments/assets/f96af1b1-8203-4a8f-be63-80bea02dc3e6" />
</div>


SYSC4806 Group Project - JS Svelte

[![CI - Maven](https://github.com/jtkandrews/SYSC4806Project/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/jtkandrews/SYSC4806Project/actions/workflows/deploy.yml)

# Running the Application Locally
In the terminal, run the following: \
`cd frontend`

`npm run build` 

`cd ..` 

`rm -rf SvelteAmazin/src/main/resources/static` 

`mkdir -p SvelteAmazin/src/main/resources/static` 

`cp -r frontend/build/* SvelteAmazin/src/main/resources/static/`

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

## Plan for Sprint 3

## Current State of the Project

## Database Schema

## UML Model

# The Team
[Grant Phillips](https://github.com/grantphillips13) \
[Joseph Andrews](https://github.com/jtkandrews) \
[Zachary Dredge](https://github.com/zdredge) \
[Tharsan Sivathansan](https://github.com/tharsan18) \
[Anas Ayoubi](https://github.com/AnasAyoubi)



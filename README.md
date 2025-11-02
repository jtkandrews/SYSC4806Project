# Amazin Bookstore
SYSC4806 Group Project - JS Svelte

[![CI - Maven](https://github.com/jtkandrews/SYSC4806Project/actions/workflows/main_sysc4806project.yml/badge.svg?branch=main)](https://github.com/jtkandrews/SYSC4806Project/actions/workflows/main_sysc4806project.yml)

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



# Milestone 1
## Objectives Completed
- [x] Basic Backend with Books and their details
- [x] Svelte Frontend
- [x] Integrate with CI
- [x] Deployed on Azure
- [x] Setup Maven and Springboot Structure
- [x] Requirements outlined in [Wiki](https://github.com/jtkandrews/SYSC4806Project/wiki/Requirements)
- [x] Created Test Cases

 ## Current State of the Project
Currently, the project has the bare-bones elements configured. As seen here, the GitHub repo has been established with CI/CD integrations. Additionally, the Azure configuration has been complete, and the current version of the webapp can be seen here: https://sysc4806project.azurewebsites.net/. 

In terms of implementation, the backend is configured as a Spring Boot app which exposes a read-only GET endpoint to list current library of books. This information is displayed via Svelte on the frontend, which fetches the books and displays a stylized grid of said books. 

In regard to deployment, the frontend build is currently being copied into Spring Boot's static resources, from which a single JAR is created. We recognize this situation may not be ideal, and as such we have future plans to potentially address this process. 

 ## Database Schema
 


## The Team
[Grant Phillips](https://github.com/grantphillips13) \
[Joseph Andrews](https://github.com/jtkandrews) \
[Zachary Dredge](https://github.com/zdredge) \
[Tharsan Sivathansan](https://github.com/tharsan18) \
[Anas Ayoubi](https://github.com/AnasAyoubi)



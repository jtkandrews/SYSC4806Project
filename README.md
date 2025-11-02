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

 ## Database Schema
 


## The Team
[Grant Phillips](https://github.com/grantphillips13) \
[Joseph Andrews](https://github.com/jtkandrews) \
[Zachary Dredge](https://github.com/zdredge) \
[Tharsan Sivathansan](https://github.com/tharsan18) \
[Anas Ayoubi](https://github.com/AnasAyoubi)



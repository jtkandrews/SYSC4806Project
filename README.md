# SYSC4806Project
SYSC4806 Group Project - JS Svelte

## Running Locally

This project consists of a Spring Boot backend and a SvelteKit frontend.

### Prerequisites

- **Java 21** (check with `java -version`)
- **Maven** (check with `mvn -version`)
- **Node.js 18+** and **npm** (check with `node -version` and `npm -version`)

### Backend Setup (Spring Boot)

1. Navigate to the backend directory:
   ```bash
   cd SvelteAmazin
   ```

2. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or if you prefer using Maven directly:
   ```bash
   mvn spring-boot:run
   ```

3. The backend will start on `http://localhost:8080`
   - API endpoints are available at `http://localhost:8080/api/books`
   - The application uses an in-memory H2 database

### Frontend Setup (SvelteKit)

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. If `package.json` doesn't exist, initialize a SvelteKit project:
   ```bash
   npm create svelte@latest .
   ```
   - Choose: Skeleton project, Yes to TypeScript, Add Prettier (optional), Add ESLint (optional)

3. Install dependencies:
   ```bash
   npm install
   ```

4. Create a `vite.config.ts` file to proxy API requests to the backend:
   ```typescript
   import { sveltekit } from '@sveltejs/kit/vite';
   import { defineConfig } from 'vite';

   export default defineConfig({
     plugins: [sveltekit()],
     server: {
       proxy: {
         '/api': {
           target: 'http://localhost:8080',
           changeOrigin: true
         }
       }
     }
   });
   ```

5. Run the development server:
   ```bash
   npm run dev
   ```

6. The frontend will start on `http://localhost:5173` (or another port if 5173 is taken)

### Running Both Services

1. **Terminal 1** - Start the backend:
   ```bash
   cd SvelteAmazin
   ./mvnw spring-boot:run
   ```

2. **Terminal 2** - Start the frontend:
   ```bash
   cd frontend
   npm run dev
   ```

3. Open your browser to `http://localhost:5173` (or the port shown in the terminal)

### API Endpoints

The backend provides the following endpoints:
- `GET /api/books` - Get all books
- `GET /api/books/{isbn}` - Get a book by ISBN
- `GET /api/books/search?query={query}` - Search books

### Troubleshooting

- **Backend won't start**: Make sure Java 21 is installed and `JAVA_HOME` is set correctly
- **Frontend can't connect to backend**: Ensure the backend is running on port 8080 and the vite proxy is configured correctly
- **Port conflicts**: Change the Spring Boot port in `application.properties` by adding `server.port=8081`, or change the frontend port in `vite.config.ts`

## Building for Production

The project is configured to build both the frontend and backend together using Maven. This makes it easy to deploy to Azure or run as a single JAR file.

### Building the Complete Application

From the project root:

```bash
cd SvelteAmazin
mvn clean install
```

This will:
1. Install Node.js and npm (if needed)
2. Install frontend dependencies
3. Build the SvelteKit frontend
4. Copy the frontend build to Spring Boot's static resources
5. Package everything into a single JAR file

The JAR file will be created at `SvelteAmazin/target/SvelteAmazin-0.0.1-SNAPSHOT.jar`

### Running the Production Build

After building, you can run the complete application with:

```bash
cd SvelteAmazin
java -jar target/SvelteAmazin-0.0.1-SNAPSHOT.jar
```

The application will be available at `http://localhost:8080` with both the frontend and backend served from the same port.

## Azure Deployment

The project is configured for Azure deployment via GitHub Actions. When you push to the `main` branch:

1. The GitHub Actions workflow will automatically build both the frontend and backend
2. The complete JAR (including the frontend) will be deployed to Azure App Service
3. Both frontend and backend will be served from a single application

### What Changed for Azure

- **Frontend is built into static files** during the Maven build process
- **Spring Boot serves the frontend** from `/static` directory
- **Single JAR deployment** - no need to run separate servers
- **API routes work correctly** - `/api/*` routes go to controllers, everything else serves the frontend
- **Relative API paths** - the frontend uses relative URLs that work in production

### Local Development vs Production

- **Local Development**: Run Spring Boot (`mvn spring-boot:run`) and frontend (`npm run dev`) separately for hot reloading
- **Production/Azure**: Everything runs from a single JAR file, frontend is pre-built and served as static files

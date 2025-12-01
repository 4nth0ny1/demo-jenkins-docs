# Jenkins CI/CD Pipeline with Docker, GitHub, and Spring Boot
_Ongoing DevOps Project_

This repository demonstrates a simple but realistic CI/CD setup using:

- Jenkins running inside Docker
- A Spring Boot CRUD API backed by SQLite
- A Jenkins Declarative Pipeline that builds the app with Maven and pulls code from GitHub

It is an ongoing project focused on learning DevOps concepts that are directly useful in interviews and real teams.

---

## Project Overview

This repo includes:

- A minimal Spring Boot backend:
  - Java 17
  - Spring Boot 4.0
  - Spring Data JPA
  - SQLite database
- A Jenkinsfile that defines a Declarative Pipeline:
  - Checks out code from GitHub
  - Runs Maven inside a Docker container
- A Docker-based Jenkins setup (Blue Ocean image) that:
  - Runs Jenkins in a container
  - Mounts the Docker socket so Jenkins can run Docker containers
  - Uses a GitHub personal access token for authenticated API access

---

## Tech Stack

### Backend

- Java 17
- Spring Boot 4.0
- Spring Web
- Spring Data JPA
- SQLite JDBC driver

### DevOps / CI/CD

- Jenkins (Blue Ocean)
- Jenkins running inside Docker
- Multibranch Pipeline
- Docker Workflow plugin
- GitHub Branch Source plugin
- Maven build agent container

### Tools

- Docker Desktop
- Git
- Maven 3.9+
- GitHub

---

## Project Structure

```text
demo-jenkins-docs/
│
├── src/main/java/com/jenkinstutorial/demo/
│   ├── DemoApplication.java
│   ├── Product.java
│   ├── ProductRepository.java
│   ├── ProductService.java
│   ├── ProductServiceImpl.java
│   └── ProductController.java
│
├── src/main/resources/
│   ├── application.properties
│
├── Jenkinsfile
└── README.md
```

---

## How to Run the Spring Boot API Locally

### Prerequisites

- Git
- Java 21+
- Maven 3.9 or higher

### Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/4nth0ny1/demo-jenkins-docs.git
   cd demo-jenkins-docs
   ```

2. Build and run the application:

   ```bash
   mvn clean spring-boot:run
   ```

3. The API will start on:

   ```
   http://localhost:8080
   ```

4. Test the endpoint:

   ```bash
   curl http://localhost:8080/api/products
   ```

5. The SQLite database file (`demo.db`) will be created automatically.

---

## Running Jenkins in Docker (Full DevOps Setup)

### Prerequisites

- Docker Desktop
- GitHub PAT (Personal Access Token)
- GitHub account
- This repository cloned or forked

### 1. Create Jenkins volume

```bash
docker volume create jenkins_home
```

### 2. Start Jenkins container

```bash
docker run -d   --name jenkins-blueocean   --restart unless-stopped   -p 8081:8080   -p 50000:50000   -v jenkins_home:/var/jenkins_home   -v /var/run/docker.sock:/var/run/docker.sock   devopsjourney1/jenkins-blueocean:2.332.3-1
```

### 3. Get Jenkins admin password

```bash
docker exec -it jenkins-blueocean cat /var/jenkins_home/secrets/initialAdminPassword
```

### 4. Open Jenkins UI

```
http://localhost:8081
```

Unlock Jenkins → Install suggested plugins → Create admin user.

---

## Configuring GitHub Credentials in Jenkins

### 1. Create GitHub PAT

Visit:
```
https://github.com/settings/tokens
```

Scopes required:

- `public_repo` (for public repos)
- or `repo` (for private repos)

### 2. Add token to Jenkins

Navigate to:

```
Manage Jenkins → Manage Credentials → (global) → Add Credentials
```

Fill in:

- Kind: `Secret text`
- Secret: `<your GitHub token>`
- ID: `github-token`
- Description: GitHub PAT for Jenkins

### 3. Configure GitHub server

Navigate to:

```
Manage Jenkins → Configure System → GitHub
```

Add GitHub server:

- Name: `github`
- API URL: `https://api.github.com`
- Credentials: choose `github-token`
- Click: **Test Connection**

You should see:

```
GitHub API rate limit: 5000
```

---

## Creating the Multibranch Pipeline

1. Click **New Item**
2. Name: `demo-jenkins`
3. Select **Multibranch Pipeline**
4. Under "Branch Sources":
  - Add source → Git
  - Repo URL:

    ```
    https://github.com/4nth0ny1/demo-jenkins-docs.git
    ```

  - Credentials: select `github-token`
5. Save

Jenkins will:

- Scan the repo
- Detect the `Jenkinsfile`
- Create jobs automatically
- Trigger builds

---

## Jenkinsfile (Pipeline Summary)

```groovy
pipeline {
    agent {
        docker { image 'maven:3.9.11-eclipse-temurin-21-alpine' }
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn --version'
                sh 'mvn -B -DskipTests clean package'
            }
        }
    }
}
```

---

## How Anyone Can Reproduce the Entire Setup

1. Install Docker Desktop
2. Clone this repo:

   ```bash
   git clone https://github.com/4nth0ny1/demo-jenkins-docs.git
   ```

3. Start Jenkins with the Docker command above
4. Unlock Jenkins & install plugins
5. Add GitHub token
6. Configure GitHub server
7. Create Multibranch Pipeline
8. Jenkins auto-builds the project

Optional: run the Spring Boot app locally:

```bash
mvn spring-boot:run
```

---

## Roadmap / Future Work

- Add unit tests + pipeline test stage
- Add Dockerfile for backend
- Build & push Docker images in Jenkins
- Add deployment stage (Docker Compose or Kubernetes)
- Add GitHub webhooks for instant builds
- Add code-quality tools (SpotBugs, Checkstyle)
- Add Slack/email notifications

---

## Author

**Anthony Catullo**  
Backend Developer & DevOps
GitHub: https://github.com/4nth0ny1/demo-jenkins-docs

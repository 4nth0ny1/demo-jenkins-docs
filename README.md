# Jenkins Tutorial
C:\Users\Anthony\Downloads>java -jar jenkins.war --httpPort=8081 --enable-future-java


## First Commit Spring Boot, No Sqlite Yet
- setup spring boot with maven.
- java 17.
- currently have only getAllProducts. 

## Sqlite and Seeded Data
- getAllProducts is working and showing the seeded data 
- removed data.sql and schema.sql
```
data.sql
INSERT INTO product (name, price) VALUES
('Notebook', 4.99),
('Pencil', 0.99),
('Laptop', 899.00);
```

```
schema.sql
DROP TABLE IF EXISTS product;

CREATE TABLE product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price REAL NOT NULL
);
```
- changed in applicaiton properties
  - spring.jpa.hibernate.ddl-auto=none -->>> spring.jpa.hibernate.ddl-auto=update

## Add Docker with jenkins instructions 
Powershell
1. Stop and remove the old container
   docker rm -f jenkins-blueocean

2. (Optional but cleaner) Remove the old volume

Since its plugins were installed for the old Jenkins version:

docker volume rm jenkins_home
docker volume create jenkins_home

3. Start a modern Jenkins container

Use the official LTS image (has a recent enough core for the Pipeline plugins):

docker run -d --name jenkins `
  --restart unless-stopped `
-p 8081:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
-v /var/run/docker.sock:/var/run/docker.sock `
jenkins/jenkins:lts-jdk21


Still reachable at http://localhost:8081

Still has access to Docker via /var/run/docker.sock

Now has a newer Jenkins core that matches the Pipeline plugins.

4. Go through setup again

docker logs jenkins → copy the initialAdminPassword

Open http://localhost:8081

Unlock Jenkins with that password

Choose Install suggested plugins

This time:

Pipeline, Pipeline: API, Pipeline: Step API, etc. should install cleanly.

No “Jenkins 2.452.4 or higher required” error.

Once that’s done, we can:

Point it at your GitHub repo

Use your Docker-based Jenkinsfile with agent { docker { image 'maven:3.9.11-eclipse-temurin-21-alpine' } }

```

// Jenkinsfile (Declarative Pipeline)
// Requires the Docker Pipeline plugin
pipeline {
    agent { docker { image 'maven:3.9.11-eclipse-temurin-21-alpine' } }
    stages {
        stage('build') {
            steps {
                sh 'mvn --version'
            }
        }
    }
}

```

Get a clean green build for your Spring Boot + SQLite app.
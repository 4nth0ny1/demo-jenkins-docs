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
0. Clean slate (containers + volume)

In PowerShell:

docker rm -f jenkins jenkins-blueocean
docker volume rm jenkins_home
docker volume create jenkins_home


Errors like No such container are fine if one of them doesn’t exist.

1. Run Jenkins (LTS) in Docker, as root, with Docker socket

Use a single-line command to avoid backtick weirdness:

docker run -d --name jenkins --restart unless-stopped -p 8081:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -u root jenkins/jenkins:lts-jdk21


Jenkins UI will be at: http://localhost:8081

Inside the container, Jenkins runs as root so it can talk to Docker easily.

Check it’s running:

docker ps


You should see something like:

CONTAINER ID   IMAGE                     PORTS                    NAMES
xxxxxxx        jenkins/jenkins:lts-jdk21 0.0.0.0:8081->8080/tcp   jenkins

2. Install the Docker CLI inside the Jenkins container

Now we make docker actually work inside the Jenkins container.

Enter the container:

docker exec -it jenkins bash


Inside the container shell (Linux):

apt-get update
apt-get install -y docker.io


Quick sanity check (still inside the container):

docker ps


You should see at least the jenkins container listed. If that works, Jenkins can now use Docker.

Exit the shell:

exit

3. Unlock Jenkins and install plugins

Get the initial admin password:

docker logs jenkins


Look for:

Jenkins initial setup is required. An admin user has been created and a password generated.
Please use the following password to proceed to installation:

<big-random-token-here>


Copy that token.

Then:

Open http://localhost:8081

Paste the token to Unlock Jenkins

Choose Install suggested plugins

Create your admin user (this will be your Jenkins login)

After the wizard, go to:

Manage Jenkins → Manage Plugins → Available

Search and install:

Docker Pipeline (ID: docker-workflow)

Make sure Pipeline and Git plugins are also installed (they usually are from “suggested plugins”).

4. Jenkinsfile using Docker agent (the “real DevOps way”)

In your repo 4nth0ny1/demo-jenkins-docs, put this as your Jenkinsfile (at the root):

pipeline {
agent {
docker {
image 'maven:3.9.11-eclipse-temurin-21-alpine'
// optional: cache Maven repo between builds
args '-v $HOME/.m2:/root/.m2'
}
}

    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
    }
}


What this does:

Jenkins itself runs in the jenkins container.

For the pipeline, it spins up a separate Maven container:

image: maven:3.9.11-eclipse-temurin-21-alpine

mounts Maven cache for faster builds

Runs mvn clean package inside that Maven container.

This is the “real DevOps” pattern: Jenkins orchestrates Docker containers to do the work.

5. Run the pipeline

You already have a multibranch job pointed at https://github.com/4nth0ny1/demo-jenkins-docs.git.

Now:

Commit and push the Jenkinsfile change.

In Jenkins, open your multibranch job → branch master → Build Now (or it will auto-build on commit).

In the console log you want to see lines like:

docker inspect -f . maven:3.9.11-eclipse-temurin-21-alpine
docker pull maven:3.9.11-eclipse-temurin-21-alpine
...
[INFO] Building jar...


No more docker: not found. If you still see that, it means step 2 (apt-get install docker.io inside container) didn’t run or ran in the wrong container.
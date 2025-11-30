# Jenkins Tutorial

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


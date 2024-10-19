To make the project work, create a file `application.conf` in
the `networks-impl/src/main/resources` directory.

### **_networks-impl/src/main/resources/application.conf_**

```hocon
dbms {
  credentials {
    password = "neo4j"
    uri = "bolt://0.0.0.0:7687"
    username = "neo4j"
  }

  fabricName = "fabric"

  leftSplit {
    primaryDatabaseName = "db-11"
    secondaryDatabaseName = "db-12"
  }

  rightSplit {
    primaryDatabaseName = "db-21"
    secondaryDatabaseName = "db-22"
  }
}
```
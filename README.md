# How to use ZLayers in a ZIO 2.0 Application

This is the Scala project for the Hivemind Technologies blog article about ZIO Layers (_ZLayers_).
You can find the [article in this link](http://hivemindtechnologies.com).

## Architecture of the Application

```mermaid
%%{init: {'theme': 'neutral'}}%%
flowchart TB
subgraph appLayer["Application Layer"]
    direction LR
    app["Application"]
    appConfig["App Config"]
    appLogger["App Logger"]
end

subgraph serviceLayer["Service Layer"]
    propService["Property Service"]
    userService["User Service"]
end

subgraph repoLayer["Repository Layer"]
    userRepo["User Repository"]
    propRepo["Property Repository"]
end

subgraph dbLayer["Database Layer"]
    database["Database"]
    db[(Database)]
    dbConfig["Database Config"]
    dbLogger["DB Logger"]
end

app -."findUser()".-> userService -.getUserById().-> userRepo -."getObjectById()".-> database -."SQL query".-> db

app -."findProperty()".-> propService -.getPropertyById().-> propRepo -."getObjectById()".-> database

app -."findPropertiesOfUser()".-> propService -.getPropertiesByOwnerId().-> propRepo

database -.uses.-> dbLogger
database -.uses.-> dbConfig
```


## Provided SBT commands

### Compiling the project

```bash
sbt check
```

### Formatting all project files

```bash
sbt fmt
```

### Running the tests

```bash
sbt test
```

# A SpringAI / Redis RAG application demo

---

This is a demo Spring application that demonstrates how to use SpringAI, Redis as a vector store, 
and Ollama to create a chat application that uses information fed to it from PDF files, 
all running on a local machine.

## Software Pre-requisites
* Docker
* Docker Compose
* Java 17 
* Ollama

## Redis
The Redis instance is available via Docker Compose. 
To start Redis, execute `docker compose up -d` from the command line.
This will start the Redis stack. This configuration also includes the Redis dashboard, 
accessible at [http://localhost:8001](http://localhost:8001/).

## Chat data
The application writes PDF files to Redis and uses this information to pass information to the prompat. 
The documents will be stored in the `./src/main/resources/docs` folder. 
Since this folder is empty by default, add at least one document before starting the application. 
One example is the [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/6.0.0/reference/pdf/spring-framework.pdf).

## Running 
### Intellij
Import the project into your IDE and set a default configuration 

### Gradle
In the command line execute 
```bash
./gradlew bootRun
```

## Endpoint
The application exposes one endpoint `GET http://localhost/ollama/chat` and expects the parameter 
`message` to contain the text that will be send to the bot

## Configuration
In ./src/resources/application.properties, there are a few commented-out properties that can help tweak the bot responses. 
For more information, refer to the [documentation](https://docs.spring.io/spring-ai/reference/api/clients/ollama-chat.html). 
Please note that if you want to use different models, you'll have to install them first using ollama run modelname:version. 
Here is the list of models.











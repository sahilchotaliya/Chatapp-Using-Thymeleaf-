# Chat Application

This is a chat application built with Spring Boot and Thymeleaf. The application allows users to create chat rooms where they can share an image and a unique room code. Other users can join the room using the room code to communicate with each other.

## Features

- **Create Chat Room:** Users can create a new chat room by providing a room name, uploading an image, and generating a unique room code.
- **Join Chat Room:** Users can join an existing chat room by entering the room code.
- **Real-time Communication:** Users in the same chat room can communicate with each other in real-time.

## Tech Stack

- **Backend:** Spring Boot
- **Frontend:** Thymeleaf
- **Database:** (MySQL)
- **WebSocket:** For real-time communication

## Installation

1. Clone the repository:
   ```bash
   git clone git@github.com:sahilchotaliya/Chatapp-Using-Thymeleaf-.git
   ```
    ```bash
   cd chat-application
     ```

2.Build the project:

```bash
./mvnw clean install

```
Run the application:

bash
```bash
./mvnw spring-boot:run
 ```


Access the application at http://localhost:8080.



## 🚀 You have use Docker then run the following command.

To start the application using Docker Compose, follow these steps:



1**Run the following command:**

   ```bash
   docker compose build
```
Then 
   ```bash
   docker compose up 
```
Access the application at http://localhost:8085.
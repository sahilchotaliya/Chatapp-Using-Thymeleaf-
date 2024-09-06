# Chat Application

This is a chat application built with Spring Boot and Thymeleaf. The application allows users to create chat rooms where they can share an image and a unique room code. Other users can join the room using the room code to communicate with each other.

## Features

- **Create Chat Room:** Users can create a new chat room by providing a room name, uploading an image, and generating a unique room code.
- **Join Chat Room:** Users can join an existing chat room by entering the room code.
- **Real-time Communication:** Users in the same chat room can communicate with each other in real-time.
- **Image Sharing:** Room creators can share an image that will be visible to all participants in the chat room.
- **User Authentication:** Users must register and log in to create or join a chat room.

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
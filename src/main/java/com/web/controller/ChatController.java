    package com.web.controller;



    import com.web.model.ChatMessage;
    import com.web.model.ChatRoom;
    import com.web.model.User;
    import com.web.repository.ChatMessageRepository;
    import com.web.repository.ChatRoomRepository;
    import com.web.repository.UserRepository;
    import com.web.util.FileUploadUtil;
    import org.springframework.beans.factory.annotation.Autowired;

    import org.springframework.context.event.EventListener;
    import org.springframework.core.io.ResourceLoader;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.handler.annotation.DestinationVariable;
    import org.springframework.messaging.handler.annotation.MessageMapping;
    import org.springframework.messaging.handler.annotation.Payload;
    import org.springframework.messaging.handler.annotation.SendTo;
    import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.util.StringUtils;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import org.springframework.web.socket.messaging.SessionDisconnectEvent;

    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.UUID;

    @Controller
    public class ChatController {

        @Autowired
        private ChatRoomRepository chatRoomRepository;

        @Autowired
        private ChatMessageRepository chatMessageRepository;
        @Autowired
        private UserRepository userRepository;


        @Autowired
        private ResourceLoader resourceLoader;

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @GetMapping("/")
        public String home() {
            return "home";
        }

        @GetMapping("/create-room")
        public String showCreateRoomForm() {
            return "create-room";
        }



        @PostMapping("/create-room")
        public String createRoom(@RequestParam String name, @RequestParam String roomName, @RequestParam("profileImage") MultipartFile profileImage) throws IOException {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setName(roomName);

            String imageUrl = "/images/default-avatar.png";
            if (!profileImage.isEmpty()) {
                String fileName = StringUtils.cleanPath(profileImage.getOriginalFilename());
                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                String uploadDir = "static/images/";
                FileUploadUtil.saveFile(resourceLoader, uploadDir, uniqueFileName, profileImage);
                imageUrl = "/images/" + uniqueFileName;
            }

            chatRoom.setImageUrl(imageUrl);
            chatRoomRepository.save(chatRoom);

            User user = new User();
            user.setUsername(name);
            user.setProfileImageUrl(imageUrl);
            user.setOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            ChatMessage roomCreationMessage = new ChatMessage();
            roomCreationMessage.setType(ChatMessage.MessageType.SYSTEM);
            roomCreationMessage.setSender("System");
            roomCreationMessage.setContent(name + " created the room");
            roomCreationMessage.setChatRoom(chatRoom);
            roomCreationMessage.setTimestamp(LocalDateTime.now());
            chatMessageRepository.save(roomCreationMessage);

            return "redirect:/chat/" + chatRoom.getId() + "?user=" + name + "&isCreator=true";
        }
        @GetMapping("/join-room")
        public String showJoinRoomForm() {
            return "join-room";
        }

        @PostMapping("/join-room")
        @ResponseBody
        public ResponseEntity<?> joinRoom(@RequestParam String name, @RequestParam String roomCode) {
            ChatRoom chatRoom = chatRoomRepository.findByCode(roomCode)
                    .orElse(null);

            if (chatRoom == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Room not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            User user = userRepository.findByUsername(name)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(name);
                        newUser.setProfileImageUrl(chatRoom.getImageUrl());
                        return userRepository.save(newUser);
                    });

            Map<String, String> response = new HashMap<>();
            response.put("redirect", "/chat/" + chatRoom.getId() + "?user=" + name);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }



        @GetMapping("/chat/{roomId}")
        public String chatRoom(@PathVariable Long roomId, @RequestParam String user, @RequestParam(required = false) Boolean isCreator, Model model) {
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            User currentUser = userRepository.findByUsername(user)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(user);
                        newUser.setProfileImageUrl("/images/default-avatar.png");
                        newUser.setOnline(true);
                        newUser.setLastSeen(LocalDateTime.now());
                        return userRepository.save(newUser);
                    });
            model.addAttribute("room", chatRoom);
            model.addAttribute("messages", chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId));
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isCreator", isCreator != null && isCreator);
            return "chat-room";
        }

        @MessageMapping("/chat/{roomId}")
        @SendTo("/topic/messages/{roomId}")
        public ChatMessage sendMessage(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage) {
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            chatMessage.setChatRoom(chatRoom);
            chatMessage.setTimestamp(LocalDateTime.now());

            if (chatMessage.getType() == ChatMessage.MessageType.JOIN) {
                User user = userRepository.findByUsername(chatMessage.getSender())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                if (!user.isOnline()) {
                    user.setOnline(true);
                    user.setLastSeen(LocalDateTime.now());
                    userRepository.save(user);
                    return chatMessageRepository.save(chatMessage);
                }
                return null; // Return null if the user is already online (creator)
            } else if (chatMessage.getType() == ChatMessage.MessageType.LEAVE) {
                User user = userRepository.findByUsername(chatMessage.getSender())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setOnline(false);
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);
                return chatMessageRepository.save(chatMessage);
            }

            return chatMessageRepository.save(chatMessage);
        }
        @MessageMapping("/chat.addUser")
        @SendTo("/topic/public")
        public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
            String username = chatMessage.getSender();
            headerAccessor.getSessionAttributes().put("username", username);
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            user.setOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            return chatMessage;
        }

        @MessageMapping("/chat.sendMessage")
        @SendTo("/topic/public")
        public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
            return chatMessage;
        }

        @MessageMapping("/chat.typing/{roomId}")
        @SendTo("/topic/typing/{roomId}")
        public ChatMessage typingIndicator(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage) {
            chatMessage.setType(ChatMessage.MessageType.TYPING);
            return chatMessage;
        }

        @MessageMapping("/chat.stopTyping/{roomId}")
        @SendTo("/topic/typing/{roomId}")
        public ChatMessage stopTypingIndicator(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage) {
            chatMessage.setType(ChatMessage.MessageType.STOP_TYPING);
            return chatMessage;
        }

        @EventListener
        public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");
            if (username != null && roomId != null) {
                User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
                user.setOnline(false);
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.MessageType.LEAVE);
                chatMessage.setSender(username);
                chatMessage.setContent(username + " has left the room");
                chatMessage.setChatRoom(chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found")));
                chatMessage.setTimestamp(LocalDateTime.now());
                chatMessageRepository.save(chatMessage);

                messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessage);
            }
        }


    }
package com.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String profileImageUrl;

    private boolean online;
    private LocalDateTime lastSeen;
    @ManyToOne
    @JoinColumn(name = "current_room_id")
    private ChatRoom currentRoom;

    // Getters and setters
}

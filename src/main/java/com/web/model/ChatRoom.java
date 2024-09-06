package com.web.model;

import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;
    private String imageUrl;



    @PrePersist
    protected void onCreate() {
        this.code = UUID.randomUUID().toString().substring(0, 8);
    }


}

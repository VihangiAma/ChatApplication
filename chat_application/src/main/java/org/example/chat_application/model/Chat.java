/*package org.example.chat_application.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chat_id;

    private LocalDateTime started_at;
    private LocalDateTime ended_at;
    private String chat_file_path;

    public int getChat_id() {
        return chat_id;
    }

    public void setChat_id(int chat_id) {
        this.chat_id = chat_id;
    }

    public LocalDateTime getStarted_at() {
        return started_at;
    }

    public void setStarted_at(LocalDateTime started_at) {
        this.started_at = started_at;
    }

    public LocalDateTime getEnded_at() {
        return ended_at;
    }

    public void setEnded_at(LocalDateTime ended_at) {
        this.ended_at = ended_at;
    }

    public String getChat_file_path() {
        return chat_file_path;
    }

    public void setChat_file_path(String chat_file_path) {
        this.chat_file_path = chat_file_path;
    }

    // Getters & Setters
}*/
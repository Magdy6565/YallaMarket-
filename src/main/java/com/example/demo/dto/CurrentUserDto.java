package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CurrentUserDto {
    private Long id;
    private String username;
    private String email;
    private int role;
}

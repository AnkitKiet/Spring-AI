package com.example.springboot.helloworld.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    private String id;
    private String text;
    private List<Float> embedding;
}

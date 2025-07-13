package com.example.springboot.helloworld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.helloworld.service.NoteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping
    public ResponseEntity<String> saveNote(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        noteService.saveNote(text);
        return ResponseEntity.ok("Note saved successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> search(@RequestParam("q") String query) {
        System.out.println("Query received: " + query);
        List<String> matches = noteService.searchNotes(query);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> saveBulkNotes(@RequestBody Map<String, List<String>> body) {
        List<String> notes = body.get("notes");
        if (notes == null || notes.isEmpty()) {
            return ResponseEntity.badRequest().body("No notes provided");
        }

        noteService.saveBulkNotes(notes);
        return ResponseEntity.ok("Bulk notes saved successfully");
    }

}

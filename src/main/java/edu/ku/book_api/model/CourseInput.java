package edu.ku.book_api.model;

// Request model: what a client sends. No id, because the server creates it.
public record CourseInput(
        String code,
        String title,
        int credits
) {
}

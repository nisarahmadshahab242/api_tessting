package edu.ku.book_api.model;

// Response model: a course as the API returns it (converted to JSON).
public record Course(
        Long id,
        String code,
        String title,
        int credits
) {
}

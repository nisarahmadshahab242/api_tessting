package edu.ku.book_api.controller;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ku.book_api.model.Course;
import edu.ku.book_api.model.CourseInput;

@RestController
@RequestMapping("/api/v1/courses")
@CrossOrigin(origins = "http://localhost:5173")
public class CourseController {

    // In-memory, modifiable and thread-safe (List.of alone would be immutable).
    private final List<Course> courses = new CopyOnWriteArrayList<>(List.of(
            new Course(101L, "IS-401", "Enterprise Web Application Development", 3),
            new Course(102L, "IS-402", "Database Systems", 3),
            new Course(103L, "IS-403", "Software Project Management", 3)
    ));

    // The server creates ids: 104, 105, 106, ...
    private final AtomicLong nextId = new AtomicLong(104);

    // GET /api/v1/courses -> 200 + JSON array
    @GetMapping
    public List<Course> getCourses() {
        return courses;
    }

    // GET /api/v1/courses/{courseId} -> 200 + course, or 404 with an empty body
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long courseId) {
        return courses.stream()
                .filter(course -> course.id().equals(courseId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/v1/courses -> 201 + created course, 400 invalid, 409 duplicate code
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseInput input) {
        String problem = validate(input);
        if (problem != null) {
            return ResponseEntity.badRequest().body(problem);
        }

        String code = input.code().trim().toUpperCase();
        boolean duplicateCode = courses.stream()
                .anyMatch(course -> course.code().equalsIgnoreCase(code));
        if (duplicateCode) {
            return ResponseEntity.status(409).body("A course with this code already exists.");
        }

        Course newCourse = new Course(
                nextId.getAndIncrement(),
                code,
                input.title().trim(),
                input.credits()
        );
        courses.add(newCourse);

        URI location = URI.create("/api/v1/courses/" + newCourse.id());
        return ResponseEntity.created(location).body(newCourse);
    }

    // PUT /api/v1/courses/{courseId} -> 200 updated course, 400, 404 or 409
    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseInput input) {

        String problem = validate(input);
        if (problem != null) {
            return ResponseEntity.badRequest().body(problem);
        }

        String code = input.code().trim().toUpperCase();

        // The code may not belong to a DIFFERENT course.
        boolean codeTaken = courses.stream()
                .anyMatch(c -> !c.id().equals(courseId) && c.code().equalsIgnoreCase(code));
        if (codeTaken) {
            return ResponseEntity.status(409).body("A course with this code already exists.");
        }

        for (int index = 0; index < courses.size(); index++) {
            if (courses.get(index).id().equals(courseId)) {
                Course updated = new Course(courseId, code, input.title().trim(), input.credits());
                courses.set(index, updated); // records are immutable: replace, don't modify
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE /api/v1/courses/{courseId} -> 204, or 404 if it does not exist
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        boolean removed = courses.removeIf(course -> course.id().equals(courseId));
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // Returns an error message, or null when the input is valid.
    private String validate(CourseInput input) {
        if (input.code() == null || input.code().isBlank()
                || input.title() == null || input.title().isBlank()) {
            return "Course code and title are required.";
        }
        if (input.credits() <= 0) {
            return "Credits must be a positive number.";
        }
        return null;
    }
}

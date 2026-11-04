package edu.ku.book_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import edu.ku.book_api.model.Course;
import edu.ku.book_api.model.CourseInput;

// Plain unit tests: the controller methods are called directly,
// so no Spring context, web server or database is needed.
class CourseControllerTest {

    private CourseController controller;

    // A fresh controller (and a fresh in-memory list) for every test.
    @BeforeEach
    void setUp() {
        controller = new CourseController();
    }

    @Test
    @DisplayName("Test 01 - Find an existing course")
    void test01_findExistingCourse() {
        // Act
        ResponseEntity<Course> response = controller.getCourseById(101L);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        Course course = response.getBody();
        assertNotNull(course);
        assertEquals(101L, course.id());
        assertEquals("IS-401", course.code());
    }

    @Test
    @DisplayName("Test 02 - Look up a missing course")
    void test02_missingCourseReturnsNotFound() {
        // Act
        ResponseEntity<Course> response = controller.getCourseById(9999L);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test 03 - Create a valid course")
    void test03_createValidCourse() {
        // Arrange
        CourseInput input = new CourseInput("IS-404", "Software Testing", 3);

        // Act
        ResponseEntity<?> response = controller.createCourse(input);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        Course created = assertInstanceOf(Course.class, response.getBody());
        assertEquals("IS-404", created.code());
        assertEquals(3, created.credits());
    }

    @Test
    @DisplayName("Test 04 - Reject zero credits")
    void test04_rejectZeroCredits() {
        // Arrange
        CourseInput input = new CourseInput("IS-404", "Software Testing", 0);

        // Act
        ResponseEntity<?> response = controller.createCourse(input);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        String message = String.valueOf(response.getBody());
        assertTrue(message.toLowerCase().contains("positive"),
                "Message should explain that credits must be positive, but was: " + message);
    }

    @Test
    @DisplayName("Test 05 - Delete an existing course")
    void test05_deleteExistingCourse() {
        // Act
        ResponseEntity<Void> deleteResponse = controller.deleteCourse(103L);

        // Assert: deleted, and a later lookup no longer finds it
        assertEquals(204, deleteResponse.getStatusCode().value());
        assertEquals(404, controller.getCourseById(103L).getStatusCode().value());
    }
}

package com.codecrafthub.courseapi.service;

import com.codecrafthub.courseapi.model.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTest {

    private CourseService courseService;
    private Path backupFile;
    private Path coursesFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        coursesFile = Path.of("courses.json").toAbsolutePath();
        backupFile = tempDir.resolve("courses-backup.json");

        if (Files.exists(coursesFile)) {
            Files.copy(coursesFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.writeString(coursesFile, "[]");

        courseService = new CourseService();
        courseService.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(backupFile)) {
            Files.copy(backupFile, coursesFile, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(coursesFile);
        }
    }

    @Test
    void addCourseShouldAssignIdAndCreatedAt() {
        Course course = createSampleCourse("Java Basics", "Learn Java fundamentals.");

        Course saved = courseService.addCourse(course);

        assertNotNull(saved.getId(), "Saved course should have an ID");
        assertNotNull(saved.getCreatedAt(), "Saved course should have createdAt timestamp");
        assertEquals(1L, saved.getId());

        List<Course> allCourses = courseService.getAllCourses();
        assertEquals(1, allCourses.size());
        assertEquals("Java Basics", allCourses.get(0).getName());
    }

    @Test
    void getCourseByIdShouldReturnSavedCourse() {
        Course course = createSampleCourse("Spring Boot", "Build REST APIs with Spring Boot.");
        Course saved = courseService.addCourse(course);

        Course found = courseService.getCourseById(saved.getId());

        assertEquals(saved.getId(), found.getId());
        assertEquals(saved.getName(), found.getName());
    }

    @Test
    void updateCourseShouldChangeFields() {
        Course course = createSampleCourse("React", "Learn React components.");
        Course saved = courseService.addCourse(course);

        Course update = new Course();
        update.setName("React Advanced");
        update.setDescription("Build advanced React apps.");
        update.setTargetDate(LocalDate.of(2026, 12, 31));
        update.setStatus("In Progress");

        Course updated = courseService.updateCourse(saved.getId(), update);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("React Advanced", updated.getName());
        assertEquals("Build advanced React apps.", updated.getDescription());
        assertEquals(LocalDate.of(2026, 12, 31), updated.getTargetDate());
        assertEquals("In Progress", updated.getStatus());
    }

    @Test
    void deleteCourseShouldRemoveCourse() {
        Course first = courseService.addCourse(createSampleCourse("Docker", "Containerize apps."));
        Course second = courseService.addCourse(createSampleCourse("Kubernetes", "Orchestrate containers."));

        courseService.deleteCourse(first.getId());

        List<Course> allCourses = courseService.getAllCourses();
        assertEquals(1, allCourses.size());
        assertEquals(second.getId(), allCourses.get(0).getId());
    }

    @Test
    void addCourseShouldRejectInvalidStatus() {
        Course invalid = createSampleCourse("Bad Status", "This status is not allowed.");
        invalid.setStatus("Unknown Status");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> courseService.addCourse(invalid));

        assertTrue(exception.getMessage().contains("Invalid status"));
    }

    @Test
    void getCourseByIdShouldThrowWhenMissing() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> courseService.getCourseById(999L));

        assertTrue(exception.getMessage().contains("Course not found"));
    }

    private Course createSampleCourse(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setTargetDate(LocalDate.of(2026, 1, 1));
        course.setStatus("Not Started");
        return course;
    }
}

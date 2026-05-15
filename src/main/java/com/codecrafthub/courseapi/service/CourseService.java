package com.codecrafthub.courseapi.service;

import com.codecrafthub.courseapi.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseService {

    // JSON file name
    private static final String FILE_NAME = "courses.json";

    private final ObjectMapper objectMapper;

    public CourseService() {
        objectMapper = new ObjectMapper();

        // Support LocalDate and LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Write readable JSON
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Automatically create courses.json if it does not exist
     */
    @PostConstruct
    public void init() {
        File file = new File(FILE_NAME);

        try {
            if (!file.exists()) {

                // Create empty JSON array
                objectMapper.writeValue(file, new ArrayList<Course>());

                System.out.println("courses.json created successfully.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating courses.json file");
        }
    }

    /**
     * Read all courses from JSON file
     */
    private List<Course> readCourses() {

        try {
            File file = new File(FILE_NAME);

            return objectMapper.readValue(
                    file,
                    new TypeReference<List<Course>>() {
                    });

        } catch (IOException e) {
            throw new RuntimeException("Error reading courses file");
        }
    }

    /**
     * Save all courses into JSON file
     */
    private void writeCourses(List<Course> courses) {

        try {
            objectMapper.writeValue(new File(FILE_NAME), courses);

        } catch (IOException e) {
            throw new RuntimeException("Error writing to courses file");
        }
    }

    /**
     * Add new course
     */
    public Course addCourse(Course course) {

        validateStatus(course.getStatus());

        List<Course> courses = readCourses();

        // Generate ID starting from 1
        long nextId = courses.stream()
                .mapToLong(Course::getId)
                .max()
                .orElse(0) + 1;

        course.setId(nextId);

        // Set creation timestamp
        course.setCreatedAt(LocalDateTime.now());

        courses.add(course);

        writeCourses(courses);

        return course;
    }

    /**
     * Get all courses
     */
    public List<Course> getAllCourses() {
        return readCourses();
    }

    /**
     * Get course by ID
     */
    public Course getCourseById(Long id) {

        return readCourses().stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Course not found with ID: " + id));
    }

    /**
     * Update course
     */
    public Course updateCourse(Long id, Course updatedCourse) {

        validateStatus(updatedCourse.getStatus());

        List<Course> courses = readCourses();

        Optional<Course> existingCourseOptional = courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst();

        if (existingCourseOptional.isEmpty()) {
            throw new RuntimeException("Course not found with ID: " + id);
        }

        Course existingCourse = existingCourseOptional.get();

        existingCourse.setName(updatedCourse.getName());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setTargetDate(updatedCourse.getTargetDate());
        existingCourse.setStatus(updatedCourse.getStatus());

        writeCourses(courses);

        return existingCourse;
    }

    /**
     * Delete course
     */
    public void deleteCourse(Long id) {

        List<Course> courses = readCourses();

        boolean removed = courses.removeIf(course ->
                course.getId().equals(id));

        if (!removed) {
            throw new RuntimeException("Course not found with ID: " + id);
        }

        writeCourses(courses);
    }

    /**
     * Validate allowed status values
     */
    private void validateStatus(String status) {

        List<String> validStatuses = List.of(
                "Not Started",
                "In Progress",
                "Completed"
        );

        if (!validStatuses.contains(status)) {
            throw new RuntimeException(
                    "Invalid status. Allowed values are: " +
                            "Not Started, In Progress, Completed"
            );
        }
    }

        /**
     * Partially update course fields
     */
    public Course patchCourse(Long id, Map<String, Object> updates) {

        List<Course> courses = readCourses();

        Course existingCourse = courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found with ID: " + id));

        // Update name if provided
        if (updates.containsKey("name")) {

            String name = updates.get("name").toString();

            if (name.isBlank()) {
                throw new RuntimeException(
                        "Course name cannot be blank");
            }

            existingCourse.setName(name);
        }

        // Update description if provided
        if (updates.containsKey("description")) {

            String description =
                    updates.get("description").toString();

            if (description.isBlank()) {
                throw new RuntimeException(
                        "Course description cannot be blank");
            }

            existingCourse.setDescription(description);
        }

        // Update target_date if provided
        if (updates.containsKey("target_date")) {

            try {

                existingCourse.setTargetDate(
                        java.time.LocalDate.parse(
                                updates.get("target_date")
                                        .toString()));

            } catch (Exception e) {

                throw new RuntimeException(
                        "Invalid target_date format. Use YYYY-MM-DD");
            }
        }

        // Update status if provided
        if (updates.containsKey("status")) {

            String status = updates.get("status").toString();

            validateStatus(status);

            existingCourse.setStatus(status);
        }

        // Save updated courses
        writeCourses(courses);

        return existingCourse;
    }
}

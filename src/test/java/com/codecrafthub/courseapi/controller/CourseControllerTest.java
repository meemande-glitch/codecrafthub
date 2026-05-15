package com.codecrafthub.courseapi.controller;

import com.codecrafthub.courseapi.model.Course;
import com.codecrafthub.courseapi.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CourseControllerTest {

    @Test
    void addCourseShouldReturnCreatedCourse() {
        Course request = createCourse("Java Basics", "Learn Java fundamentals.", LocalDate.of(2026, 1, 1), "Not Started");
        Course saved = createCourse("Java Basics", "Learn Java fundamentals.", LocalDate.of(2026, 1, 1), "Not Started");
        saved.setId(1L);
        saved.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        CourseController controller = new CourseController(new TestCourseService() {
            @Override
            public Course addCourse(Course course) {
                assertEquals(request.getName(), course.getName());
                return saved;
            }
        });

        ResponseEntity<Course> response = controller.addCourse(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(saved, response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getAllCoursesShouldReturnCourseList() {
        Course course = createCourse("Spring Boot", "Build REST APIs.", LocalDate.of(2026, 2, 1), "In Progress");
        course.setId(2L);
        course.setCreatedAt(LocalDateTime.of(2026, 2, 2, 12, 0));

        CourseController controller = new CourseController(new TestCourseService() {
            @Override
            public List<Course> getAllCourses() {
                return List.of(course);
            }
        });

        ResponseEntity<?> response = controller.getAllCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        @SuppressWarnings("unchecked")
        List<Course> body = (List<Course>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(2L, body.get(0).getId());
    }

    @Test
    void getCourseByIdShouldReturnCourse() {
        Course course = createCourse("React", "Learn React components.", LocalDate.of(2026, 3, 1), "Completed");
        course.setId(3L);

        CourseController controller = new CourseController(new TestCourseService() {
            @Override
            public Course getCourseById(Long id) {
                assertEquals(3L, id);
                return course;
            }
        });

        ResponseEntity<?> response = controller.getCourseById(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(course, response.getBody());
    }

    @Test
    void updateCourseShouldReturnUpdatedCourse() {
        Course update = createCourse("React Advanced", "Build advanced apps.", LocalDate.of(2026, 12, 31), "In Progress");
        update.setId(4L);

        CourseController controller = new CourseController(new TestCourseService() {
            @Override
            public Course updateCourse(Long id, Course updatedCourse) {
                assertEquals(4L, id);
                assertEquals("React Advanced", updatedCourse.getName());
                return update;
            }
        });

        ResponseEntity<?> response = controller.updateCourse(4L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(update, response.getBody());
    }

    @Test
    void deleteCourseShouldReturnSuccessMessage() {
        CourseController controller = new CourseController(new TestCourseService() {
            @Override
            public void deleteCourse(Long id) {
                assertEquals(5L, id);
            }
        });

        ResponseEntity<?> response = controller.deleteCourse(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Course deleted successfully", body.get("message"));
    }

    @Test
    void patchCourseShouldReturnPatchedCourse() {
        Course updated = createCourse("Docker", "Containerize apps.", LocalDate.of(2026, 5, 1), "In Progress");
        updated.setId(6L);

        CourseController controller = new CourseController(new TestCourseService() {
            @Override
            public Course patchCourse(Long id, Map<String, Object> updates) {
                assertEquals(6L, id);
                assertEquals("In Progress", updates.get("status"));
                return updated;
            }
        });

        ResponseEntity<?> response = controller.patchCourse(6L, Map.of("status", "In Progress"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
    }

    @Test
    void handleRuntimeExceptionShouldReturnNotFoundResponse() {
        CourseController controller = new CourseController(new TestCourseService());
        RuntimeException exception = new RuntimeException("Course not found with ID: 999");

        ResponseEntity<?> response = controller.handleRuntimeException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Course not found with ID: 999", body.get("error"));
    }

    @Test
    void handleValidationExceptionShouldReturnFieldErrors() throws NoSuchMethodException {
        CourseController controller = new CourseController(new TestCourseService());
        Course target = new Course();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "course");
        bindingResult.addError(new FieldError("course", "name", "Course name is required"));

        Method method = CourseController.class.getMethod("addCourse", Course.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<?> response = controller.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Course name is required", body.get("name"));
    }

    private Course createCourse(String name, String description, LocalDate targetDate, String status) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setTargetDate(targetDate);
        course.setStatus(status);
        return course;
    }

    private static class TestCourseService extends CourseService {
        @Override
        public Course addCourse(Course course) {
            throw new UnsupportedOperationException("Not implemented in stub");
        }

        @Override
        public List<Course> getAllCourses() {
            throw new UnsupportedOperationException("Not implemented in stub");
        }

        @Override
        public Course getCourseById(Long id) {
            throw new UnsupportedOperationException("Not implemented in stub");
        }

        @Override
        public Course updateCourse(Long id, Course updatedCourse) {
            throw new UnsupportedOperationException("Not implemented in stub");
        }

        @Override
        public void deleteCourse(Long id) {
            throw new UnsupportedOperationException("Not implemented in stub");
        }

        @Override
        public Course patchCourse(Long id, Map<String, Object> updates) {
            throw new UnsupportedOperationException("Not implemented in stub");
        }
    }
}

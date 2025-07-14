# Blog Backend Test Plan

## Overview
This test plan outlines the test cases required for the blog backend application. The application appears to be a social media platform with features such as posts, comments, friend relationships, and file management.

## Unit Tests

### RandomUtil Tests
- Test generateTimeBasedRandomLong - verify unique ID generation
- Test generateRandomString - verify length and character content
- Test generateRandomInt - verify length and numeric content
- Test getMD5 - verify consistent hashing 
- Test generateMessageId - verify format and uniqueness

### Entity Tests
- Test Post entity - getters, setters, constructors
- Test Account entity - getters, setters, constructors
- Test Relationship entity - getters, setters, constructors
- Test Trend entity - getters, setters, constructors

### Controller Tests
- Test PostController
  - Test uploadPost - verify successful upload and proper response
  - Test getPostByPostId - verify retrieval of correct post
  - Test getPostsByUserId - verify retrieval of all posts for a user
  - Test getFriendsPosts - verify retrieval of friends' posts
  - Test getPostsByTimeStamp - verify retrieval based on time range
  - Test searchLocation - verify location search functionality
  - Test fetchPictures - verify picture retrieval

### Service Tests
- Test PostService
  - Test uploadPost - verify proper data saving
  - Test getPostByPostID - verify retrieval of specific post
  - Test getPostsByUserID - verify retrieval and pagination
  - Test getFriendsPosts - verify friend posts retrieval
  - Test getPostsByTimestamp - verify time-based retrieval
  - Test getTrends - verify trending posts retrieval
  - Test deletePost - verify post deletion

### Mapper Tests
- Test PostParser
  - Test userDetailParser - verify proper mapping from ResultSet to Post objects

## Integration Tests

### Database Integration
- Test ScyllaDB connectivity
- Test database operations (CRUD for posts)
- Test query performance with large datasets

### Redis Integration
- Test Redis connectivity
- Test trend storage and retrieval
- Test caching mechanisms

### External Service Integration
- Test MapboxSearchUtil - verify location search functionality

## End-to-End Tests
- Complete user journey test:
  - Create account
  - Add friends
  - Create post with image
  - View post
  - Comment on post
  - Search for posts

## Implementation Approach
Tests will be implemented using:
- JUnit 5 for unit testing
- Mockito for mocking dependencies
- TestContainers for database integration tests
- MockMVC for controller tests

## Priority Order
1. Unit tests for core utilities (RandomUtil)
2. Entity tests
3. Service layer tests with mocked dependencies
4. Controller tests
5. Integration tests
6. End-to-end tests 
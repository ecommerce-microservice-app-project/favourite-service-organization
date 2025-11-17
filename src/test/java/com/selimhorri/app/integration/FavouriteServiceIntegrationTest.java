package com.selimhorri.app.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.FavouriteRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas de Integración para FavouriteService
 * Estas pruebas usan la base de datos real (H2 en memoria)
 * y prueban la integración completa entre capas
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Favourite Service Integration Tests")
class FavouriteServiceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FavouriteRepository favouriteRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RestTemplate restTemplate;

	private LocalDateTime testLikeDate;
	private DateTimeFormatter formatter;

	@BeforeEach
	void setUp() {
		// Clean database before each test
		favouriteRepository.deleteAll();

		testLikeDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
		formatter = DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT);

		// Mock RestTemplate responses for external service calls
		UserDto mockUserDto = UserDto.builder()
				.userId(1)
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@example.com")
				.build();

		ProductDto mockProductDto = ProductDto.builder()
				.productId(100)
				.productTitle("Test Product")
				.priceUnit(99.99)
				.build();

		when(restTemplate.getForObject(any(String.class), eq(UserDto.class)))
				.thenReturn(mockUserDto);
		when(restTemplate.getForObject(any(String.class), eq(ProductDto.class)))
				.thenReturn(mockProductDto);
	}

	@Test
	@DisplayName("Should create favourite successfully via REST API")
	void testCreateFavourite_Success() throws Exception {
		// Given
		FavouriteDto favouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.build();

		// When & Then
		mockMvc.perform(post("/api/favourites")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(favouriteDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.productId").value(100))
				.andExpect(jsonPath("$.likeDate").exists());

		// Verify it was saved in database
		assertTrue(favouriteRepository.count() > 0);
	}

	@Test
	@DisplayName("Should retrieve favourite by id via REST API")
	void testGetFavouriteById_Success() throws Exception {
		// Given
		Favourite savedFavourite = createFavouriteInDatabase();
		String likeDateStr = savedFavourite.getLikeDate().format(formatter);

		// When & Then
		mockMvc.perform(get("/api/favourites/{userId}/{productId}/{likeDate}",
				savedFavourite.getUserId(),
				savedFavourite.getProductId(),
				likeDateStr))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(savedFavourite.getUserId()))
				.andExpect(jsonPath("$.productId").value(savedFavourite.getProductId()))
				.andExpect(jsonPath("$.user").exists())
				.andExpect(jsonPath("$.product").exists());
	}

	@Test
	@DisplayName("Should retrieve all favourites via REST API")
	void testGetAllFavourites_Success() throws Exception {
		// Given - Create favourites with different composite keys
		Favourite favourite1 = Favourite.builder()
				.userId(1)
				.productId(100)
				.likeDate(LocalDateTime.now().withNano(0))
				.build();
		favouriteRepository.save(favourite1);

		// Wait a bit to ensure different timestamp
		Thread.sleep(100);

		Favourite favourite2 = Favourite.builder()
				.userId(2)
				.productId(200)
				.likeDate(LocalDateTime.now().withNano(0))
				.build();
		favouriteRepository.save(favourite2);

		// When & Then
		mockMvc.perform(get("/api/favourites"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection").isArray())
				.andExpect(jsonPath("$.collection.length()").value(2));
	}

	@Test
	@DisplayName("Should update favourite successfully via REST API")
	void testUpdateFavourite_Success() throws Exception {
		// Given
		Favourite savedFavourite = createFavouriteInDatabase();
		LocalDateTime updatedDate = LocalDateTime.of(2024, 2, 20, 15, 45, 0);

		FavouriteDto updatedFavouriteDto = FavouriteDto.builder()
				.userId(savedFavourite.getUserId())
				.productId(savedFavourite.getProductId())
				.likeDate(updatedDate)
				.build();

		// When & Then
		mockMvc.perform(put("/api/favourites")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedFavouriteDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(savedFavourite.getUserId()))
				.andExpect(jsonPath("$.productId").value(savedFavourite.getProductId()));
	}

	@Test
	@DisplayName("Should delete favourite successfully via REST API")
	void testDeleteFavourite_Success() throws Exception {
		// Given
		Favourite savedFavourite = createFavouriteInDatabase();
		String likeDateStr = savedFavourite.getLikeDate().format(formatter);

		// When & Then
		mockMvc.perform(delete("/api/favourites/{userId}/{productId}/{likeDate}",
				savedFavourite.getUserId(),
				savedFavourite.getProductId(),
				likeDateStr))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

		// Verify it was deleted from database
		FavouriteId favouriteId = new FavouriteId(
				savedFavourite.getUserId(),
				savedFavourite.getProductId(),
				savedFavourite.getLikeDate());
		assertTrue(favouriteRepository.findById(favouriteId).isEmpty());
	}

	@Test
	@DisplayName("Should return 400 error when favourite not found")
	void testGetFavouriteById_NotFound() throws Exception {
		// Given
		String likeDateStr = testLikeDate.format(formatter);

		// When & Then
		mockMvc.perform(get("/api/favourites/{userId}/{productId}/{likeDate}",
				999, 999, likeDateStr))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should persist favourite with correct composite key")
	void testFavouriteCompositeKey() throws Exception {
		// Given
		FavouriteDto favouriteDto = FavouriteDto.builder()
				.userId(5)
				.productId(500)
				.likeDate(testLikeDate)
				.build();

		// When
		String response = mockMvc.perform(post("/api/favourites")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(favouriteDto)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		FavouriteDto result = objectMapper.readValue(response, FavouriteDto.class);

		// Then - Verify composite key in database
		FavouriteId favouriteId = new FavouriteId(
				result.getUserId(),
				result.getProductId(),
				result.getLikeDate());
		Favourite dbFavourite = favouriteRepository.findById(favouriteId).orElseThrow();
		assertNotNull(dbFavourite);
		assertEquals(5, dbFavourite.getUserId());
		assertEquals(500, dbFavourite.getProductId());
		assertEquals(testLikeDate, dbFavourite.getLikeDate());
	}

	@Test
	@DisplayName("Should retrieve favourites with user and product information")
	void testGetFavouritesWithUserAndProduct() throws Exception {
		// Given
		createFavouriteInDatabase();

		// When & Then
		mockMvc.perform(get("/api/favourites"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection[0].user").exists())
				.andExpect(jsonPath("$.collection[0].user.userId").exists())
				.andExpect(jsonPath("$.collection[0].product").exists())
				.andExpect(jsonPath("$.collection[0].product.productId").exists());
	}

	@Test
	@DisplayName("Should handle multiple favourites for same user")
	void testMultipleFavouritesForSameUser() throws Exception {
		// Given
		Favourite favourite1 = Favourite.builder()
				.userId(1)
				.productId(100)
				.likeDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
				.build();

		Favourite favourite2 = Favourite.builder()
				.userId(1)
				.productId(200)
				.likeDate(LocalDateTime.of(2024, 1, 16, 11, 0, 0))
				.build();

		favouriteRepository.save(favourite1);
		favouriteRepository.save(favourite2);

		// When & Then
		mockMvc.perform(get("/api/favourites"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection.length()").value(2))
				.andExpect(jsonPath("$.collection[0].userId").value(1))
				.andExpect(jsonPath("$.collection[1].userId").value(1));
	}

	/**
	 * Helper method to create a favourite in the database
	 */
	private Favourite createFavouriteInDatabase() {
		// Use current time to ensure uniqueness
		LocalDateTime uniqueDate = LocalDateTime.now().withNano(0);

		Favourite favourite = Favourite.builder()
				.userId(1)
				.productId(100)
				.likeDate(uniqueDate)
				.build();

		return favouriteRepository.save(favourite);
	}

}

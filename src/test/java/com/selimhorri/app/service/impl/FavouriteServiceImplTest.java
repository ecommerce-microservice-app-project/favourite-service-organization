package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavouriteServiceImpl Unit Tests")
class FavouriteServiceImplTest {
	
	@Mock
	private FavouriteRepository favouriteRepository;
	
	@Mock
	private RestTemplate restTemplate;
	
	@InjectMocks
	private FavouriteServiceImpl favouriteService;
	
	private Favourite testFavourite;
	private FavouriteDto testFavouriteDto;
	private FavouriteId testFavouriteId;
	private UserDto testUserDto;
	private ProductDto testProductDto;
	private LocalDateTime testLikeDate;
	
	@BeforeEach
	void setUp() {
		testLikeDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
		
		testFavouriteId = new FavouriteId(1, 100, testLikeDate);
		
		testUserDto = UserDto.builder()
				.userId(1)
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@example.com")
				.build();
		
		testProductDto = ProductDto.builder()
				.productId(100)
				.productTitle("Test Product")
				.priceUnit(99.99)
				.build();
		
		testFavourite = Favourite.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.build();
		
		testFavouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.userDto(testUserDto)
				.productDto(testProductDto)
				.build();
	}
	
	@Test
	@DisplayName("Should find all favourites successfully")
	void testFindAll_Success() {
		// Given
		List<Favourite> favourites = Arrays.asList(testFavourite);
		when(favouriteRepository.findAll()).thenReturn(favourites);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"),
				eq(UserDto.class)))
				.thenReturn(testUserDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class)))
				.thenReturn(testProductDto);
		
		// When
		List<FavouriteDto> result = favouriteService.findAll();
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getUserId());
		assertEquals(100, result.get(0).getProductId());
		assertNotNull(result.get(0).getUserDto());
		assertNotNull(result.get(0).getProductDto());
		verify(favouriteRepository, times(1)).findAll();
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"),
				eq(UserDto.class));
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class));
	}
	
	@Test
	@DisplayName("Should return empty list when no favourites exist")
	void testFindAll_EmptyList() {
		// Given
		when(favouriteRepository.findAll()).thenReturn(Collections.emptyList());
		
		// When
		List<FavouriteDto> result = favouriteService.findAll();
		
		// Then
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(favouriteRepository, times(1)).findAll();
		verify(restTemplate, never()).getForObject(any(String.class), any(Class.class));
	}
	
	@Test
	@DisplayName("Should find favourite by id successfully")
	void testFindById_Success() {
		// Given
		when(favouriteRepository.findById(testFavouriteId)).thenReturn(Optional.of(testFavourite));
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"),
				eq(UserDto.class)))
				.thenReturn(testUserDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class)))
				.thenReturn(testProductDto);
		
		// When
		FavouriteDto result = favouriteService.findById(testFavouriteId);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getUserId());
		assertEquals(100, result.getProductId());
		assertEquals(testLikeDate, result.getLikeDate());
		assertNotNull(result.getUserDto());
		assertNotNull(result.getProductDto());
		verify(favouriteRepository, times(1)).findById(testFavouriteId);
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"),
				eq(UserDto.class));
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class));
	}
	
	@Test
	@DisplayName("Should throw FavouriteNotFoundException when favourite not found")
	void testFindById_NotFound() {
		// Given
		FavouriteId nonExistentId = new FavouriteId(999, 999, testLikeDate);
		when(favouriteRepository.findById(nonExistentId)).thenReturn(Optional.empty());
		
		// When & Then
		FavouriteNotFoundException exception = assertThrows(
				FavouriteNotFoundException.class,
				() -> favouriteService.findById(nonExistentId)
		);
		
		assertTrue(exception.getMessage().contains("Favourite with id: [" + nonExistentId + "] not found!"));
		verify(favouriteRepository, times(1)).findById(nonExistentId);
		verify(restTemplate, never()).getForObject(any(String.class), any(Class.class));
	}
	
	@Test
	@DisplayName("Should save favourite successfully")
	void testSave_Success() {
		// Given
		FavouriteDto newFavouriteDto = FavouriteDto.builder()
				.userId(2)
				.productId(200)
				.likeDate(testLikeDate)
				.build();
		
		Favourite savedFavourite = Favourite.builder()
				.userId(2)
				.productId(200)
				.likeDate(testLikeDate)
				.build();
		
		when(favouriteRepository.save(any(Favourite.class))).thenReturn(savedFavourite);
		
		// When
		FavouriteDto result = favouriteService.save(newFavouriteDto);
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.getUserId());
		assertEquals(200, result.getProductId());
		assertEquals(testLikeDate, result.getLikeDate());
		verify(favouriteRepository, times(1)).save(any(Favourite.class));
	}
	
	@Test
	@DisplayName("Should update favourite successfully")
	void testUpdate_Success() {
		// Given
		FavouriteDto updatedFavouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.build();
		
		Favourite updatedFavourite = Favourite.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.build();
		
		when(favouriteRepository.save(any(Favourite.class))).thenReturn(updatedFavourite);
		
		// When
		FavouriteDto result = favouriteService.update(updatedFavouriteDto);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getUserId());
		assertEquals(100, result.getProductId());
		verify(favouriteRepository, times(1)).save(any(Favourite.class));
	}
	
	@Test
	@DisplayName("Should delete favourite by id successfully")
	void testDeleteById_Success() {
		// Given
		// No need to mock anything for delete
		
		// When
		favouriteService.deleteById(testFavouriteId);
		
		// Then
		verify(favouriteRepository, times(1)).deleteById(testFavouriteId);
	}
	
	@Test
	@DisplayName("Should handle multiple favourites and return distinct list")
	void testFindAll_MultipleFavourites() {
		// Given
		Favourite favourite2 = Favourite.builder()
				.userId(2)
				.productId(200)
				.likeDate(testLikeDate)
				.build();
		
		List<Favourite> favourites = Arrays.asList(testFavourite, favourite2);
		when(favouriteRepository.findAll()).thenReturn(favourites);
		
		UserDto userDto2 = UserDto.builder()
				.userId(2)
				.firstName("Jane")
				.lastName("Smith")
				.build();
		
		ProductDto productDto2 = ProductDto.builder()
				.productId(200)
				.productTitle("Another Product")
				.build();
		
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"),
				eq(UserDto.class)))
				.thenReturn(testUserDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/2"),
				eq(UserDto.class)))
				.thenReturn(userDto2);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class)))
				.thenReturn(testProductDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/200"),
				eq(ProductDto.class)))
				.thenReturn(productDto2);
		
		// When
		List<FavouriteDto> result = favouriteService.findAll();
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getUserId());
		assertEquals(2, result.get(1).getUserId());
		verify(favouriteRepository, times(1)).findAll();
	}
	
}


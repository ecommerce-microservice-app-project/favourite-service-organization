package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.dto.FavouriteDto;

@DisplayName("FavouriteMappingHelper Unit Tests")
class FavouriteMappingHelperTest {
	
	private Favourite testFavourite;
	private FavouriteDto testFavouriteDto;
	private LocalDateTime testLikeDate;
	
	@BeforeEach
	void setUp() {
		testLikeDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
		
		testFavourite = Favourite.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.build();
		
		testFavouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(100)
				.likeDate(testLikeDate)
				.build();
	}
	
	@Test
	@DisplayName("Should map Favourite to FavouriteDto successfully")
	void testMapFavouriteToDto_Success() {
		// When
		FavouriteDto result = FavouriteMappingHelper.map(testFavourite);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getUserId());
		assertEquals(100, result.getProductId());
		assertEquals(testLikeDate, result.getLikeDate());
		assertNotNull(result.getUserDto());
		assertEquals(1, result.getUserDto().getUserId());
		assertNotNull(result.getProductDto());
		assertEquals(100, result.getProductDto().getProductId());
	}
	
	@Test
	@DisplayName("Should map FavouriteDto to Favourite successfully")
	void testMapDtoToFavourite_Success() {
		// When
		Favourite result = FavouriteMappingHelper.map(testFavouriteDto);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getUserId());
		assertEquals(100, result.getProductId());
		assertEquals(testLikeDate, result.getLikeDate());
	}
	
	@Test
	@DisplayName("Should maintain bidirectional mapping consistency")
	void testBidirectionalMapping_Consistency() {
		// When - Favourite to DTO and back
		FavouriteDto mappedDto = FavouriteMappingHelper.map(testFavourite);
		Favourite mappedBackFavourite = FavouriteMappingHelper.map(mappedDto);
		
		// Then
		assertEquals(testFavourite.getUserId(), mappedBackFavourite.getUserId());
		assertEquals(testFavourite.getProductId(), mappedBackFavourite.getProductId());
		assertEquals(testFavourite.getLikeDate(), mappedBackFavourite.getLikeDate());
	}
	
	@Test
	@DisplayName("Should map Favourite with different date correctly")
	void testMapFavouriteWithDifferentDate() {
		// Given
		LocalDateTime differentDate = LocalDateTime.of(2024, 12, 25, 15, 45, 30);
		Favourite favouriteWithDifferentDate = Favourite.builder()
				.userId(5)
				.productId(500)
				.likeDate(differentDate)
				.build();
		
		// When
		FavouriteDto result = FavouriteMappingHelper.map(favouriteWithDifferentDate);
		
		// Then
		assertNotNull(result);
		assertEquals(5, result.getUserId());
		assertEquals(500, result.getProductId());
		assertEquals(differentDate, result.getLikeDate());
	}
	
	@Test
	@DisplayName("Should map FavouriteDto with different date correctly")
	void testMapDtoWithDifferentDate() {
		// Given
		LocalDateTime differentDate = LocalDateTime.of(2024, 6, 1, 8, 0, 0);
		FavouriteDto favouriteDtoWithDifferentDate = FavouriteDto.builder()
				.userId(10)
				.productId(1000)
				.likeDate(differentDate)
				.build();
		
		// When
		Favourite result = FavouriteMappingHelper.map(favouriteDtoWithDifferentDate);
		
		// Then
		assertNotNull(result);
		assertEquals(10, result.getUserId());
		assertEquals(1000, result.getProductId());
		assertEquals(differentDate, result.getLikeDate());
	}
	
}


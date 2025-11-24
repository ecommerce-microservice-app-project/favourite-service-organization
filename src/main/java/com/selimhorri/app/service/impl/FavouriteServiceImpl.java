package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.helper.FavouriteMappingHelper;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.FavouriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FavouriteServiceImpl implements FavouriteService {

	private final FavouriteRepository favouriteRepository;
	private final RestTemplate restTemplate;

	@Override
	public List<FavouriteDto> findAll() {
		log.info("*** FavouriteDto List, service; fetch all favourites *");
		return this.favouriteRepository.findAll()
				.stream()
				.map(FavouriteMappingHelper::map)
				.map(f -> {
					try {
						f.setUserDto(this.restTemplate
								.getForObject(
										AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + f.getUserId(),
										UserDto.class));
					} catch (Exception e) {
						log.error("Error fetching user {}: {}", f.getUserId(), e.getMessage());
						// Continuar sin userDto si falla
					}
					try {
						f.setProductDto(this.restTemplate
								.getForObject(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/"
										+ f.getProductId(), ProductDto.class));
					} catch (Exception e) {
						log.error("Error fetching product {}: {}", f.getProductId(), e.getMessage());
						// Continuar sin productDto si falla
					}
					return f;
				})
				.distinct()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public FavouriteDto findById(final FavouriteId favouriteId) {
		log.info("*** FavouriteDto, service; fetch favourite by id *");
		return this.favouriteRepository.findById(favouriteId)
				.map(FavouriteMappingHelper::map)
				.map(f -> {
					try {
						f.setUserDto(this.restTemplate
								.getForObject(
										AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + f.getUserId(),
										UserDto.class));
					} catch (Exception e) {
						log.error("Error fetching user {}: {}", f.getUserId(), e.getMessage());
						// Lanzar excepción para que el handler la capture
						throw new RuntimeException("Failed to fetch user data: " + e.getMessage(), e);
					}
					try {
						f.setProductDto(this.restTemplate
								.getForObject(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/"
										+ f.getProductId(), ProductDto.class));
					} catch (Exception e) {
						log.error("Error fetching product {}: {}", f.getProductId(), e.getMessage());
						// Lanzar excepción para que el handler la capture
						throw new RuntimeException("Failed to fetch product data: " + e.getMessage(), e);
					}
					return f;
				})
				.orElseThrow(() -> new FavouriteNotFoundException(
						String.format("Favourite with id: [%s] not found!", favouriteId)));
	}

	@Override
	public FavouriteDto save(final FavouriteDto favouriteDto) {
		return FavouriteMappingHelper.map(this.favouriteRepository
				.save(FavouriteMappingHelper.map(favouriteDto)));
	}

	@Override
	public FavouriteDto update(final FavouriteDto favouriteDto) {
		return FavouriteMappingHelper.map(this.favouriteRepository
				.save(FavouriteMappingHelper.map(favouriteDto)));
	}

	@Override
	public void deleteById(final FavouriteId favouriteId) {
		this.favouriteRepository.deleteById(favouriteId);
	}

}

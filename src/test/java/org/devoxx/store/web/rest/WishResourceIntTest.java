package org.devoxx.store.web.rest;

import org.devoxx.store.StoreApp;

import org.devoxx.store.domain.Wish;
import org.devoxx.store.repository.WishRepository;
import org.devoxx.store.service.WishService;
import org.devoxx.store.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the WishResource REST controller.
 *
 * @see WishResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StoreApp.class)
public class WishResourceIntTest {

    private static final Long DEFAULT_PRODUCT_ID = 1L;
    private static final Long UPDATED_PRODUCT_ID = 2L;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(1);

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private WishService wishService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restWishMockMvc;

    private Wish wish;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        WishResource wishResource = new WishResource(wishService);
        this.restWishMockMvc = MockMvcBuilders.standaloneSetup(wishResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Wish createEntity(EntityManager em) {
        Wish wish = new Wish()
                .productId(DEFAULT_PRODUCT_ID)
                .price(DEFAULT_PRICE);
        return wish;
    }

    @Before
    public void initTest() {
        wish = createEntity(em);
    }

    @Test
    @Transactional
    public void createWish() throws Exception {
        int databaseSizeBeforeCreate = wishRepository.findAll().size();

        // Create the Wish

        restWishMockMvc.perform(post("/api/wishes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wish)))
            .andExpect(status().isCreated());

        // Validate the Wish in the database
        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeCreate + 1);
        Wish testWish = wishList.get(wishList.size() - 1);
        assertThat(testWish.getProductId()).isEqualTo(DEFAULT_PRODUCT_ID);
        assertThat(testWish.getPrice()).isEqualTo(DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void createWishWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = wishRepository.findAll().size();

        // Create the Wish with an existing ID
        Wish existingWish = new Wish();
        existingWish.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restWishMockMvc.perform(post("/api/wishes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingWish)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkProductIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = wishRepository.findAll().size();
        // set the field null
        wish.setProductId(null);

        // Create the Wish, which fails.

        restWishMockMvc.perform(post("/api/wishes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wish)))
            .andExpect(status().isBadRequest());

        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = wishRepository.findAll().size();
        // set the field null
        wish.setPrice(null);

        // Create the Wish, which fails.

        restWishMockMvc.perform(post("/api/wishes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wish)))
            .andExpect(status().isBadRequest());

        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllWishes() throws Exception {
        // Initialize the database
        wishRepository.saveAndFlush(wish);

        // Get all the wishList
        restWishMockMvc.perform(get("/api/wishes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wish.getId().intValue())))
            .andExpect(jsonPath("$.[*].productId").value(hasItem(DEFAULT_PRODUCT_ID.intValue())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())));
    }

    @Test
    @Transactional
    public void getWish() throws Exception {
        // Initialize the database
        wishRepository.saveAndFlush(wish);

        // Get the wish
        restWishMockMvc.perform(get("/api/wishes/{id}", wish.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(wish.getId().intValue()))
            .andExpect(jsonPath("$.productId").value(DEFAULT_PRODUCT_ID.intValue()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingWish() throws Exception {
        // Get the wish
        restWishMockMvc.perform(get("/api/wishes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateWish() throws Exception {
        // Initialize the database
        wishService.save(wish);

        int databaseSizeBeforeUpdate = wishRepository.findAll().size();

        // Update the wish
        Wish updatedWish = wishRepository.findOne(wish.getId());
        updatedWish
                .productId(UPDATED_PRODUCT_ID)
                .price(UPDATED_PRICE);

        restWishMockMvc.perform(put("/api/wishes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedWish)))
            .andExpect(status().isOk());

        // Validate the Wish in the database
        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeUpdate);
        Wish testWish = wishList.get(wishList.size() - 1);
        assertThat(testWish.getProductId()).isEqualTo(UPDATED_PRODUCT_ID);
        assertThat(testWish.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void updateNonExistingWish() throws Exception {
        int databaseSizeBeforeUpdate = wishRepository.findAll().size();

        // Create the Wish

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restWishMockMvc.perform(put("/api/wishes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(wish)))
            .andExpect(status().isCreated());

        // Validate the Wish in the database
        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteWish() throws Exception {
        // Initialize the database
        wishService.save(wish);

        int databaseSizeBeforeDelete = wishRepository.findAll().size();

        // Get the wish
        restWishMockMvc.perform(delete("/api/wishes/{id}", wish.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Wish> wishList = wishRepository.findAll();
        assertThat(wishList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Wish.class);
    }
}

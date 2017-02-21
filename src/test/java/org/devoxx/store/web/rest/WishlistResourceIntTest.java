package org.devoxx.store.web.rest;

import org.devoxx.store.StoreApp;

import org.devoxx.store.domain.Wishlist;
import org.devoxx.store.repository.WishlistRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the WishlistResource REST controller.
 *
 * @see WishlistResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StoreApp.class)
public class WishlistResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";

    private static final LocalDate DEFAULT_CREATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_HIDDEN = false;
    private static final Boolean UPDATED_HIDDEN = true;

    @Inject
    private WishlistRepository wishlistRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restWishlistMockMvc;

    private Wishlist wishlist;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        WishlistResource wishlistResource = new WishlistResource();
        ReflectionTestUtils.setField(wishlistResource, "wishlistRepository", wishlistRepository);
        this.restWishlistMockMvc = MockMvcBuilders.standaloneSetup(wishlistResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Wishlist createEntity(EntityManager em) {
        Wishlist wishlist = new Wishlist()
                .name(DEFAULT_NAME)
                .creationDate(DEFAULT_CREATION_DATE)
                .hidden(DEFAULT_HIDDEN);
        return wishlist;
    }

    @Before
    public void initTest() {
        wishlist = createEntity(em);
    }

    @Test
    @Transactional
    public void createWishlist() throws Exception {
        int databaseSizeBeforeCreate = wishlistRepository.findAll().size();

        // Create the Wishlist

        restWishlistMockMvc.perform(post("/api/wishlists")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(wishlist)))
                .andExpect(status().isCreated());

        // Validate the Wishlist in the database
        List<Wishlist> wishlists = wishlistRepository.findAll();
        assertThat(wishlists).hasSize(databaseSizeBeforeCreate + 1);
        Wishlist testWishlist = wishlists.get(wishlists.size() - 1);
        assertThat(testWishlist.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testWishlist.getCreationDate()).isEqualTo(DEFAULT_CREATION_DATE);
        assertThat(testWishlist.isHidden()).isEqualTo(DEFAULT_HIDDEN);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = wishlistRepository.findAll().size();
        // set the field null
        wishlist.setName(null);

        // Create the Wishlist, which fails.

        restWishlistMockMvc.perform(post("/api/wishlists")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(wishlist)))
                .andExpect(status().isBadRequest());

        List<Wishlist> wishlists = wishlistRepository.findAll();
        assertThat(wishlists).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllWishlists() throws Exception {
        // Initialize the database
        wishlistRepository.saveAndFlush(wishlist);

        // Get all the wishlists
        restWishlistMockMvc.perform(get("/api/wishlists?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(wishlist.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].creationDate").value(hasItem(DEFAULT_CREATION_DATE.toString())))
                .andExpect(jsonPath("$.[*].hidden").value(hasItem(DEFAULT_HIDDEN.booleanValue())));
    }

    @Test
    @Transactional
    public void getWishlist() throws Exception {
        // Initialize the database
        wishlistRepository.saveAndFlush(wishlist);

        // Get the wishlist
        restWishlistMockMvc.perform(get("/api/wishlists/{id}", wishlist.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(wishlist.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.creationDate").value(DEFAULT_CREATION_DATE.toString()))
            .andExpect(jsonPath("$.hidden").value(DEFAULT_HIDDEN.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingWishlist() throws Exception {
        // Get the wishlist
        restWishlistMockMvc.perform(get("/api/wishlists/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateWishlist() throws Exception {
        // Initialize the database
        wishlistRepository.saveAndFlush(wishlist);
        int databaseSizeBeforeUpdate = wishlistRepository.findAll().size();

        // Update the wishlist
        Wishlist updatedWishlist = wishlistRepository.findOne(wishlist.getId());
        updatedWishlist
                .name(UPDATED_NAME)
                .creationDate(UPDATED_CREATION_DATE)
                .hidden(UPDATED_HIDDEN);

        restWishlistMockMvc.perform(put("/api/wishlists")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedWishlist)))
                .andExpect(status().isOk());

        // Validate the Wishlist in the database
        List<Wishlist> wishlists = wishlistRepository.findAll();
        assertThat(wishlists).hasSize(databaseSizeBeforeUpdate);
        Wishlist testWishlist = wishlists.get(wishlists.size() - 1);
        assertThat(testWishlist.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testWishlist.getCreationDate()).isEqualTo(UPDATED_CREATION_DATE);
        assertThat(testWishlist.isHidden()).isEqualTo(UPDATED_HIDDEN);
    }

    @Test
    @Transactional
    public void deleteWishlist() throws Exception {
        // Initialize the database
        wishlistRepository.saveAndFlush(wishlist);
        int databaseSizeBeforeDelete = wishlistRepository.findAll().size();

        // Get the wishlist
        restWishlistMockMvc.perform(delete("/api/wishlists/{id}", wishlist.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Wishlist> wishlists = wishlistRepository.findAll();
        assertThat(wishlists).hasSize(databaseSizeBeforeDelete - 1);
    }
}

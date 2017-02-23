package org.devoxx.store.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A Wish.
 */
@Entity
@Table(name = "wish")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Wish implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "price", precision=10, scale=2, nullable = false)
    private BigDecimal price;

    @ManyToOne
    private Wishlist wishlist;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public Wish productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Wish price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public Wish wishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
        return this;
    }

    public void setWishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Wish wish = (Wish) o;
        if(wish.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, wish.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Wish{" +
            "id=" + id +
            ", productId='" + productId + "'" +
            ", price='" + price + "'" +
            '}';
    }
}

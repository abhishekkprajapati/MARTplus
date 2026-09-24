/* =========================================================
   SHOPSPHERE — PRODUCTS JAVASCRIPT
   ========================================================= */


/* =========================================================
   PRODUCT DATA
   Loaded from backend API with rich defaults fallback
   ========================================================= */

const defaultProducts = [

    {
        id: 1,
        name: "Apple iPhone 15",
        category: "Electronics",
        categorySlug: "electronics",
        price: 69999,
        oldPrice: 79999,
        discount: 13,
        rating: 4.8,
        reviews: 1248,
        badge: "BESTSELLER",
        emoji: "📱",
        stock: 25,
        seller: "TechWorld",
        description:
            "Experience powerful performance, an advanced camera system and a beautiful display with the Apple iPhone 15."
    },

    {
        id: 2,
        name: "Sony WH-1000XM5 Headphones",
        category: "Electronics",
        categorySlug: "electronics",
        price: 24999,
        oldPrice: 29999,
        discount: 17,
        rating: 4.7,
        reviews: 864,
        badge: "TOP RATED",
        emoji: "🎧",
        stock: 18,
        seller: "AudioHub",
        description:
            "Premium wireless headphones with industry-leading noise cancellation and immersive sound."
    },

    {
        id: 3,
        name: "Nike Air Max Running Shoes",
        category: "Fashion",
        categorySlug: "fashion",
        price: 8499,
        oldPrice: 11999,
        discount: 29,
        rating: 4.6,
        reviews: 532,
        badge: "HOT",
        emoji: "👟",
        stock: 42,
        seller: "SportZone",
        description:
            "Lightweight and comfortable running shoes designed for everyday performance and active lifestyles."
    },

    {
        id: 4,
        name: "Premium Smart Watch Series 5",
        category: "Electronics",
        categorySlug: "electronics",
        price: 4999,
        oldPrice: 7999,
        discount: 38,
        rating: 4.5,
        reviews: 723,
        badge: "DEAL",
        emoji: "⌚",
        stock: 31,
        seller: "SmartTech",
        description:
            "Track your fitness, receive notifications and stay connected with this feature-packed smart watch."
    },

    {
        id: 5,
        name: "Minimalist Leather Backpack",
        category: "Fashion",
        categorySlug: "fashion",
        price: 2199,
        oldPrice: 3499,
        discount: 37,
        rating: 4.7,
        reviews: 316,
        badge: "TRENDING",
        emoji: "🎒",
        stock: 56,
        seller: "UrbanCarry",
        description:
            "A stylish and durable backpack designed for work, college and everyday travel."
    },

    {
        id: 6,
        name: "Modern Ceramic Table Lamp",
        category: "Home & Living",
        categorySlug: "home",
        price: 1599,
        oldPrice: 2499,
        discount: 36,
        rating: 4.4,
        reviews: 189,
        badge: "NEW",
        emoji: "💡",
        stock: 20,
        seller: "HomeDecor",
        description:
            "Add warmth and modern elegance to your room with this beautifully designed ceramic table lamp."
    },

    {
        id: 7,
        name: "Premium Skincare Gift Set",
        category: "Beauty",
        categorySlug: "beauty",
        price: 1299,
        oldPrice: 1999,
        discount: 35,
        rating: 4.6,
        reviews: 421,
        badge: "POPULAR",
        emoji: "🧴",
        stock: 64,
        seller: "GlowStore",
        description:
            "A complete skincare gift set featuring carefully selected everyday essentials."
    },

    {
        id: 8,
        name: "Professional Fitness Dumbbell Set",
        category: "Sports",
        categorySlug: "sports",
        price: 3499,
        oldPrice: 4999,
        discount: 30,
        rating: 4.5,
        reviews: 267,
        badge: "DEAL",
        emoji: "🏋️",
        stock: 15,
        seller: "FitGear",
        description:
            "Adjustable dumbbells for strength training at home or in the gym."
    },

    {
        id: 9,
        name: "Mechanical RGB Gaming Keyboard",
        category: "Electronics",
        categorySlug: "electronics",
        price: 3299,
        oldPrice: 4999,
        discount: 34,
        rating: 4.7,
        reviews: 954,
        badge: "GAMING",
        emoji: "⌨️",
        stock: 37,
        seller: "GameZone",
        description:
            "Responsive mechanical switches, customizable RGB lighting and a durable gaming-focused design."
    },

    {
        id: 10,
        name: "Wireless Gaming Mouse",
        category: "Electronics",
        categorySlug: "electronics",
        price: 1799,
        oldPrice: 2499,
        discount: 28,
        rating: 4.6,
        reviews: 645,
        badge: "POPULAR",
        emoji: "🖱️",
        stock: 48,
        seller: "GameZone",
        description:
            "Fast, precise and comfortable wireless gaming mouse with a responsive sensor."
    },

    {
        id: 11,
        name: "Classic Oversized Hoodie",
        category: "Fashion",
        categorySlug: "fashion",
        price: 1499,
        oldPrice: 2299,
        discount: 35,
        rating: 4.5,
        reviews: 378,
        badge: "TRENDING",
        emoji: "🧥",
        stock: 72,
        seller: "StyleStreet",
        description:
            "Comfortable oversized hoodie made for casual everyday styling."
    },

    {
        id: 12,
        name: "Premium Coffee Maker",
        category: "Home & Living",
        categorySlug: "home",
        price: 4299,
        oldPrice: 5999,
        discount: 28,
        rating: 4.6,
        reviews: 241,
        badge: "TOP PICK",
        emoji: "☕",
        stock: 12,
        seller: "KitchenPro",
        description:
            "Brew rich and flavorful coffee at home with this compact premium coffee maker."
    }

];

let products = [...defaultProducts];

/* =========================================================
   MAP BACKEND PRODUCT TO FRONTEND MODEL
   ========================================================= */

function mapBackendProduct(p) {
    if (!p) return null;
    const catSlug = (p.category || "general").toLowerCase().replace(/[^a-z0-9]/g, "-");
    let emoji = "📦";
    const n = (p.name || "").toLowerCase();
    const c = (p.category || "").toLowerCase();
    if (n.includes("phone") || n.includes("mobile") || n.includes("apple")) emoji = "📱";
    else if (n.includes("headphone") || n.includes("audio") || n.includes("speaker") || n.includes("sound")) emoji = "🎧";
    else if (n.includes("watch")) emoji = "⌚";
    else if (n.includes("mouse") || n.includes("keyboard") || n.includes("laptop")) emoji = "🖱️";
    else if (n.includes("shoe") || n.includes("sneaker") || c.includes("fashion")) emoji = "👟";
    else if (n.includes("bag") || n.includes("backpack")) emoji = "🎒";
    else if (n.includes("lamp") || n.includes("light") || c.includes("home")) emoji = "💡";

    return {
        id: p.id,
        name: p.name,
        category: p.category || "General",
        categorySlug: catSlug,
        price: Number(p.price) || 0,
        oldPrice: Math.round((Number(p.price) || 0) * 1.2),
        discount: 15,
        rating: 4.8,
        reviews: 120,
        badge: (p.stock <= 5 && p.stock > 0) ? "LOW STOCK" : (p.stock === 0 ? "OUT OF STOCK" : "POPULAR"),
        emoji: emoji,
        imageUrl: p.imageUrl || "",
        image: p.imageUrl || "",
        images: p.imageUrl ? [p.imageUrl] : [],
        stock: p.stock !== undefined ? p.stock : 10,
        seller: p.seller?.fullName || p.seller?.email || p.sellerName || p.sellerEmail || "MARTplus Seller",
        description: p.description || ""
    };
}

/* =========================================================
   LOAD PRODUCTS FROM BACKEND API
   ========================================================= */

async function loadProductsFromBackend() {
    try {
        const api = window.ShopSphereApi;
        if (!api) return;

        const backendProducts = await api.products.getAll();
        if (Array.isArray(backendProducts)) {
            const mapped = backendProducts.map(mapBackendProduct).filter(Boolean);

            const backendIds = new Set(mapped.map(m => m.id));
            products = [...mapped, ...defaultProducts.filter(d => !backendIds.has(d.id))];

            if (window.ShopSphereProducts) {
                window.ShopSphereProducts.products = products;
            }

            renderHomepageProducts();
            syncWishlistButtons();

            // Dispatch global event so other pages/scripts immediately get new products
            window.dispatchEvent(new CustomEvent("shopsphere:productsLoaded", { detail: products }));

            // Notify page renderers if present
            const productsGrid = document.getElementById("productsGrid");
            if (productsGrid) {
                renderProducts(productsGrid, products);
                const countEl = document.getElementById("resultsCount");
                if (countEl) countEl.textContent = products.length;
            }

            return products;
        }
    } catch (e) {
        console.warn("Products API unavailable, using default catalog:", e);
    }
    return products;
}


/* =========================================================
   FORMAT PRICE
   ========================================================= */

function formatPrice(price) {

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 0
        }
    ).format(price);
}


/* =========================================================
   CREATE STAR RATING
   ========================================================= */

function createStars(rating) {

    const roundedRating =
        Math.round(rating);


    let stars = "";


    for (
        let i = 1;
        i <= 5;
        i++
    ) {

        stars +=
            i <= roundedRating
                ? "★"
                : "☆";
    }


    return stars;
}


/* =========================================================
   CREATE PRODUCT CARD
   ========================================================= */

function createProductCard(product) {

    const card =
        document.createElement("article");

    card.className = "product-card";
    card.dataset.productId = product.id;
    card.setAttribute("data-product-id", product.id);
    card.style.cursor = "pointer";


    const hasImage = Boolean(product.imageUrl || product.image);
    const imgSrc = product.imageUrl || product.image || "";
    const imageHtml = hasImage
        ? `<img src="${imgSrc}" alt="${product.name || 'Product'}" class="product-card-img" style="width:100%;height:100%;object-fit:cover;display:block;" onerror="this.style.display='none';if(this.nextElementSibling)this.nextElementSibling.style.display='flex';">
           <span class="product-placeholder" style="display:none;">${product.emoji || '📦'}</span>`
        : `<span class="product-placeholder">${product.emoji || '📦'}</span>`;

    card.innerHTML = `

        <div class="product-image">

            ${imageHtml}

            <span class="product-badge">
                ${product.badge}
            </span>

            <button
                class="product-wishlist"
                type="button"
                data-product-id="${product.id}"
                aria-label="Add ${product.name} to wishlist"
            >
                ♡
            </button>

        </div>


        <div class="product-info">

            <span class="product-category">
                ${product.category}
            </span>


            <h3 class="product-name">
                ${product.name}
            </h3>


            <div class="product-rating">

                <span class="stars">
                    ${createStars(product.rating)}
                </span>

                <span class="rating-count">
                    ${product.rating}
                    (${product.reviews})
                </span>

            </div>


            <div class="product-price-row">

                <strong class="product-price">
                    ${formatPrice(product.price)}
                </strong>

                <span class="product-old-price">
                    ${formatPrice(product.oldPrice)}
                </span>

                <span class="product-discount">
                    ${product.discount}% OFF
                </span>

            </div>


            <button
                class="add-to-cart-btn"
                type="button"
                data-product-id="${product.id}"
            >
                Add to Cart
            </button>

        </div>
    `;


    return card;
}


/* =========================================================
   RENDER PRODUCTS
   ========================================================= */

function renderProducts(
    container,
    productList
) {

    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (!productList.length) {

        container.innerHTML = `

            <div
                class="empty-state"
                style="grid-column: 1 / -1;"
            >

                <h3>
                    No products found
                </h3>

                <p>
                    Try another search or category.
                </p>

            </div>

        `;

        return;
    }


    productList.forEach(product => {

        container.appendChild(
            createProductCard(product)
        );

    });
}


/* =========================================================
   GET PRODUCT BY ID
   ========================================================= */

function getProductById(id) {

    return products.find(
        product =>
            Number(product.id) === Number(id)
    );
}


/* =========================================================
   ADD TO CART
   ========================================================= */

async function addToCart(productId) {

    let product =
        getProductById(productId);

    if (!product && window.ShopSphereApi?.products?.getById) {
        try {
            const p = await window.ShopSphereApi.products.getById(productId);
            if (p && p.id) {
                product = mapBackendProduct(p);
                products.push(product);
            }
        } catch (e) {
            console.warn("addToCart backend fetch notice:", e);
        }
    }

    if (!product) {
        return;
    }

    const cart =
        window.ShopSphere?.getStorageData(
            "shopsphere_cart",
            []
        ) || [];

    const existingItem =
        cart.find(
            item =>
                item.productId === product.id
        );

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            emoji: product.emoji,
            seller: product.seller,
            quantity: 1
        });
    }

    window.ShopSphere?.saveStorageData(
        "shopsphere_cart",
        cart
    );

    window.ShopSphere?.updateCartCount();

    // Sync with backend buyer cart if authenticated
    if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
        try {
            const api = window.ShopSphereApi;
            if (api) {
                await api.buyer.addToCart(product.id, 1);
            }
        } catch (err) {
            console.warn("Backend cart sync notice:", err.message);
        }
    }

    window.ShopSphere?.showToast(
        "Added to Cart 🛒",
        `${product.name} has been added to your cart.`
    );
}


/* =========================================================
   ADD TO WISHLIST
   ========================================================= */

async function toggleWishlist(productId, button) {

    let product =
        getProductById(productId);

    if (!product && window.ShopSphereApi?.products?.getById) {
        try {
            const p = await window.ShopSphereApi.products.getById(productId);
            if (p && p.id) {
                product = mapBackendProduct(p);
                products.push(product);
            }
        } catch (e) {
            console.warn("toggleWishlist backend fetch notice:", e);
        }
    }

    if (!product) {
        return;
    }

    const wishlist =
        window.ShopSphere?.getStorageData(
            "shopsphere_wishlist",
            []
        ) || [];

    const existingIndex =
        wishlist.findIndex(
            item =>
                item.productId === product.id
        );

    if (existingIndex !== -1) {
        wishlist.splice(
            existingIndex,
            1
        );

        button?.classList.remove(
            "active"
        );

        if (button) {
            button.textContent = "♡";
        }

        window.ShopSphere?.saveStorageData(
            "shopsphere_wishlist",
            wishlist
        );

        window.ShopSphere?.updateWishlistCount?.();

        if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
            try {
                const api = window.ShopSphereApi;
                if (api) {
                    await api.buyer.removeWishlistItem(product.id);
                }
            } catch (e) {
                console.warn("Backend wishlist sync notice:", e.message);
            }
        }

        window.ShopSphere?.showToast(
            "Wishlist Updated",
            `${product.name} was removed from your wishlist.`
        );

    } else {
        wishlist.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            emoji: product.emoji,
            seller: product.seller
        });

        button?.classList.add(
            "active"
        );

        if (button) {
            button.textContent = "♥";
        }

        window.ShopSphere?.saveStorageData(
            "shopsphere_wishlist",
            wishlist
        );

        window.ShopSphere?.updateWishlistCount?.();

        if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
            try {
                const api = window.ShopSphereApi;
                if (api) {
                    await api.buyer.addToWishlist(product.id);
                }
            } catch (e) {
                console.warn("Backend wishlist sync notice:", e.message);
            }
        }

        window.ShopSphere?.showToast(
            "Added to Wishlist ❤️",
            `${product.name} was added to your wishlist.`
        );
    }


    window.ShopSphere?.saveStorageData(
        "shopsphere_wishlist",
        wishlist
    );


    window.ShopSphere?.updateWishlistCount();
}


/* =========================================================
   PRODUCT CARD EVENTS
   ========================================================= */

document.addEventListener(
    "click",
    function (event) {

        const cartButton =
            event.target.closest(
                ".add-to-cart-btn"
            );


        if (cartButton) {

            const productId =
                Number(
                    cartButton.dataset.productId
                );


            addToCart(productId);

            return;
        }


        const wishlistButton =
            event.target.closest(
                ".product-wishlist"
            );


        if (wishlistButton) {

            const productId =
                Number(
                    wishlistButton.dataset.productId
                );


            toggleWishlist(
                productId,
                wishlistButton
            );

            return;
        }

    }
);


/* =========================================================
   SYNC WISHLIST BUTTONS
   ========================================================= */

function syncWishlistButtons() {

    const wishlist =
        window.ShopSphere?.getStorageData(
            "shopsphere_wishlist",
            []
        ) || [];


    const wishlistIds =
        wishlist.map(
            item =>
                Number(item.productId)
        );


    document
        .querySelectorAll(
            ".product-wishlist"
        )
        .forEach(button => {

            const productId =
                Number(
                    button.dataset.productId
                );


            if (
                wishlistIds.includes(
                    productId
                )
            ) {

                button.classList.add(
                    "active"
                );

                button.textContent = "♥";

            } else {

                button.classList.remove(
                    "active"
                );

                button.textContent = "♡";
            }

        });
}


/* =========================================================
   HOMEPAGE PRODUCT SECTIONS
   ========================================================= */

function renderHomepageProducts() {

    const flashDealsGrid =
        document.getElementById(
            "flashDealsGrid"
        );


    const trendingProductsGrid =
        document.getElementById(
            "trendingProductsGrid"
        );


    const bestSellersGrid =
        document.getElementById(
            "bestSellersGrid"
        );


    /*
       Flash Deals
       Highest discounts first
    */

    const flashDeals =
        [...products]
            .sort(
                (a, b) =>
                    b.discount - a.discount
            )
            .slice(0, 4);


    /*
       Trending Products
       Highest ratings + reviews
    */

    const trending =
        [...products]
            .sort(
                (a, b) => {

                    const scoreA =
                        a.rating *
                        Math.log10(
                            a.reviews + 1
                        );

                    const scoreB =
                        b.rating *
                        Math.log10(
                            b.reviews + 1
                        );


                    return scoreB - scoreA;
                }
            )
            .slice(0, 4);


    /*
       Best Sellers
       Highest review count
    */

    const bestSellers =
        [...products]
            .sort(
                (a, b) =>
                    b.reviews - a.reviews
            )
            .slice(0, 4);


    renderProducts(
        flashDealsGrid,
        flashDeals
    );


    renderProducts(
        trendingProductsGrid,
        trending
    );


    renderProducts(
        bestSellersGrid,
        bestSellers
    );


    syncWishlistButtons();
}


/* =========================================================
   SEARCH PRODUCTS
   ========================================================= */

function searchProducts(searchTerm) {

    const term =
        searchTerm
            .trim()
            .toLowerCase();


    if (!term) {
        return products;
    }


    return products.filter(
        product =>

            product.name
                .toLowerCase()
                .includes(term)

            ||

            product.category
                .toLowerCase()
                .includes(term)

            ||

            product.seller
                .toLowerCase()
                .includes(term)
    );
}


/* =========================================================
   FILTER BY CATEGORY
   ========================================================= */

function filterProductsByCategory(
    category
) {

    const normalizedCategory =
        category
            .trim()
            .toLowerCase();


    if (
        !normalizedCategory ||
        normalizedCategory === "all"
    ) {
        return products;
    }


    return products.filter(
        product =>
            product.categorySlug ===
            normalizedCategory
    );
}


/* =========================================================
   INITIALIZE
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        renderHomepageProducts();
        loadProductsFromBackend();

    }
);


/* =========================================================
   GLOBAL PRODUCT API
   ========================================================= */

window.ShopSphereProducts = {

    products,

    formatPrice,

    createProductCard,

    renderProducts,

    getProductById,

    addToCart,

    toggleWishlist,

    searchProducts,

    filterProductsByCategory,

    syncWishlistButtons,

    loadProductsFromBackend,

    mapBackendProduct

};



/* =========================================================
   GLOBAL PRODUCT CARD CLICK
   Opens Product Details
========================================================= */

document.addEventListener("click", function (event) {

    const productCard =
        event.target.closest(".product-card");

    if (!productCard) {
        return;
    }


    /* Don't open details when clicking buttons */

    const clickedButton =
        event.target.closest(
            ".add-to-cart-btn, .product-wishlist, button, a"
        );

    if (clickedButton) {
        return;
    }


    const productId =
        productCard.dataset.productId ||
        productCard.getAttribute("data-product-id");

    if (!productId) {
        return;
    }


    window.location.href =
        `product-details.html?id=${encodeURIComponent(productId)}`;

});
/* =========================================================
   ShopSphere - Wishlist JavaScript
   ========================================================= */

(function () {

    "use strict";


    const WISHLIST_KEY =
        "shopsphere_wishlist";

    const CART_KEY =
        "shopsphere_cart";


    /* =====================================================
       STORAGE
       ===================================================== */

    function getStorage(key, fallback) {

        try {

            const data =
                JSON.parse(
                    localStorage.getItem(key)
                );

            return data === null ||
                   data === undefined
                ? fallback
                : data;

        } catch (error) {

            console.warn(
                "Wishlist storage error:",
                error
            );

            return fallback;
        }
    }


    function setStorage(key, data) {

        try {

            localStorage.setItem(
                key,
                JSON.stringify(data)
            );

            return true;

        } catch (error) {

            console.error(
                "Unable to save wishlist:",
                error
            );

            return false;
        }
    }


    /* =====================================================
       WISHLIST NORMALIZATION
       ===================================================== */

    function getRawWishlist() {

        const data =
            getStorage(
                WISHLIST_KEY,
                []
            );

        return Array.isArray(data)
            ? data
            : [];
    }


    function getProductId(item) {

        if (
            typeof item === "object" &&
            item !== null
        ) {

            return Number(
                item.productId ??
                item.id
            );
        }

        return Number(item);
    }


    function normalizeWishlist() {

        const raw =
            getRawWishlist();

        const normalized = [];


        raw.forEach(function (item) {

            const productId =
                getProductId(item);


            if (
                !Number.isFinite(productId)
            ) {
                return;
            }


            if (
                normalized.some(
                    function (existing) {

                        return existing.productId ===
                            productId;

                    }
                )
            ) {
                return;
            }


            if (
                typeof item === "object" &&
                item !== null
            ) {

                normalized.push({
                    productId:
                        productId,

                    name:
                        item.name || "",

                    price:
                        Number(
                            item.price || 0
                        ),

                    emoji:
                        item.emoji || "📦",

                    seller:
                        item.seller || "",

                    addedAt:
                        item.addedAt ||
                        new Date().toISOString()
                });

            } else {

                normalized.push({
                    productId:
                        productId,

                    addedAt:
                        new Date().toISOString()
                });

            }

        });


        return normalized;
    }


    function saveWishlist(wishlist) {

        return setStorage(
            WISHLIST_KEY,
            wishlist
        );
    }


    function getWishlistIds() {

        return normalizeWishlist()
            .map(function (item) {

                return Number(
                    item.productId
                );

            });
    }


    function isInWishlist(productId) {

        const id =
            Number(productId);

        return getWishlistIds()
            .includes(id);
    }


    /* =====================================================
       PRODUCT DATA
       ===================================================== */

    function getProduct(productId) {

        const id =
            Number(productId);


        if (
            window.ShopSphereProducts &&
            typeof window.ShopSphereProducts
                .getProductById ===
                "function"
        ) {

            return window.ShopSphereProducts
                .getProductById(id);
        }


        return null;
    }


    function getWishlistProducts() {

        const wishlist =
            normalizeWishlist();


        return wishlist
            .map(function (item) {

                const product =
                    getProduct(
                        item.productId
                    );


                if (product) {
                    return product;
                }


                return {

                    id:
                        item.productId,

                    name:
                        item.name ||
                        "Product",

                    price:
                        item.price || 0,

                    emoji:
                        item.emoji ||
                        "📦",

                    seller:
                        item.seller ||
                        "MARTplus Seller"

                };

            });
    }


    /* =====================================================
       ADD / REMOVE
       ===================================================== */

    function addToWishlist(productId) {

        const id =
            Number(productId);


        if (!Number.isFinite(id)) {
            return false;
        }


        let wishlist =
            normalizeWishlist();


        if (
            wishlist.some(
                function (item) {

                    return Number(
                        item.productId
                    ) === id;

                }
            )
        ) {

            return false;
        }


        const product =
            getProduct(id);


        wishlist.push({

            productId:
                id,

            name:
                product?.name || "",

            price:
                Number(
                    product?.price || 0
                ),

            emoji:
                product?.emoji || "📦",

            seller:
                product?.seller || "",

            addedAt:
                new Date().toISOString()

        });


        saveWishlist(
            wishlist
        );


        updateWishlistCount();

        return true;
    }


    function removeFromWishlist(productId) {

        const id =
            Number(productId);


        let wishlist =
            normalizeWishlist();


        const oldLength =
            wishlist.length;


        wishlist =
            wishlist.filter(
                function (item) {

                    return Number(
                        item.productId
                    ) !== id;

                }
            );


        if (
            wishlist.length ===
            oldLength
        ) {
            return false;
        }


        saveWishlist(
            wishlist
        );


        updateWishlistCount();

        return true;
    }


    function toggleWishlist(productId) {

        if (
            isInWishlist(productId)
        ) {

            removeFromWishlist(
                productId
            );

            return false;
        }


        addToWishlist(
            productId
        );

        return true;
    }


    function clearWishlist() {

        saveWishlist([]);

        updateWishlistCount();

    }


    /* =====================================================
       CART
       ===================================================== */

    function getCart() {

        const cart =
            getStorage(
                CART_KEY,
                []
            );

        return Array.isArray(cart)
            ? cart
            : [];
    }


    function saveCart(cart) {

        return setStorage(
            CART_KEY,
            cart
        );
    }


    function addProductToCart(productId) {

        const id =
            Number(productId);


        const product =
            getProduct(id);


        if (!product) {

            showToast(
                "Product not found."
            );

            return false;
        }


        const cart =
            getCart();


        const existing =
            cart.find(
                function (item) {

                    return Number(
                        item.productId ??
                        item.id
                    ) === id;

                }
            );


        if (existing) {

            existing.quantity =
                Number(
                    existing.quantity || 1
                ) + 1;

        } else {

            cart.push({

                productId:
                    id,

                name:
                    product.name,

                price:
                    Number(
                        product.price || 0
                    ),

                quantity:
                    1,

                emoji:
                    product.emoji ||
                    "📦",

                seller:
                    product.seller ||
                    ""

            });

        }


        saveCart(cart);

        updateCartCount();


        showToast(
            "Added to cart."
        );


        return true;
    }


    /* =====================================================
       COUNTS
       ===================================================== */

    function getWishlistCount() {

        return normalizeWishlist()
            .length;
    }


    function getCartCount() {

        return getCart().reduce(
            function (total, item) {

                return total +
                    Number(
                        item.quantity || 1
                    );

            },
            0
        );
    }


    function updateWishlistCount() {

        const count =
            getWishlistCount();


        document
            .querySelectorAll(
                "#wishlistCount, [data-wishlist-count]"
            )
            .forEach(
                function (element) {

                    element.textContent =
                        count;

                }
            );


        const itemCount =
            document.getElementById(
                "wishlistItemCount"
            );


        if (itemCount) {

            itemCount.textContent =
                count +
                (
                    count === 1
                        ? " item"
                        : " items"
                );
        }


        return count;
    }


    function updateCartCount() {

        const count =
            getCartCount();


        document
            .querySelectorAll(
                "#cartCount, [data-cart-count]"
            )
            .forEach(
                function (element) {

                    element.textContent =
                        count;

                }
            );


        return count;
    }


    /* =====================================================
       PRODUCT CARD RENDERING
       ===================================================== */

    function renderProductCard(
        product,
        container
    ) {

        if (
            !product ||
            !container
        ) {
            return;
        }


        if (
            window.ShopSphereProducts &&
            typeof window.ShopSphereProducts
                .createProductCard ===
                "function"
        ) {

            const card =
                window.ShopSphereProducts
                    .createProductCard(
                        product
                    );


            /*
             * createProductCard() returns
             * an HTMLElement, so appendChild()
             * must be used instead of
             * insertAdjacentHTML().
             */

            container.appendChild(
                card
            );

        }
    }


    function renderWishlist() {

        const grid =
            document.getElementById(
                "wishlistGrid"
            );

        if (!grid) {
            return;
        }


        const emptyState =
            document.getElementById(
                "wishlistEmpty"
            );


        const actions =
            document.getElementById(
                "wishlistActions"
            );


        const products =
            getWishlistProducts();


        grid.innerHTML =
            "";


        if (!products.length) {

            if (emptyState) {
                emptyState.style.display =
                    "block";
            }

            if (actions) {
                actions.style.display =
                    "none";
            }

            return;
        }


        if (emptyState) {
            emptyState.style.display =
                "none";
        }


        if (actions) {
            actions.style.display =
                "flex";
        }


        products.forEach(
            function (product) {

                renderProductCard(
                    product,
                    grid
                );

            }
        );


        updateWishlistCount();

        updateCartCount();

    }


    /* =====================================================
       TOAST
       ===================================================== */

    function showToast(message) {

        if (
            window.ShopSphere &&
            typeof window.ShopSphere.showToast ===
                "function"
        ) {

            window.ShopSphere.showToast(
                message
            );

            return;
        }


        let toast =
            document.getElementById(
                "toast"
            );


        if (!toast) {

            toast =
                document.createElement(
                    "div"
                );

            toast.id =
                "toast";

            toast.className =
                "toast";

            document.body.appendChild(
                toast
            );
        }


        toast.textContent =
            message;

        toast.classList.add(
            "show"
        );


        setTimeout(
            function () {

                toast.classList.remove(
                    "show"
                );

            },
            2200
        );
    }


    /* =====================================================
       EVENT HANDLING
       ===================================================== */

    function bindEvents() {

        const grid =
            document.getElementById(
                "wishlistGrid"
            );


        if (grid) {

            grid.addEventListener(
                "click",
                function (event) {

                    const card =
                        event.target.closest(
                            ".product-card"
                        );


                    if (!card) {
                        return;
                    }


                    const productId =
                        Number(
                            card.dataset.productId
                        );


                    if (
                        !Number.isFinite(
                            productId
                        )
                    ) {
                        return;
                    }


                    const addButton =
                        event.target.closest(
                            ".add-to-cart-btn"
                        );


                    const wishlistButton =
                        event.target.closest(
                            ".product-wishlist"
                        );


                    if (addButton) {

                        event.preventDefault();
                        event.stopPropagation();

                        addProductToCart(
                            productId
                        );

                        return;
                    }


                    if (wishlistButton) {

                        event.preventDefault();
                        event.stopPropagation();

                        removeFromWishlist(
                            productId
                        );

                        renderWishlist();

                        showToast(
                            "Removed from wishlist."
                        );

                        return;
                    }


                    /*
                     * Clicking the product card
                     * opens its details page.
                     */

                    window.location.href =
                        "product-details.html?id=" +
                        productId;

                }
            );
        }


        const clearButton =
            document.getElementById(
                "clearWishlistBtn"
            );


        if (clearButton) {

            clearButton.addEventListener(
                "click",
                function () {

                    clearWishlist();

                    renderWishlist();

                    showToast(
                        "Wishlist cleared."
                    );

                }
            );
        }


        document
            .querySelectorAll(
                "[data-wishlist-remove]"
            )
            .forEach(
                function (button) {

                    button.addEventListener(
                        "click",
                        function (event) {

                            event.preventDefault();

                            const id =
                                button.dataset
                                    .wishlistRemove;

                            removeFromWishlist(
                                id
                            );

                            renderWishlist();

                            showToast(
                                "Removed from wishlist."
                            );

                        }
                    );

                }
            );
    }


    /* =====================================================
       INITIALIZE
       ===================================================== */

    function initialize() {

        updateWishlistCount();
        updateCartCount();

        renderWishlist();

        bindEvents();

    }


    /* =====================================================
       PUBLIC API
       ===================================================== */

    window.ShopSphereWishlist = {

        getRawWishlist:
            getRawWishlist,

        normalizeWishlist:
            normalizeWishlist,

        saveWishlist:
            saveWishlist,

        getWishlistIds:
            getWishlistIds,

        getWishlistProducts:
            getWishlistProducts,

        isInWishlist:
            isInWishlist,

        addToWishlist:
            addToWishlist,

        removeFromWishlist:
            removeFromWishlist,

        toggleWishlist:
            toggleWishlist,

        clearWishlist:
            clearWishlist,

        getCart:
            getCart,

        saveCart:
            saveCart,

        addProductToCart:
            addProductToCart,

        getWishlistCount:
            getWishlistCount,

        getCartCount:
            getCartCount,

        updateWishlistCount:
            updateWishlistCount,

        updateCartCount:
            updateCartCount,

        renderWishlist:
            renderWishlist,

        showToast:
            showToast
    };


    document.addEventListener(
        "DOMContentLoaded",
        initialize
    );

})();
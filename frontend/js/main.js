/* =========================================================
   SHOPSPHERE — MAIN JAVASCRIPT
   ========================================================= */


/* =========================================================
   DOM ELEMENTS
   ========================================================= */

const searchForm = document.getElementById("searchForm");
const searchInput = document.getElementById("searchInput");

const categoryMenuBtn = document.getElementById("categoryMenuBtn");
const categoryDrawer = document.getElementById("categoryDrawer");
const closeCategoryDrawer = document.getElementById("closeCategoryDrawer");
const drawerOverlay = document.getElementById("drawerOverlay");

const toast = document.getElementById("toast");
const toastTitle = document.getElementById("toastTitle");
const toastMessage = document.getElementById("toastMessage");
const closeToast = document.getElementById("closeToast");

const newsletterForm = document.getElementById("newsletterForm");
const newsletterEmail = document.getElementById("newsletterEmail");

const cartCountElement = document.getElementById("cartCount");
const wishlistCountElement = document.getElementById("wishlistCount");


/* =========================================================
   LOCAL STORAGE HELPERS
   ========================================================= */

function getStorageData(key, defaultValue = []) {
    try {
        const data = localStorage.getItem(key);

        if (!data) {
            return defaultValue;
        }

        return JSON.parse(data);

    } catch (error) {
        console.error(`Unable to read ${key}:`, error);

        return defaultValue;
    }
}


function saveStorageData(key, data) {
    try {
        localStorage.setItem(
            key,
            JSON.stringify(data)
        );

    } catch (error) {
        console.error(`Unable to save ${key}:`, error);
    }
}


/* =========================================================
   CART COUNT
   ========================================================= */

function updateCartCount() {

    const cart = getStorageData("shopsphere_cart", []);

    const totalItems = cart.reduce(
        (total, item) => {
            return total + Number(item.quantity || 1);
        },
        0
    );


    if (cartCountElement) {
        cartCountElement.textContent = totalItems;
    }
}


/* =========================================================
   WISHLIST COUNT
   ========================================================= */

function updateWishlistCount() {

    const wishlist = getStorageData(
        "shopsphere_wishlist",
        []
    );


    if (wishlistCountElement) {
        wishlistCountElement.textContent =
            wishlist.length;
    }
}


/* =========================================================
   UPDATE HEADER COUNTS
   ========================================================= */

function updateHeaderCounts() {
    updateCartCount();
    updateWishlistCount();
}


/* =========================================================
   SEARCH
   ========================================================= */

if (searchForm) {

    searchForm.addEventListener(
        "submit",
        function (event) {

            event.preventDefault();


            const searchTerm =
                searchInput?.value.trim();


            if (!searchTerm) {

                showToast(
                    "Search",
                    "Please enter a product or keyword."
                );

                searchInput?.focus();

                return;
            }


            const encodedSearch =
                encodeURIComponent(searchTerm);


            window.location.href =
                `products.html?search=${encodedSearch}`;
        }
    );
}


/* =========================================================
   CATEGORY DRAWER
   ========================================================= */

function openCategoryDrawer() {

    if (!categoryDrawer) {
        return;
    }


    categoryDrawer.classList.add("open");

    drawerOverlay?.classList.add("active");


    document.body.style.overflow = "hidden";
}


function closeDrawer() {

    if (!categoryDrawer) {
        return;
    }


    categoryDrawer.classList.remove("open");

    drawerOverlay?.classList.remove("active");


    document.body.style.overflow = "";
}


categoryMenuBtn?.addEventListener(
    "click",
    openCategoryDrawer
);


closeCategoryDrawer?.addEventListener(
    "click",
    closeDrawer
);


drawerOverlay?.addEventListener(
    "click",
    closeDrawer
);


/* =========================================================
   ESC KEY — CLOSE DRAWER
   ========================================================= */

document.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Escape") {
            closeDrawer();
            hideToast();
        }

    }
);


/* =========================================================
   TOAST NOTIFICATION
   ========================================================= */

let toastTimeout;


function showToast(
    title = "Success",
    message = "Action completed successfully."
) {
    let activeToast = document.getElementById("toast");

    if (!activeToast) {
        return;
    }

    let container = activeToast.closest(".toast-container");
    if (!container) {
        container = document.createElement("div");
        container.className = "toast-container";
        container.id = "toastContainer";
        activeToast.parentNode.insertBefore(container, activeToast);
        container.appendChild(activeToast);
    }

    let titleEl = activeToast.querySelector("#toastTitle");
    let messageEl = activeToast.querySelector("#toastMessage");

    if (!titleEl || !messageEl) {
        activeToast.innerHTML = `
            <span class="toast-icon">✓</span>
            <div class="toast-content">
                <strong id="toastTitle"></strong>
                <p id="toastMessage"></p>
            </div>
            <button id="closeToast" type="button" aria-label="Close notification">×</button>
        `;
        titleEl = activeToast.querySelector("#toastTitle");
        messageEl = activeToast.querySelector("#toastMessage");
        const closeBtn = activeToast.querySelector("#closeToast");
        closeBtn?.addEventListener("click", hideToast);
    }

    if (titleEl) {
        titleEl.textContent = title;
    }

    if (messageEl) {
        messageEl.textContent = message;
    }

    activeToast.classList.add("show");

    clearTimeout(toastTimeout);

    toastTimeout = setTimeout(
        hideToast,
        4000
    );
}


function hideToast() {
    const activeToast = document.getElementById("toast");
    activeToast?.classList.remove("show");

    clearTimeout(toastTimeout);
}


closeToast?.addEventListener(
    "click",
    hideToast
);


/* =========================================================
   NEWSLETTER
   ========================================================= */

newsletterForm?.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();


        const email =
            newsletterEmail?.value.trim();


        if (!email) {

            showToast(
                "Newsletter",
                "Please enter your email address."
            );

            return;
        }


        if (!isValidEmail(email)) {

            showToast(
                "Invalid Email",
                "Please enter a valid email address."
            );

            return;
        }


        const subscribers =
            getStorageData(
                "shopsphere_subscribers",
                []
            );


        if (
            subscribers.some(
                subscriber =>
                    subscriber.toLowerCase() ===
                    email.toLowerCase()
            )
        ) {

            showToast(
                "Already Subscribed",
                "This email is already subscribed."
            );

            return;
        }


        subscribers.push(email);


        saveStorageData(
            "shopsphere_subscribers",
            subscribers
        );


        newsletterForm.reset();


        showToast(
            "You're Subscribed! 🎉",
            "You'll receive our latest deals and updates."
        );
    }
);


/* =========================================================
   EMAIL VALIDATION
   ========================================================= */

function isValidEmail(email) {

    const emailPattern =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


    return emailPattern.test(email);
}


/* =========================================================
   FLASH DEAL TIMER
   ========================================================= */

function startFlashDealTimer() {

    const hoursElement =
        document.getElementById("hours");

    const minutesElement =
        document.getElementById("minutes");

    const secondsElement =
        document.getElementById("seconds");


    if (
        !hoursElement ||
        !minutesElement ||
        !secondsElement
    ) {
        return;
    }


    let totalSeconds =
        Number(
            hoursElement.textContent
        ) * 3600
        +
        Number(
            minutesElement.textContent
        ) * 60
        +
        Number(
            secondsElement.textContent
        );


    function updateTimer() {

        if (totalSeconds <= 0) {

            totalSeconds =
                8 * 3600
                +
                59 * 60
                +
                59;
        }


        const hours =
            Math.floor(
                totalSeconds / 3600
            );


        const minutes =
            Math.floor(
                (totalSeconds % 3600) / 60
            );


        const seconds =
            totalSeconds % 60;


        hoursElement.textContent =
            String(hours).padStart(2, "0");


        minutesElement.textContent =
            String(minutes).padStart(2, "0");


        secondsElement.textContent =
            String(seconds).padStart(2, "0");


        totalSeconds--;
    }


    updateTimer();


    setInterval(
        updateTimer,
        1000
    );
}


/* =========================================================
   SMOOTH INTERNAL LINKS
   ========================================================= */

document.addEventListener(
    "click",
    function (event) {

        const link =
            event.target.closest("a[href^='#']");


        if (!link) {
            return;
        }


        const targetId =
            link.getAttribute("href");


        if (
            !targetId ||
            targetId === "#"
        ) {
            return;
        }


        const target =
            document.querySelector(targetId);


        if (!target) {
            return;
        }


        event.preventDefault();


        target.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }
);


/* =========================================================
   HEADER SCROLL EFFECT
   ========================================================= */

let lastScrollPosition = 0;


window.addEventListener(
    "scroll",
    function () {

        const header =
            document.querySelector(".main-header");


        if (!header) {
            return;
        }


        const currentScroll =
            window.scrollY;


        if (currentScroll > 80) {

            header.style.boxShadow =
                "0 5px 25px rgba(17, 24, 39, 0.07)";

        } else {

            header.style.boxShadow =
                "none";
        }


        lastScrollPosition =
            currentScroll;
    },
    {
        passive: true
    }
);


/* =========================================================
   ACTIVE NAVIGATION
   ========================================================= */

/* =========================================================
   ACTIVE NAVIGATION
   ========================================================= */

function setActiveNavigation() {

    const currentPage =
        window.location.pathname
            .split("/")
            .pop();

    const currentParams =
        new URLSearchParams(
            window.location.search
        );

    const currentCategory =
        currentParams.get("category");

    const navLinks =
        document.querySelectorAll(
            ".nav-links a"
        );


    navLinks.forEach(link => {

        const href =
            link.getAttribute("href");

        if (!href) {
            return;
        }


        const linkURL =
            new URL(
                href,
                window.location.href
            );

        const linkPage =
            linkURL.pathname
                .split("/")
                .pop();

        const linkCategory =
            linkURL.searchParams.get(
                "category"
            );


        let isActive = false;


        /* =========================================
           NORMAL PAGES
        ========================================== */

        if (
            linkPage === currentPage &&
            !linkCategory &&
            !currentCategory
        ) {
            isActive = true;
        }


        /* =========================================
           CATEGORY PAGES
        ========================================== */

        if (
            currentPage === "category.html" &&
            linkPage === "category.html" &&
            linkCategory === currentCategory
        ) {
            isActive = true;
        }


        /* =========================================
           HOMEPAGE
        ========================================== */

        if (
            currentPage === "" &&
            linkPage === "index.html"
        ) {
            isActive = true;
        }


        if (isActive) {

            link.classList.add("active");

        } else {

            link.classList.remove("active");

        }

    });

}


/* =========================================================
   GLOBAL WINDOW HELPERS
   ========================================================= */

window.ShopSphere = {

    getStorageData,

    saveStorageData,

    updateCartCount,

    updateWishlistCount,

    updateHeaderCounts,

    showToast,

    hideToast,

    isValidEmail

};


/* =========================================================
   INITIALIZATION
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        updateHeaderCounts();

        startFlashDealTimer();

        setActiveNavigation();

    }
);
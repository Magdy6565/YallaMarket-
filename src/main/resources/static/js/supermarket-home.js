// supermarket-home.js - JavaScript for the supermarket homepage

// Global variables
let currentCategoryId = null;
let currentVendorFilter = '';
let cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];
let vendors = [];
let categoriesData = [];
let currentOffset = 0;
const LIMIT = 4; // Number of products per page

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // --- DOM Element Selections ---
    const categoriesContainer = document.getElementById('categoriesContainer');
    const productsGrid = document.getElementById('productsGrid');
    const emptyState = document.getElementById('emptyState');
    const vendorFilter = document.getElementById('vendorFilter');
    const filterButton = document.getElementById('filterButton');
    const productModal = document.getElementById('productModal');
    const closeModal = document.querySelector('.close-modal');
    const modalBody = document.getElementById('modalBody');
    const cartBadge = document.getElementById('cartBadge');

    // Update cart badge
    updateCartBadge();

    // Mobile menu toggle functionality
    const menuToggle = document.getElementById('menuToggle');
    const navbarLinks = document.getElementById('navbarLinks');

    if (menuToggle && navbarLinks) {
        menuToggle.addEventListener('click', function() {
            navbarLinks.classList.toggle('active');
        });
    }

    // User menu dropdown functionality
    const userMenu = document.getElementById('userMenu');
    const userMenuButton = userMenu ? userMenu.querySelector('.user-menu-button') : null;

    if (userMenuButton) {
        userMenuButton.addEventListener('click', function() {
            userMenu.classList.toggle('open');
        });

        // Close the dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!userMenu.contains(event.target)) {
                userMenu.classList.remove('open');
            }
        });
    }

    // --- Core Functions ---

    /**
     * Fetch categories from the API
     */
    async function fetchCategories() {
        try {
            const response = await fetch('/api/my-products/categories');
            
            if (!response.ok) {
                throw new Error('Failed to fetch categories');
            }
            
            categoriesData = await response.json();
            renderCategories(categoriesData);
        } catch (error) {
            console.error('Error fetching categories:', error);
            categoriesContainer.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load categories. Please try again later.</p>
                </div>
            `;
        }
    }

    /**
     * Render categories in the UI
     */
    function renderCategories(categories) {
        categoriesContainer.innerHTML = '';
        
        if (!categories || categories.length === 0) {
            categoriesContainer.innerHTML = `
                <div class="empty">
                    <p>No categories available</p>
                </div>
            `;
            return;
        }

        // Map category names to appropriate Font Awesome icons
        const categoryIcons = {
            'Fruits': 'fa-apple-whole',
            'Vegetables': 'fa-carrot',
            'Meat': 'fa-drumstick-bite',
            'Dairy': 'fa-cheese',
            'Bakery': 'fa-bread-slice',
            'Beverages': 'fa-mug-hot',
            'Snacks': 'fa-cookie',
            'Canned Goods': 'fa-can-food',
            'Frozen Foods': 'fa-snowflake',
            'Household': 'fa-broom',
            'Personal Care': 'fa-pump-soap',
            'Baby Products': 'fa-baby',
            'Pet Supplies': 'fa-paw',
            'Electronics': 'fa-laptop',
            'Clothing': 'fa-tshirt',
            'Toys': 'fa-gamepad',
            'Books': 'fa-book',
            'Office Supplies': 'fa-paperclip',
            'Sports & Outdoors': 'fa-football',
            'Tools & Hardware': 'fa-tools',
            'Home Decor': 'fa-couch',
            'Garden': 'fa-leaf',
            'Health & Wellness': 'fa-heart-pulse',
            'Beauty': 'fa-spa'
        };

        // Create category cards
        categories.forEach(category => {
            const iconClass = categoryIcons[category] || 'fa-tag';
            const categoryCard = document.createElement('div');
            categoryCard.className = 'category-card';
            categoryCard.setAttribute('data-category-id', category.id);
            categoryCard.innerHTML = `
                <div class="category-icon">
                    <i class="fas ${iconClass}"></i>
                </div>
                <div class="category-name">${category}</div>
            `;
            
            // Add click event to fetch products of this category
            categoryCard.addEventListener('click', function() {
                // Remove active class from all categories
                document.querySelectorAll('.category-card.active').forEach(card => {
                    card.classList.remove('active');
                });
                
                // Add active class to clicked category
                this.classList.add('active');
                
                // Store the selected category ID
                currentCategoryId = category.id;
                
                // Reset offset when changing category
                currentOffset = 0;
                
                // Fetch products for this category
                fetchProducts(category.id, currentVendorFilter);
            });
            
            categoriesContainer.appendChild(categoryCard);
        });
    }

    /**
     * Fetch vendors list
     */
    async function fetchVendors() {
        try {
            // Replace with your actual vendor API endpoint
            const response = await fetch('/users/role/1');
            
            if (!response.ok) {
                throw new Error('Failed to fetch vendors');
            }
            
            vendors = await response.json();
            populateVendorFilter(vendors);
        } catch (error) {
            console.error('Error fetching vendors:', error);
        }
    }

    /**
     * Populate vendor filter dropdown
     */
    function populateVendorFilter(vendors) {
        vendorFilter.innerHTML = '<option value="">All Vendors</option>';
        
        vendors.forEach(vendor => {
            const option = document.createElement('option');
            option.value = vendor.id;
            option.textContent = vendor.name;
            vendorFilter.appendChild(option);
        });
    }

    /**
     * Fetch products by category and vendor filter
     */
    async function fetchProducts(categoryId, vendorIds = '') {
        // Show loading state
        productsGrid.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading products...</div>';
        
        try {
            // Create URL with query parameters
            let url = `/api/my-products/category/${categoryId}?offset=${currentOffset}&limit=${LIMIT}`;
            
            if (vendorIds) {
                url += `&vendorids=${vendorIds}`;
            }
            
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error('Failed to fetch products');
            }
            
            const products = await response.json();
            renderProducts(products);
        } catch (error) {
            console.error('Error fetching products:', error);
            productsGrid.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load products. Please try again later.</p>
                </div>
            `;
        }
    }

    /**
     * Render products in the grid
     */
    function renderProducts(products) {
        // Hide loading state
        productsGrid.innerHTML = '';
        
        if (!products || products.length === 0) {
            emptyState.style.display = 'flex';
            return;
        }
        
        emptyState.style.display = 'none';
        
        // Render each product card
        products.forEach(product => {
            const productCard = document.createElement('div');
            productCard.className = 'product-card';
            
            // Check if product is in cart
            const isInCart = cartItems.some(item => item.id === product.id);
            
            productCard.innerHTML = `
                <div class="product-image">
                    <img src="${product.imageUrl || 'https://via.placeholder.com/250'}" alt="${product.name}">
                </div>
                <div class="product-info">
                    <h3 class="product-name">${product.name}</h3>
                    <span class="product-category">${product.category}</span>
                    <div class="product-vendor">${product.vendorName || 'Unknown Vendor'}</div>
                    <div class="product-price">$${parseFloat(product.price).toFixed(2)}</div>
                    
                    <div class="product-stock">
                        ${product.quantity > 10 ? 
                            '<span class="in-stock"><i class="fas fa-check-circle"></i> In Stock</span>' : 
                            product.quantity > 0 ? 
                                `<span class="low-stock"><i class="fas fa-exclamation-circle"></i> Low Stock: ${product.quantity} left</span>` :
                                '<span class="out-of-stock"><i class="fas fa-times-circle"></i> Out of Stock</span>'
                        }
                    </div>
                    
                    <div class="product-actions">
                        <a href="javascript:void(0)" class="view-btn" data-product-id="${product.id}">
                            <i class="fas fa-eye"></i> View
                        </a>
                        <a href="javascript:void(0)" class="cart-btn ${isInCart ? 'added' : ''}" data-product-id="${product.id}">
                            <i class="fas ${isInCart ? 'fa-check' : 'fa-cart-plus'}"></i> 
                            ${isInCart ? 'Added' : 'Add to Cart'}
                        </a>
                    </div>
                </div>
            `;
            
            productsGrid.appendChild(productCard);
        });
        
        // Add event listeners to buttons
        attachProductEventListeners();
    }

    /**
     * Attach event listeners to product buttons
     */
    function attachProductEventListeners() {
        // View button click
        document.querySelectorAll('.view-btn').forEach(button => {
            button.addEventListener('click', function() {
                const productId = this.getAttribute('data-product-id');
                openProductModal(productId);
            });
        });
        
        // Add to cart button click
        document.querySelectorAll('.cart-btn').forEach(button => {
            button.addEventListener('click', function() {
                const productId = this.getAttribute('data-product-id');
                
                // Check if product is already in cart
                const isInCart = cartItems.some(item => item.id === productId);
                
                if (!isInCart) {
                    // Find product data from the DOM
                    const productCard = this.closest('.product-card');
                    const productName = productCard.querySelector('.product-name').textContent;
                    const productPrice = parseFloat(productCard.querySelector('.product-price').textContent.replace('$', ''));
                    const productCategory = productCard.querySelector('.product-category').textContent;
                    const productImage = productCard.querySelector('.product-image img').src;
                    
                    // Add to cart
                    addToCart({
                        id: productId,
                        name: productName,
                        price: productPrice,
                        category: productCategory,
                        imageUrl: productImage,
                        quantity: 1
                    });
                    
                    // Update button style
                    this.classList.add('added');
                    this.innerHTML = '<i class="fas fa-check"></i> Added';
                }
            });
        });
    }

    /**
     * Open product modal with details
     */
    async function openProductModal(productId) {
        try {
            // Show loading in modal
            modalBody.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading product details...</div>';
            
            // Open modal
            productModal.style.display = 'block';
            
            // Fetch product details
            const response = await fetch(`/api/products/${productId}`);
            
            if (!response.ok) {
                throw new Error('Failed to fetch product details');
            }
            
            const product = await response.json();
            
            // Check if product is in cart
            const cartItem = cartItems.find(item => item.id === productId);
            const isInCart = !!cartItem;
            
            // Render product details in modal
            modalBody.innerHTML = `
                <div class="modal-product">
                    <div class="modal-product-header">
                        <div class="modal-product-image">
                            <img src="${product.imageUrl || 'https://via.placeholder.com/300'}" alt="${product.name}">
                        </div>
                        <div class="modal-product-info">
                            <h2 class="modal-product-title">${product.name}</h2>
                            <span class="modal-product-category">${product.category}</span>
                            <div class="modal-product-price">$${parseFloat(product.price).toFixed(2)}</div>
                            <div class="modal-product-vendor">
                                <strong>Vendor:</strong> ${product.vendorName || 'Unknown Vendor'}
                            </div>
                            <div class="modal-product-stock">
                                ${product.quantity > 10 ? 
                                    '<span class="in-stock"><i class="fas fa-check-circle"></i> In Stock</span>' : 
                                    product.quantity > 0 ? 
                                        `<span class="low-stock"><i class="fas fa-exclamation-circle"></i> Low Stock: ${product.quantity} left</span>` :
                                        '<span class="out-of-stock"><i class="fas fa-times-circle"></i> Out of Stock</span>'
                                }
                            </div>
                        </div>
                    </div>
                    
                    <div class="modal-product-description">
                        <h3>Description</h3>
                        <p>${product.description || 'No description available'}</p>
                    </div>
                    
                    <div class="modal-product-actions">
                        <div class="quantity-control">
                            <button class="quantity-btn minus">-</button>
                            <input type="number" class="quantity-input" value="${isInCart ? cartItem.quantity : 1}" min="1" max="${product.quantity || 99}">
                            <button class="quantity-btn plus">+</button>
                            
                            <button class="add-to-cart-btn" data-product-id="${product.id}">
                                ${isInCart ? '<i class="fas fa-sync"></i> Update Cart' : '<i class="fas fa-cart-plus"></i> Add to Cart'}
                            </button>
                        </div>
                    </div>
                </div>
            `;
            
            // Add modal event listeners
            const quantityInput = modalBody.querySelector('.quantity-input');
            const minusBtn = modalBody.querySelector('.minus');
            const plusBtn = modalBody.querySelector('.plus');
            const addToCartBtn = modalBody.querySelector('.add-to-cart-btn');
            
            // Quantity buttons
            minusBtn.addEventListener('click', function() {
                if (quantityInput.value > 1) {
                    quantityInput.value = parseInt(quantityInput.value) - 1;
                }
            });
            
            plusBtn.addEventListener('click', function() {
                const max = parseInt(quantityInput.max);
                if (parseInt(quantityInput.value) < max) {
                    quantityInput.value = parseInt(quantityInput.value) + 1;
                }
            });
            
            // Add to cart button in modal
            addToCartBtn.addEventListener('click', function() {
                const quantity = parseInt(quantityInput.value);
                
                // Add to cart
                addToCart({
                    id: product.id,
                    name: product.name,
                    price: product.price,
                    category: product.category,
                    imageUrl: product.imageUrl || 'https://via.placeholder.com/100',
                    quantity: quantity
                }, true); // true = update if exists
                
                // Close modal after adding to cart
                productModal.style.display = 'none';
                
                // Update product buttons in the grid
                updateProductButtons(product.id);
            });
            
        } catch (error) {
            console.error('Error loading product details:', error);
            modalBody.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load product details. Please try again later.</p>
                </div>
            `;
        }
    }

    /**
     * Add product to cart
     */
    function addToCart(product, updateExisting = false) {
        // Check if product is already in cart
        const existingItemIndex = cartItems.findIndex(item => item.id === product.id);
        
        if (existingItemIndex !== -1 && updateExisting) {
            // Update quantity if product exists
            cartItems[existingItemIndex].quantity = product.quantity;
        } else if (existingItemIndex === -1) {
            // Add new product to cart
            cartItems.push(product);
        }
        
        // Save cart to localStorage
        localStorage.setItem('cartItems', JSON.stringify(cartItems));
        
        // Update cart badge
        updateCartBadge();
    }

    /**
     * Update cart badge count
     */
    function updateCartBadge() {
        const totalItems = cartItems.reduce((total, item) => total + item.quantity, 0);
        cartBadge.textContent = totalItems;
        
        // Make badge visible only if items in cart
        cartBadge.style.display = totalItems > 0 ? 'flex' : 'none';
    }

    /**
     * Update product buttons in grid after cart changes
     */
    function updateProductButtons(productId) {
        // Update regular product cards in grid
        const productButton = document.querySelector(`.cart-btn[data-product-id="${productId}"]`);
        if (productButton) {
            productButton.classList.add('added');
            productButton.innerHTML = '<i class="fas fa-check"></i> Added';
        }
    }

    // --- Event Listeners ---
    
    // Filter button click
    if (filterButton) {
        filterButton.addEventListener('click', function() {
            currentVendorFilter = vendorFilter.value;
            currentOffset = 0; // Reset pagination when filtering
            
            if (currentCategoryId) {
                fetchProducts(currentCategoryId, currentVendorFilter);
            } else {
                alert('Please select a category first');
            }
        });
    }
    
    // Modal close button
    if (closeModal) {
        closeModal.addEventListener('click', function() {
            productModal.style.display = 'none';
        });
    }
    
    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === productModal) {
            productModal.style.display = 'none';
        }
    });

    // --- Initial Setup ---
    fetchCategories();
    fetchVendors();
});

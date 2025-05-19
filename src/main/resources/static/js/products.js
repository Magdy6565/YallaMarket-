// products.js - JavaScript for the products page functionality

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
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
    
    // Product card hover effects (optional enhancement)
    const productCards = document.querySelectorAll('.product-card');
    
    productCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 5px 15px rgba(0, 0, 0, 0.1)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
        });
    });
});

// Function to confirm product deletion (for the delete button in product view)
function confirmDelete(productId) {
    if (confirm('Are you sure you want to delete this product? This action cannot be undone.')) {
        // Send delete request to the API
        fetch(`/api/my-products/${productId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                // Redirect to products page on success
                window.location.href = '/products';
            } else {
                alert('Failed to delete product. Please try again.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        });
    }
}

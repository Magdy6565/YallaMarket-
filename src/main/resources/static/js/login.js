// login.js
document.addEventListener('DOMContentLoaded', function() {
    // This function will run when the DOM is fully loaded
    console.log('Login page loaded');
    
    // Add event listener to the login form
    const loginForm = document.querySelector('form');
    if (loginForm) {
        loginForm.addEventListener('submit', function(event) {
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            // Basic validation
            if (!username || !password) {
                event.preventDefault();
                alert('Please enter both username and password');
            }
        });
    }
    
    // Animate form elements on page load
    const formElements = document.querySelectorAll('.form-group, .form-options, .login-btn, .register-link');
    formElements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';
        element.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        
        setTimeout(() => {
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, 100 * (index + 1));
    });
});

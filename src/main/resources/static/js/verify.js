document.addEventListener("DOMContentLoaded", function () {
  const verifyForm = document.getElementById("verifyForm");
  const resendBtn = document.getElementById("resendBtn");
  const timerSpan = document.getElementById("timer");
  const verifyError = document.getElementById("verifyError");
  const verifySuccess = document.getElementById("verifySuccess");
  let countdown = 60;
  let timerInterval;

  // Enable resend after 60 seconds
  function startTimer() {
    resendBtn.disabled = true;
    countdown = 60;
    timerSpan.textContent = countdown;
    timerInterval = setInterval(() => {
      countdown--;
      timerSpan.textContent = countdown;
      if (countdown <= 0) {
        clearInterval(timerInterval);
        resendBtn.disabled = false;
        timerSpan.textContent = "0";
      }
    }, 1000);
  }

  startTimer();

  verifyForm.addEventListener("submit", function (e) {
    e.preventDefault();
    verifyError.textContent = "";
    verifySuccess.textContent = "";
    const email = document.getElementById("verifyEmail").value.trim();
    const code = document.getElementById("verificationCode").value.trim();
    fetch("/auth/verify", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, verificationCode: code }),
    })
      .then((res) => {
        return res.text().then((text) => {
          let data;
          try {
            data = JSON.parse(text);
          } catch (e) {
            data = text;
          }
          return { status: res.status, body: data };
        });
      })
      .then(({ status, body }) => {
        if (status === 200) {
          verifySuccess.textContent = "Account verified! Redirecting...";
          setTimeout(() => {
            window.location.href = "/login";
          }, 1500);
        } else {
          verifyError.textContent =
            body && body.message
              ? body.message
              : body || "Invalid code or error.";
        }
      })
      .catch(() => {
        verifyError.textContent = "Network error.";
      });
  });

  resendBtn.addEventListener("click", function () {
    resendBtn.disabled = true;
    verifyError.textContent = "";
    verifySuccess.textContent = "";
    const email = document.getElementById("verifyEmail").value.trim();
    if (!email) {
      verifyError.textContent = "Please enter your email to resend.";
      resendBtn.disabled = false;
      return;
    }
    fetch("/auth/resend?email=" + encodeURIComponent(email), {
      method: "POST",
    })
      .then((res) =>
        res.text().then((text) => ({ status: res.status, body: text }))
      )
      .then(({ status, body }) => {
        if (status === 200) {
          verifySuccess.textContent = "Verification code resent!";
          startTimer();
        } else {
          verifyError.textContent = body || "Could not resend code.";
          resendBtn.disabled = false;
        }
      })
      .catch(() => {
        verifyError.textContent = "Network error.";
        resendBtn.disabled = false;
      });
  });
});

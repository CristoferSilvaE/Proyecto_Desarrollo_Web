function toggleUserMenu(event) {
  event.stopPropagation();

  const menu = document.getElementById("userDropdown");

  if (menu) {
    menu.classList.toggle("active");
  }
}

document.addEventListener("click", function (event) {
  const menu = document.getElementById("userDropdown");

  if (!menu) {
    return;
  }

  if (!event.target.closest(".user-menu")) {
    menu.classList.remove("active");
  }
});

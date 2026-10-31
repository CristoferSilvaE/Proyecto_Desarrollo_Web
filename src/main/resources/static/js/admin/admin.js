(() => {
  const toast = document.getElementById("admToast");
  let toastTimer;
  const notify = (message) => {
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("is-visible");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove("is-visible"), 3800);
  };

  document.querySelectorAll("[data-open-modal]").forEach((button) => {
    button.addEventListener("click", () => {
      const dialog = document.getElementById(button.dataset.openModal);
      if (dialog && !dialog.open) dialog.showModal();
    });
  });
  document.querySelectorAll("[data-close-modal]").forEach((button) => {
    button.addEventListener("click", () => button.closest("dialog")?.close());
  });
  document.querySelectorAll("dialog.adm-modal").forEach((dialog) => {
    dialog.addEventListener("click", (event) => {
      if (event.target === dialog) dialog.close();
    });
  });
  document.querySelectorAll("[data-demo-form]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      form.closest("dialog")?.close();
      notify("Interfaz lista. El guardado en MySQL se implementará en la siguiente etapa.");
    });
  });
  document.querySelectorAll("[data-toast]").forEach((button) => {
    button.addEventListener("click", () => notify(button.dataset.toast));
  });
  document.querySelectorAll("[data-table-search]").forEach((input) => {
    input.addEventListener("input", () => {
      const table = document.getElementById(input.dataset.tableSearch);
      const term = input.value.toLocaleLowerCase("es").trim();
      table?.querySelectorAll("tbody tr").forEach((row) => {
        row.hidden = !row.textContent.toLocaleLowerCase("es").includes(term);
      });
    });
  });
  document.querySelectorAll("[data-panel-tabs]").forEach((tabs) => {
    tabs.querySelectorAll("[data-panel-target]").forEach((button) => {
      button.addEventListener("click", () => {
        tabs.querySelectorAll("[data-panel-target]").forEach((b) => b.classList.toggle("is-active", b === button));
        document.querySelectorAll(".adm-tab-panel").forEach((panel) => {
          panel.hidden = panel.id !== button.dataset.panelTarget;
        });
      });
    });
  });
  // Abrir automáticamente la pestaña indicada en la URL
  const params = new URLSearchParams(window.location.search);

  const pestañas = {
    instructores: "tabInstructores",
    clases: "tabClases",
    horarios: "tabHorarios",
  };

  const pestaña = pestañas[params.get("tab")];

  if (pestaña) {
    document.querySelector(`[data-panel-target="${pestaña}"]`)?.click();
  }
})();

function editarClase(boton) {
  document.getElementById("claseId").value = boton.dataset.id;

  document.getElementById("claseNombre").value = boton.dataset.nombre;

  document.getElementById("claseCategoria").value = boton.dataset.categoria;

  document.getElementById("claseDescripcion").value = boton.dataset.descripcion || "";

  document.getElementById("tituloModalClase").textContent = "EDITAR CLASE";

  document.getElementById("btnGuardarClase").textContent = "GUARDAR CAMBIOS";
}

// Preparar el modal para crear una clase nueva
document.querySelector('[data-open-modal="modalNuevaClase"]:not([data-id])')?.addEventListener("click", () => {
  document.getElementById("formClase").reset();

  document.getElementById("claseId").value = "";

  document.getElementById("tituloModalClase").textContent = "NUEVA CLASE";

  document.getElementById("btnGuardarClase").textContent = "CREAR CLASE";
});

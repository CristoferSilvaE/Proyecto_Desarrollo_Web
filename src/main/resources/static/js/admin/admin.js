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
      const modalId = button.dataset.openModal;

      // PREPARAR MODAL DE INSTRUCTOR
      if (modalId === "modalNuevoInstructor") {
        const form = document.getElementById("formInstructor");

        if (button.dataset.id) {
          // MODO EDICIÓN
          form.action = form.dataset.editAction;

          document.getElementById("instructorId").value = button.dataset.id;
          document.getElementById("instructorNombres").value = button.dataset.nombres;
          document.getElementById("instructorApellidos").value = button.dataset.apellidos;
          document.getElementById("instructorClase").value = button.dataset.idClase;
          document.getElementById("instructorTelefono").value = button.dataset.telefono;
          document.getElementById("instructorCorreo").value = button.dataset.correo;

          document.getElementById("tituloModalInstructor").textContent = "EDITAR INSTRUCTOR";

          document.getElementById("btnGuardarInstructor").textContent = "GUARDAR CAMBIOS";
        } else {
          // MODO REGISTRO
          form.reset();

          form.action = form.dataset.createAction;

          document.getElementById("instructorId").value = "";

          document.getElementById("tituloModalInstructor").textContent = "NUEVO INSTRUCTOR";

          document.getElementById("btnGuardarInstructor").textContent = "REGISTRAR INSTRUCTOR";
        }
      }

      // PREPARAR CONFIRMACIÓN DE ESTADO
      if (modalId === "modalConfirmarEstadoInstructor") {
        const form = document.getElementById("formEstadoInstructor");

        const estado = button.dataset.estado;
        const nombre = button.dataset.nombre;

        form.action = button.dataset.action;

        document.getElementById("estadoInstructorNuevo").value = estado;

        document.getElementById("tituloModalEstadoInstructor").textContent = estado === "INACTIVO" ? "DESACTIVAR INSTRUCTOR" : "ACTIVAR INSTRUCTOR";

        document.getElementById("textoConfirmacionInstructor").textContent = estado === "INACTIVO" ? `¿Estás seguro de desactivar a ${nombre}?` : `¿Deseas volver a activar a ${nombre}?`;

        document.getElementById("btnConfirmarEstadoInstructor").textContent = estado === "INACTIVO" ? "DESACTIVAR" : "ACTIVAR";
      }

      // PREPARAR CONFIRMACIÓN DE ESTADO DEL HORARIO
      if (modalId === "modalConfirmarEstadoHorario") {
        const form = document.getElementById("formEstadoHorario");

        const estado = button.dataset.estado;
        const nombre = button.dataset.nombre;

        form.action = button.dataset.action;

        document.getElementById("estadoHorarioNuevo").value = estado;

        document.getElementById("tituloModalEstadoHorario").textContent = estado === "INACTIVO" ? "CANCELAR HORARIO" : "REACTIVAR HORARIO";

        document.getElementById("textoConfirmacionHorario").textContent = estado === "INACTIVO" ? `¿Estás seguro de cancelar la sesión ${nombre}?` : `¿Deseas volver a activar la sesión ${nombre}?`;

        document.getElementById("btnConfirmarEstadoHorario").textContent = estado === "INACTIVO" ? "CONFIRMAR CANCELACIÓN" : "REACTIVAR HORARIO";
      }

      const dialog = document.getElementById(modalId);

      if (dialog && !dialog.open) {
        dialog.showModal();
      }
    });
  });
  document.querySelectorAll("[data-close-modal]").forEach((button) => {
    button.addEventListener("click", () => button.closest("dialog")?.close());
  });
  document.querySelectorAll("dialog.adm-modal").forEach((dialog) => {
    dialog.addEventListener("click", (event) => {
      if (event.target === dialog && !dialog.hasAttribute("data-no-backdrop-close")) {
        dialog.close();
      }
    });
  });

  // Abrir automáticamente el modal de contraseña temporal
  const modalPasswordTemporal = document.getElementById("modalPasswordTemporal");

  if (modalPasswordTemporal && !modalPasswordTemporal.open) {
    modalPasswordTemporal.showModal();
  }
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
    solicitudes: "tabSolicitudes",
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

// ==========================================
// FILTRAR INSTRUCTORES SEGÚN LA CLASE
// ==========================================

function filtrarInstructoresPorClase(idInstructor = "") {
  const claseSelect = document.getElementById("horarioClase");
  const instructorSelect = document.getElementById("horarioInstructor");

  if (!claseSelect || !instructorSelect) return;

  const idClase = claseSelect.value;

  Array.from(instructorSelect.options).forEach((option) => {
    if (!option.value) {
      option.hidden = false;
      option.disabled = false;
      return;
    }

    const coincide = option.dataset.claseId === idClase;

    option.hidden = !coincide;
    option.disabled = !coincide;
  });

  if (idInstructor) {
    const opcionValida = Array.from(instructorSelect.options).find((option) => option.value === String(idInstructor) && !option.disabled);

    instructorSelect.value = opcionValida ? String(idInstructor) : "";
  } else if (instructorSelect.selectedOptions[0]?.disabled) {
    instructorSelect.value = "";
  }
}

// DETECTAR CAMBIO DE CLASE
document.getElementById("horarioClase")?.addEventListener("change", () => {
  filtrarInstructoresPorClase();
});
// ==========================================
// GESTIÓN DE HORARIOS
// ==========================================

// ==========================================
// EDITAR HORARIO
// ==========================================

function editarHorario(boton) {
  document.getElementById("horarioId").value = boton.dataset.id;

  document.getElementById("horarioClase").value = boton.dataset.clase;

  // Filtrar y seleccionar el instructor correspondiente
  filtrarInstructoresPorClase(boton.dataset.instructor);

  document.getElementById("horarioFecha").value = boton.dataset.fecha;

  document.getElementById("horarioHoraInicio").value = boton.dataset.inicio;

  document.getElementById("horarioHoraFin").value = boton.dataset.fin;

  document.getElementById("horarioCapacidad").value = boton.dataset.capacidad;

  document.getElementById("horarioSala").value = boton.dataset.sala || "";

  document.getElementById("tituloModalHorario").textContent = "EDITAR HORARIO";

  document.getElementById("btnGuardarHorario").textContent = "GUARDAR CAMBIOS";
}

// ==========================================
// NUEVO HORARIO
// ==========================================

document.querySelector('[data-open-modal="modalNuevoHorario"]:not([data-id])')?.addEventListener("click", () => {
  const form = document.getElementById("formHorario");

  form.reset();

  document.getElementById("horarioId").value = "";

  document.getElementById("tituloModalHorario").textContent = "PROGRAMAR HORARIO";

  document.getElementById("btnGuardarHorario").textContent = "PROGRAMAR CLASE";

  filtrarInstructoresPorClase();
});

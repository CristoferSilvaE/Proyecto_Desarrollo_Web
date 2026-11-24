function cerrarModal(id) {
  const modal = document.getElementById(id);
  if (modal && modal.open) {
    modal.close();
  }
}

async function consultarPedido(id) {
  const respuesta = await fetch(`/pedidos/${id}/detalle`);

  if (!respuesta.ok) {
    throw new Error("No se pudo consultar el pedido.");
  }

  return await respuesta.json();
}

function formatoMoneda(valor) {
  return `S/ ${Number(valor).toFixed(2)}`;
}

function formatearMetodoPago(metodo) {
  switch (metodo) {
    case "TARJETA":
      return "Tarjeta de crédito o débito";
    case "YAPE_PLIN":
      return "Yape / Plin";
    case "TRANSFERENCIA":
      return "Transferencia bancaria";
    default:
      return metodo || "-";
  }
}

function formatearEstado(estado) {
  switch (estado) {
    case "EN_PROCESO":
      return "EN PROCESO";
    case "REEMBOLSO_PENDIENTE":
      return "REEMBOLSO PENDIENTE";
    case "CANCELADO":
      return "CANCELADO";
    default:
      return estado || "-";
  }
}

function agregarTexto(contenedor, texto, clase = "") {
  const elemento = document.createElement("p");
  elemento.textContent = texto;
  if (clase) {
    elemento.className = clase;
  }
  contenedor.appendChild(elemento);
  return elemento;
}

// =======================================
// FILTROS POR ESTADO
// =======================================

function filtrarPedidos(estado, botonActivo) {
  document.querySelectorAll(".order-filters button").forEach((btn) => {
    btn.className = btn === botonActivo ? "order-filter-active" : "order-filter";
  });

  const tarjetas = document.querySelectorAll("#listaPedidos .order-card");
  let visibles = 0;

  tarjetas.forEach((tarjeta) => {
    const coincide = estado === "TODOS" || tarjeta.dataset.estado === estado;
    tarjeta.classList.toggle("hidden", !coincide);
    if (coincide) {
      visibles++;
    }
  });

  const vacio = document.getElementById("filtroVacio");
  if (vacio && tarjetas.length > 0) {
    vacio.classList.toggle("hidden", visibles > 0);
  }
}

// =======================================
// VER DETALLE
// =======================================

async function abrirDetalle(id) {
  try {
    const pedido = await consultarPedido(id);
    const contenido = document.getElementById("contenidoDetalle");
    contenido.replaceChildren();

    document.getElementById("tituloDetalle").textContent = `Pedido #PF-${pedido.idPedido}`;

    agregarTexto(contenido, `Fecha de pedido: ${pedido.fechaPedido || "-"}`);
    agregarTexto(contenido, `Estado del pedido: ${formatearEstado(pedido.estado)}`);
    agregarTexto(contenido, `Estado del pago: ${formatearEstado(pedido.estadoPago)}`);
    if (pedido.fechaPago) {
      agregarTexto(contenido, `Fecha de pago: ${pedido.fechaPago}`);
    }
    agregarTexto(contenido, `Método de pago: ${formatearMetodoPago(pedido.metodoPago)}`);
    agregarTexto(contenido, `Comprobante: ${pedido.tipoComprobante}`);

    if (pedido.tipoComprobante === "FACTURA") {
      agregarTexto(contenido, `RUC: ${pedido.ruc || "-"}`);
      agregarTexto(contenido, `Razón social: ${pedido.razonSocial || "-"}`);
    }

    agregarTexto(
      contenido,
      `Entrega: ${pedido.metodoEntrega === "RECOJO" ? "Recojo en sede" : "Delivery"}`
    );

    if (pedido.metodoEntrega === "RECOJO") {
      const sedeTexto = pedido.sedeDistrito
        ? `${pedido.sede} (${pedido.sedeDireccion || ""} - ${pedido.sedeDistrito})`
        : pedido.sede;
      agregarTexto(contenido, `Sede: ${sedeTexto}`);
    } else {
      agregarTexto(contenido, `Dirección: ${pedido.direccionEntrega || ""}`);
      agregarTexto(contenido, `Distrito: ${pedido.distrito || ""}`);
      if (pedido.referencia) {
        agregarTexto(contenido, `Referencia: ${pedido.referencia}`);
      }
    }

    if (pedido.observacion) {
      agregarTexto(contenido, `Observación: ${pedido.observacion}`, "pedido-detalle-obs");
    }

    const titulo = document.createElement("h3");
    titulo.className = "pedido-modal-seccion";
    titulo.textContent = "Productos";
    contenido.appendChild(titulo);

    pedido.productos.forEach((producto) => {
      const fila = document.createElement("div");
      fila.className = "pedido-producto";
      agregarTexto(fila, `${producto.nombre} × ${producto.cantidad}`);
      agregarTexto(fila, formatoMoneda(producto.subtotal));
      contenido.appendChild(fila);
    });

    agregarTexto(contenido, `Subtotal: ${formatoMoneda(pedido.subtotal)}`, "pedido-total");
    agregarTexto(contenido, `Envío: ${formatoMoneda(pedido.costoEnvio)}`);
    agregarTexto(contenido, `Total: ${formatoMoneda(pedido.total)}`, "pedido-total");

    const btnPdf = document.getElementById("btnPdfDesdeDetalle");
    if (btnPdf) {
      if (pedido.tieneComprobante) {
        btnPdf.href = `/pedidos/${pedido.idPedido}/comprobante`;
        btnPdf.classList.remove("hidden");
      } else {
        btnPdf.classList.add("hidden");
      }
    }

    document.getElementById("modalDetalle").showModal();
  } catch (error) {
    alert(error.message);
  }
}

// =======================================
// PAGO ONLINE SIMULADO
// =======================================

function abrirPago(boton) {
  const id = boton.dataset.id;
  const total = boton.dataset.total;
  const metodo = boton.dataset.metodo;

  const form = document.getElementById("formPagarPedido");
  form.action = `/pedidos/${id}/pagar`;

  document.getElementById("pagoPedidoTitulo").textContent = `Pagar pedido #PF-${id}`;
  document.getElementById("pagoPedidoTotal").textContent = `S/ ${total}`;

  const vistaForm = document.getElementById("vistaFormularioPagoPedido");
  const vistaProc = document.getElementById("vistaProcesandoPagoPedido");
  vistaForm.hidden = false;
  vistaProc.hidden = true;

  const btnConfirmar = document.getElementById("btnConfirmarPagoPedido");
  btnConfirmar.disabled = false;

  const radioSeleccionado = form.querySelector(`input[name="metodoPago"][value="${metodo}"]`);
  if (radioSeleccionado) {
    radioSeleccionado.checked = true;
  }

  document.getElementById("modalPagoPedido").showModal();
}

// =======================================
// EDITAR PEDIDO
// =======================================

async function abrirEditar(id) {
  try {
    const pedido = await consultarPedido(id);

    if (pedido.estado !== "PENDIENTE" || pedido.estadoPago !== "PENDIENTE") {
      throw new Error("Este pedido ya no puede editarse.");
    }

    const formulario = document.getElementById("formEditarPedido");
    formulario.action = `/pedidos/${id}/editar`;

    const contenedor = document.getElementById("productosEditar");
    contenedor.replaceChildren();

    pedido.productos.forEach((producto) => {
      const fila = document.createElement("div");
      fila.className = "pedido-producto";

      const etiqueta = document.createElement("label");
      etiqueta.textContent = producto.nombre;

      const idProducto = document.createElement("input");
      idProducto.type = "hidden";
      idProducto.name = "idProducto";
      idProducto.value = producto.idProducto;

      const cantidad = document.createElement("input");
      cantidad.type = "number";
      cantidad.name = "cantidad";
      cantidad.min = "1";
      cantidad.max = "9999";
      cantidad.required = true;
      cantidad.value = producto.cantidad;
      cantidad.className = "field-input pedido-input-cantidad";

      fila.appendChild(etiqueta);
      fila.appendChild(idProducto);
      fila.appendChild(cantidad);

      contenedor.appendChild(fila);
    });

    document.getElementById("editarMetodoEntrega").value = pedido.metodoEntrega;
    document.getElementById("editarSede").value = pedido.idSede ?? "";
    document.getElementById("editarDireccion").value = pedido.direccionEntrega ?? "";
    document.getElementById("editarDistrito").value = pedido.distrito ?? "";
    document.getElementById("editarReferencia").value = pedido.referencia ?? "";

    actualizarEntregaEdicion();

    document.getElementById("modalEditar").showModal();
  } catch (error) {
    alert(error.message);
  }
}

function actualizarEntregaEdicion() {
  const metodo = document.getElementById("editarMetodoEntrega").value;
  const esRecojo = metodo === "RECOJO";

  document.getElementById("editarDatosSede").classList.toggle("hidden", !esRecojo);
  document.getElementById("editarDatosDelivery").classList.toggle("hidden", esRecojo);

  document.getElementById("editarSede").required = esRecojo;
  document.getElementById("editarDireccion").required = !esRecojo;
  document.getElementById("editarDistrito").required = !esRecojo;
}

// =======================================
// CANCELAR PEDIDO (CON O SIN REEMBOLSO)
// =======================================

function abrirCancelar(id, esPagado) {
  document.getElementById("formCancelarPedido").action = `/pedidos/${id}/cancelar`;

  const titulo = document.getElementById("tituloCancelar");
  const principal = document.getElementById("textoCancelarPrincipal");
  const detalle = document.getElementById("textoCancelarDetalle");
  const boton = document.getElementById("btnConfirmarCancelar");

  if (esPagado) {
    titulo.textContent = `Cancelar pedido #PF-${id}`;
    principal.textContent = "¿Deseas cancelar este pedido pagado y solicitar un reembolso?";
    detalle.textContent =
      "Los productos regresarán al inventario y tu pedido quedará en estado REEMBOLSO PENDIENTE para que administración procese la devolución simulada.";
    boton.textContent = "Sí, cancelar y solicitar reembolso";
  } else {
    titulo.textContent = `Cancelar pedido #PF-${id}`;
    principal.textContent = "¿Estás seguro de cancelar este pedido?";
    detalle.textContent =
      "El pedido quedará registrado como cancelado y los productos regresarán al inventario.";
    boton.textContent = "Sí, cancelar pedido";
  }

  document.getElementById("modalCancelar").showModal();
}

// =======================================
// EVENTOS DE FORMULARIOS
// =======================================

document.addEventListener("DOMContentLoaded", () => {
  ["formEditarPedido", "formCancelarPedido"].forEach((id) => {
    const formulario = document.getElementById(id);
    if (!formulario) return;

    formulario.addEventListener("submit", () => {
      const boton = formulario.querySelector('button[type="submit"]');
      if (boton) {
        boton.disabled = true;
        boton.textContent = "Procesando...";
      }
    });
  });

  const formPago = document.getElementById("formPagarPedido");
  if (formPago) {
    let enviandoPago = false;
    formPago.addEventListener("submit", (event) => {
      if (enviandoPago) {
        event.preventDefault();
        return;
      }
      event.preventDefault();
      enviandoPago = true;

      document.getElementById("btnConfirmarPagoPedido").disabled = true;
      document.getElementById("vistaFormularioPagoPedido").hidden = true;
      document.getElementById("vistaProcesandoPagoPedido").hidden = false;

      setTimeout(() => {
        formPago.submit();
      }, 900);
    });
  }
});
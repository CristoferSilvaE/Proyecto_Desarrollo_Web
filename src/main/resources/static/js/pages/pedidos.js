function cerrarModal(id) {
  document.getElementById(id).close();
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
// VER DETALLE
// =======================================

async function abrirDetalle(id) {
  try {
    const pedido = await consultarPedido(id);

    const contenido = document.getElementById("contenidoDetalle");

    contenido.replaceChildren();

    document.getElementById("tituloDetalle").textContent = `Pedido #PF-${pedido.idPedido}`;

    agregarTexto(contenido, `Estado: ${pedido.estado}`);
    agregarTexto(contenido, `Pago: ${pedido.estadoPago}`);
    agregarTexto(contenido, `Método de pago: ${pedido.metodoPago}`);
    agregarTexto(contenido, `Comprobante: ${pedido.tipoComprobante}`);
    agregarTexto(contenido, `Entrega: ${pedido.metodoEntrega}`);

    if (pedido.metodoEntrega === "RECOJO") {
      agregarTexto(contenido, `Sede: ${pedido.sede}`);
    } else {
      agregarTexto(contenido, `Dirección: ${pedido.direccionEntrega || ""}`);

      agregarTexto(contenido, `Distrito: ${pedido.distrito || ""}`);

      if (pedido.referencia) {
        agregarTexto(contenido, `Referencia: ${pedido.referencia}`);
      }
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

    document.getElementById("modalDetalle").showModal();
  } catch (error) {
    alert(error.message);
  }
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
// CANCELAR PEDIDO
// =======================================

function abrirCancelar(id) {
  document.getElementById("formCancelarPedido").action = `/pedidos/${id}/cancelar`;

  document.getElementById("modalCancelar").showModal();
}

// =======================================
// EVITAR ENVÍOS REPETIDOS
// =======================================

document.addEventListener("DOMContentLoaded", () => {
  ["formEditarPedido", "formCancelarPedido"].forEach((id) => {
    const formulario = document.getElementById(id);

    if (!formulario) {
      return;
    }

    formulario.addEventListener("submit", () => {
      const boton = formulario.querySelector('button[type="submit"]');

      if (boton) {
        boton.disabled = true;
        boton.textContent = "Procesando...";
      }
    });
  });
});

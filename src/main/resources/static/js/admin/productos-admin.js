function abrirNuevoProducto() {
  const form = document.getElementById("formProducto");
  form.reset();

  document.getElementById("productoId").value = "";
  document.getElementById("productoEstado").value = "ACTIVO";
  document.getElementById("productoStockMinimo").value = "5";

  document.getElementById("tituloModalProducto").textContent = "NUEVO PRODUCTO";
  document.getElementById("btnGuardarProducto").textContent = "REGISTRAR PRODUCTO";

  const previewWrap = document.getElementById("previewImagenWrap");
  previewWrap.hidden = true;
  document.getElementById("previewImagenImg").src = "";

  document.getElementById("modalProducto").showModal();
}

function abrirEditarProducto(boton) {
  const form = document.getElementById("formProducto");
  form.reset();

  document.getElementById("productoId").value = boton.dataset.id || "";
  document.getElementById("productoSku").value = boton.dataset.sku || "";
  document.getElementById("productoNombre").value = boton.dataset.nombre || "";
  document.getElementById("productoCategoria").value = boton.dataset.categoria || "Nutrición Deportiva";
  document.getElementById("productoPrecio").value = (boton.dataset.precio || "").replace(",", ".");
  document.getElementById("productoStock").value = boton.dataset.stock ?? "0";
  document.getElementById("productoStockMinimo").value = boton.dataset.stockMinimo ?? "5";
  document.getElementById("productoEstado").value = boton.dataset.estado || "ACTIVO";
  document.getElementById("productoDescripcion").value = boton.dataset.descripcion || "";

  document.getElementById("tituloModalProducto").textContent = "EDITAR PRODUCTO";
  document.getElementById("btnGuardarProducto").textContent = "GUARDAR CAMBIOS";

  const imagenActual = boton.dataset.imagen;
  const previewWrap = document.getElementById("previewImagenWrap");
  const previewImg = document.getElementById("previewImagenImg");
  const previewTexto = document.getElementById("previewImagenTexto");

  if (imagenActual) {
    previewImg.src = `/imagenes/${imagenActual}`;
    previewTexto.textContent = `Imagen actual: ${imagenActual}`;
    previewWrap.hidden = false;
  } else {
    previewImg.src = "";
    previewWrap.hidden = true;
  }

  document.getElementById("modalProducto").showModal();
}

function previsualizarImagenProducto(input) {
  const archivo = input.files && input.files[0];
  const previewWrap = document.getElementById("previewImagenWrap");
  const previewImg = document.getElementById("previewImagenImg");
  const previewTexto = document.getElementById("previewImagenTexto");

  if (!archivo) {
    return;
  }

  const lector = new FileReader();
  lector.onload = (evento) => {
    previewImg.src = evento.target.result;
    previewTexto.textContent = `Nueva imagen seleccionada: ${archivo.name}`;
    previewWrap.hidden = false;
  };
  lector.readAsDataURL(archivo);
}

function abrirEstadoProducto(boton) {
  const { id, nombre, estado } = boton.dataset;
  const desactivar = estado === "INACTIVO";

  document.getElementById("formEstadoProducto").action = `/admin/productos/${id}/estado`;
  document.getElementById("estadoProductoNuevo").value = estado;

  document.getElementById("tituloEstadoProducto").textContent = desactivar
    ? "DESACTIVAR PRODUCTO"
    : "ACTIVAR PRODUCTO";

  document.getElementById("textoEstadoProducto").textContent = desactivar
    ? `¿Estás seguro de desactivar "${nombre}"?`
    : `¿Deseas volver a activar "${nombre}"?`;

  document.getElementById("notaEstadoProducto").textContent = desactivar
    ? "El producto dejará de mostrarse en la tienda pública, pero se conservará en el historial de pedidos."
    : "El producto volverá a estar visible en la tienda pública si cuenta con stock disponible.";

  const btn = document.getElementById("btnConfirmarEstadoProducto");
  btn.textContent = desactivar ? "DESACTIVAR" : "ACTIVAR";
  btn.className = desactivar ? "adm-btn adm-btn-danger" : "adm-btn adm-btn-success";

  document.getElementById("modalEstadoProducto").showModal();
}
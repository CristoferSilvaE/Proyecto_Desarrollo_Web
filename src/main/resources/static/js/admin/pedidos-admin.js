function formatoMonedaAdmin(valor) {
    return `S/ ${Number(valor).toFixed(2)}`;
}

function formatearMetodoAdmin(metodo) {
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

function crearBloqueDetalle(etiqueta, valor) {
    const div = document.createElement("div");
    div.className = "adm-order-detail-item";

    const small = document.createElement("small");
    small.textContent = etiqueta;

    const strong = document.createElement("strong");
    strong.textContent = valor || "-";

    div.appendChild(small);
    div.appendChild(strong);
    return div;
}

async function abrirDetalleAdmin(id) {
    try {
        const respuesta = await fetch(`/admin/pedidos/${id}/detalle`);
        if (!respuesta.ok) {
            throw new Error("No se pudo cargar el detalle del pedido.");
        }

        const pedido = await respuesta.json();

        document.getElementById("admDetalleTitulo").textContent = `PEDIDO #PF-${pedido.idPedido}`;
        document.getElementById("admDetalleSubtitulo").textContent =
            `Registrado el ${pedido.fechaPedido || "-"}`;

        const contenedor = document.getElementById("admDetalleContenido");
        contenedor.replaceChildren();

        const grid = document.createElement("div");
        grid.className = "adm-order-detail-grid";

        grid.appendChild(crearBloqueDetalle("CLIENTE", pedido.clienteNombre));
        grid.appendChild(crearBloqueDetalle("DNI", pedido.clienteDni));
        grid.appendChild(crearBloqueDetalle("CORREO", pedido.clienteCorreo));
        grid.appendChild(crearBloqueDetalle("TELÉFONO", pedido.clienteTelefono || "No registrado"));
        grid.appendChild(crearBloqueDetalle("ESTADO DEL PEDIDO", pedido.estado));
        grid.appendChild(crearBloqueDetalle("ESTADO DEL PAGO", pedido.estadoPago));
        grid.appendChild(crearBloqueDetalle("MÉTODO DE PAGO", formatearMetodoAdmin(pedido.metodoPago)));
        grid.appendChild(crearBloqueDetalle("FECHA DE PAGO", pedido.fechaPago || "Sin pago"));
        grid.appendChild(crearBloqueDetalle("COMPROBANTE", pedido.tipoComprobante));

        if (pedido.tipoComprobante === "FACTURA") {
            grid.appendChild(crearBloqueDetalle("RUC", pedido.ruc));
            grid.appendChild(crearBloqueDetalle("RAZÓN SOCIAL", pedido.razonSocial));
        }

        if (pedido.metodoEntrega === "RECOJO") {
            const sede = pedido.sedeDistrito
                ? `${pedido.sede} (${pedido.sedeDireccion || ""} - ${pedido.sedeDistrito})`
                : pedido.sede;
            grid.appendChild(crearBloqueDetalle("ENTREGA (RECOJO EN SEDE)", sede));
        } else {
            const dir = [pedido.direccionEntrega, pedido.distrito, pedido.referencia]
                .filter(Boolean)
                .join(" | ");
            grid.appendChild(crearBloqueDetalle("ENTREGA (DELIVERY)", dir));
        }

        contenedor.appendChild(grid);

        if (pedido.observacion) {
            const nota = document.createElement("p");
            nota.className = "adm-note";
            nota.textContent = `Observación: ${pedido.observacion}`;
            contenedor.appendChild(nota);
        }

        const sec = document.createElement("div");
        sec.className = "adm-form-section";
        sec.textContent = "PRODUCTOS DEL PEDIDO";
        contenedor.appendChild(sec);

        const lista = document.createElement("div");
        lista.className = "adm-inscritos-list";

        pedido.productos.forEach((prod) => {
            const item = document.createElement("div");
            item.className = "adm-inscrito-item";

            const info = document.createElement("div");
            info.className = "adm-inscrito-info";

            const nombre = document.createElement("strong");
            nombre.textContent = prod.nombre;

            const meta = document.createElement("small");
            meta.textContent = `Cantidad: ${prod.cantidad}  ×  P. Unit.: ${formatoMonedaAdmin(prod.precio)}`;

            info.appendChild(nombre);
            info.appendChild(meta);

            const totalItem = document.createElement("strong");
            totalItem.textContent = formatoMonedaAdmin(prod.subtotal);

            item.appendChild(info);
            item.appendChild(totalItem);
            lista.appendChild(item);
        });

        contenedor.appendChild(lista);

        const resumen = document.createElement("div");
        resumen.className = "adm-order-totals";
        resumen.innerHTML = `
      <div><span>Subtotal</span><strong>${formatoMonedaAdmin(pedido.subtotal)}</strong></div>
      <div><span>Envío</span><strong>${formatoMonedaAdmin(pedido.costoEnvio)}</strong></div>
      <div class="adm-order-totals-final"><span>Total</span><strong>${formatoMonedaAdmin(pedido.total)}</strong></div>
    `;
        contenedor.appendChild(resumen);

        const btnPdf = document.getElementById("admDetalleBtnPdf");
        if (pedido.tieneComprobante) {
            btnPdf.href = `/admin/pedidos/${pedido.idPedido}/comprobante`;
            btnPdf.hidden = false;
        } else {
            btnPdf.hidden = true;
        }

        document.getElementById("modalDetallePedidoAdmin").showModal();
    } catch (error) {
        alert(error.message);
    }
}

function abrirConfirmarPagoAdmin(boton) {
    const { id, cliente, total } = boton.dataset;
    document.getElementById("formConfirmarPagoAdmin").action = `/admin/pedidos/${id}/confirmar-pago`;
    document.getElementById("admPagoTitulo").textContent = `CONFIRMAR PAGO #PF-${id}`;
    document.getElementById("admPagoTexto").textContent =
        `¿Confirmas el pago de S/ ${total} correspondiente al cliente ${cliente}?`;
    document.getElementById("modalConfirmarPagoPedido").showModal();
}

function abrirEntregarAdmin(boton) {
    const { id, cliente } = boton.dataset;
    document.getElementById("formEntregarAdmin").action = `/admin/pedidos/${id}/entregar`;
    document.getElementById("admEntregarTitulo").textContent = `ENTREGAR PEDIDO #PF-${id}`;
    document.getElementById("admEntregarTexto").textContent =
        `¿Confirmas que el pedido de ${cliente} fue entregado correctamente?`;
    document.getElementById("modalEntregarPedido").showModal();
}

function abrirCancelarAdmin(boton) {
    const id = boton.dataset.id;
    const esPagado = boton.dataset.pagado === "true";

    document.getElementById("formCancelarAdmin").action = `/admin/pedidos/${id}/cancelar`;
    document.getElementById("admCancelarTitulo").textContent = `CANCELAR PEDIDO #PF-${id}`;
    document.getElementById("admCancelarMotivo").value = "";

    const nota = document.getElementById("admCancelarNota");
    nota.textContent = esPagado
        ? "Este pedido ya fue pagado. Al cancelarlo se devolverá el stock al inventario y se generará un estado REEMBOLSO PENDIENTE."
        : "Al cancelar este pedido se devolverá el stock de todos los productos al inventario.";

    document.getElementById("modalCancelarPedidoAdmin").showModal();
}

function abrirReembolsoAdmin(boton) {
    const { id, cliente, total, metodo } = boton.dataset;
    document.getElementById("formReembolsoAdmin").action = `/admin/pedidos/${id}/reembolso`;
    document.getElementById("admReembolsoTitulo").textContent = `REEMBOLSO #PF-${id}`;
    document.getElementById("admReembolsoPedido").textContent = `#PF-${id}`;
    document.getElementById("admReembolsoCliente").textContent = cliente;
    document.getElementById("admReembolsoMetodo").textContent = formatearMetodoAdmin(metodo);
    document.getElementById("admReembolsoMonto").textContent = `S/ ${total}`;
    document.getElementById("modalReembolsoPedido").showModal();
}
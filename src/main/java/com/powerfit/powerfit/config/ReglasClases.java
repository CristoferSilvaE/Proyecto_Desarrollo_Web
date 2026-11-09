package com.powerfit.powerfit.config;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

public final class ReglasClases {

  // ==========================================
  // BENEFICIOS MENSUALES POR PLAN
  // ==========================================

  public static final int CLASES_GRATUITAS_BASICO = 0;

  public static final int CLASES_GRATUITAS_PREMIUM = 1;

  public static final int CLASES_GRATUITAS_ELITE = 2;

  // PRECIO FIJO DE CLASES ADICIONALES
  public static final BigDecimal PRECIO_CLASE_ADICIONAL = new BigDecimal("15.00");

  private ReglasClases() {
    // Evitar instancias de esta clase.
  }

  // ==========================================
  // OBTENER BENEFICIOS SEGUN EL PLAN
  // ==========================================

  public static int obtenerClasesGratuitas(String nombrePlan) {

    if (nombrePlan == null || nombrePlan.isBlank()) {
      return 0;
    }

    String nombreNormalizado =
        Normalizer.normalize(nombrePlan.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(Locale.ROOT);

    return switch (nombreNormalizado) {
      case "BASICO" -> CLASES_GRATUITAS_BASICO;

      case "PREMIUM" -> CLASES_GRATUITAS_PREMIUM;

      case "ELITE" -> CLASES_GRATUITAS_ELITE;

      default -> 0;
    };
  }
}

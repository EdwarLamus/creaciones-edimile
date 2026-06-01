package com.creacionesedimile.controller;

import com.creacionesedimile.service.ReporteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // ── Utilidad ─────────────────────────────────────────────────────────────

    private String defecto(String s, LocalDate d) {
        return (s == null || s.isBlank()) ? d.toString() : s;
    }

    // ── Hub de reportes ──────────────────────────────────────────────────────

    @GetMapping
    public String hub(@RequestParam(required = false) String desde,
                      @RequestParam(required = false) String hasta,
                      Model model) {
        LocalDate hoy   = LocalDate.now(ZONA);
        String    desdeS = defecto(desde, hoy.withDayOfMonth(1));
        String    hastaS = defecto(hasta, hoy);

        ReporteService.ResumenPeriodo resumen = reporteService.getResumenPeriodo(desdeS, hastaS);

        model.addAttribute("desde",   desdeS);
        model.addAttribute("hasta",   hastaS);
        model.addAttribute("resumen", resumen);
        return "reportes/index";
    }

    // ── Reporte detallado de ventas ──────────────────────────────────────────

    @GetMapping("/ventas")
    public String ventas(@RequestParam(required = false) String desde,
                         @RequestParam(required = false) String hasta,
                         Model model) {
        LocalDate hoy   = LocalDate.now(ZONA);
        String    desdeS = defecto(desde, hoy.withDayOfMonth(1));
        String    hastaS = defecto(hasta, hoy);

        ReporteService.ReporteVentasData data = reporteService.getReporteVentas(desdeS, hastaS);

        model.addAttribute("desde", desdeS);
        model.addAttribute("hasta", hastaS);
        model.addAttribute("data",  data);
        return "reportes/ventas";
    }

    // ── Reporte detallado de gastos ──────────────────────────────────────────

    @GetMapping("/gastos")
    public String gastos(@RequestParam(required = false) String desde,
                         @RequestParam(required = false) String hasta,
                         Model model) {
        LocalDate hoy   = LocalDate.now(ZONA);
        String    desdeS = defecto(desde, hoy.withDayOfMonth(1));
        String    hastaS = defecto(hasta, hoy);

        ReporteService.ReporteGastosData data = reporteService.getReporteGastos(desdeS, hastaS);

        model.addAttribute("desde", desdeS);
        model.addAttribute("hasta", hastaS);
        model.addAttribute("data",  data);
        return "reportes/gastos";
    }

    // ── Resumen del período: Ingresos vs Egresos ─────────────────────────────

    @GetMapping("/resumen")
    public String resumen(@RequestParam(required = false) String desde,
                          @RequestParam(required = false) String hasta,
                          Model model) {
        LocalDate hoy   = LocalDate.now(ZONA);
        String    desdeS = defecto(desde, hoy.withDayOfMonth(1));
        String    hastaS = defecto(hasta, hoy);

        ReporteService.ResumenPeriodo resumen = reporteService.getResumenPeriodo(desdeS, hastaS);

        model.addAttribute("desde",   desdeS);
        model.addAttribute("hasta",   hastaS);
        model.addAttribute("resumen", resumen);
        return "reportes/resumen";
    }
}

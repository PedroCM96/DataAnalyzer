/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arquitectura.comparadorMain;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author pedro
 */
@WebServlet(name = "DataAnalyzer", urlPatterns = {"/DataAnalyzer"})

public class DataAnalyzer extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    HashMap<Integer, String> hmAlumnos = new HashMap<>();

    //Para Analizar datos
    //Examenes Trimestres
    HashMap<Integer, String> hmNombreExamenesT1 = new HashMap<>();
    HashMap<Integer, String> hmNombreExamenesT2 = new HashMap<>();
    HashMap<Integer, String> hmNombreExamenesT3 = new HashMap<>();

    //Notas Trimestres
    ListMultimap<Integer, Double> mmNotasT1 = ArrayListMultimap.create();
    ListMultimap<Integer, Double> mmNotasT2 = ArrayListMultimap.create();
    ListMultimap<Integer, Double> mmNotasT3 = ArrayListMultimap.create();
    HashMap<Integer, Integer> hmCoords = new HashMap<>();

    //Notas todo el curso
    ListMultimap<Integer, Double> mmNotasCurso = ArrayListMultimap.create();
    //Estadisticas
    //Datos Individuales --------------------------------------
    //Notas por trimestre
    HashMap<Integer, Double> notaMediaTrimestre1 = new HashMap<>();
    HashMap<Integer, Double> notaMediaTrimestre2 = new HashMap<>();
    HashMap<Integer, Double> notaMediaTrimestre3 = new HashMap<>();

    //Notas medias del curso
    HashMap<Integer, Double> notaMediaCurso = new HashMap<>();

    //Todos los nombres de los examanes
    HashMap<Integer, String> hmNombreExamenesCurso = new HashMap<>();

    //Datos colectivos -----------------------------------------
    //Notas medias por examen
    HashMap<Integer, Double> notaMediaPorExamenT1 = new HashMap<>();
    HashMap<Integer, Double> notaMediaPorExamenT2 = new HashMap<>();
    HashMap<Integer, Double> notaMediaPorExamenT3 = new HashMap<>();

    //Nota media del trimestre en general
    double notaMediaT1Colectivo = 0;
    double notaMediaT2Colectivo = 0;
    double notaMediaT3Colectivo = 0;

    //Nota media del curso en general
    double notaMediaCursoColectivo = 0;

    int numeroFilas = 0;
    int numeroCeldas = 0;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String origen = request.getParameter("origen");

        List<Part> fileParts = request.getParts().stream().filter(part -> "file1".equals(part.getName())).collect(Collectors.toList()); // Retrieves <input type="file" name="file" multiple="true">

        analizaDatosTrimestrales(fileParts.get(0));

        //Calculos datos individuales -----------------------------------------------------------------
        //Calculamos media de todos los alumnos por cada trimestre
        notaMediaTrimestre1 = calculaNotaMediaTrimestre(hmAlumnos, mmNotasT1);
        notaMediaTrimestre2 = calculaNotaMediaTrimestre(hmAlumnos, mmNotasT2);
        notaMediaTrimestre3 = calculaNotaMediaTrimestre(hmAlumnos, mmNotasT3);

        //Calculamos media del curso de todos los alumnos
        notaMediaCurso = calculaNotaMediaCurso(hmAlumnos, mmNotasT1, mmNotasT2, mmNotasT3);

        //Recogemos las notas de todo el curso de todos los alumnos
        //Recogemos todos los nombre de los examenes
        //Recogemos todos los nombres de los examenes del curso
        //Calculos datos colectivos  -----------------------------------------------------------------
        //Calculamos notas medias por examen
        notaMediaPorExamenT1 = calculaNotaMediaPorExamenTrimestre(hmNombreExamenesT1, mmNotasT1, hmAlumnos);
        notaMediaPorExamenT2 = calculaNotaMediaPorExamenTrimestre(hmNombreExamenesT2, mmNotasT2, hmAlumnos);
        notaMediaPorExamenT3 = calculaNotaMediaPorExamenTrimestre(hmNombreExamenesT3, mmNotasT3, hmAlumnos);

        //Calculamos nota media del trimestre colectivo
        notaMediaT1Colectivo = calculaNotaMediaTrimestreColectivo(notaMediaTrimestre1);
        notaMediaT2Colectivo = calculaNotaMediaTrimestreColectivo(notaMediaTrimestre2);
        notaMediaT3Colectivo = calculaNotaMediaTrimestreColectivo(notaMediaTrimestre3);

        //Calculamos nota media del curso colectivo
        notaMediaCursoColectivo = calculaNotaMediaCursoColectivo(notaMediaT1Colectivo, notaMediaT2Colectivo, notaMediaT3Colectivo);

        hmNombreExamenesCurso = dameNombreExamenesCurso(hmNombreExamenesT1, hmNombreExamenesT2, hmNombreExamenesT3);
        mmNotasCurso = dameTodasNotasCurso(mmNotasT1, mmNotasT2, mmNotasT3);

        //Pintar página
        try (PrintWriter out = response.getWriter()) {

            /* TODO output your page here. You may use following sample code. */
            out.println("   <!DOCTYPE html>                                                                                                                                                                               ");
            out.println("   <html lang='en' class='no-js'>                                                                                                                                                                ");
            out.println("   	<head>                                                                                                                                                                                      ");
            out.println("   		<meta charset='UTF-8' />                                                                                                                                                                ");
            out.println("   		<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>                                                                                                                          ");
            out.println("   		<meta name='viewport' content='width=device-width, initial-scale=1.0'>                                                                                                                  ");
            out.println("   		<title>Data Analyzer</title>                                                                                                                                                  ");
            out.println("   		<meta name='description' content='Blueprint: Tooltip Menu' />                                                                                                                           ");
            out.println("   		<meta name='keywords' content='Tooltip Menu, navigation, tooltip, menu, css, web development, template' />                                                                              ");
            out.println("   		<meta name='author' content='Codrops' />                                                                                                                                                ");
            out.println("   		<link rel='shortcut icon' href='../favicon.ico'>                                                                                                                                        ");
            out.println("   		<link rel='stylesheet' type='text/css' href='css/default.css' />                                                                                                                        ");
            out.println("   		<link rel='stylesheet' type='text/css' href='css/component.css' />                                                                                                                      ");
            out.println("   		<script src='js/modernizr.custom.js'></script>                                                                                                                                          ");
            out.println("   		<script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>                                                                                                 ");
            out.println("   		<script type='text/javascript'>                                                                                                                                                         ");

            //TABLA RESUMEN DEL CURSO POR ALUMNO -------------------------------------------------------------------------------
            for (int i = 1; i < hmAlumnos.size() + 1; i++) {
                out.println("   		  google.charts.load('current', {'packages':['table']});                                                                                                                                ");

                out.println("   		  google.charts.setOnLoadCallback(drawTableT1" + i + ");                                                                                                                                           ");
                out.println("   	                                                                                                                                                                                            ");

                out.println("   		  function drawTableT1" + i + "() {                                                                                                                                                                ");
                out.println("   			var data = new google.visualization.DataTable();                                                                                                                                    ");
                out.println("   			data.addColumn('string', 'Exámen');                                                                                                                                             ");
                out.println("   			data.addColumn('number', 'Nota');                                                                                                                                                   ");
                out.println("   			data.addColumn('boolean', 'Aprobado');                                                                                                                                              ");
                out.println("   			data.addRows([                                                                                                                                                                      ");
                out.println("   			  ['Nota media Trimestre 1', {v:" + notaMediaTrimestre1.get(i) + ", f: '" + notaMediaTrimestre1.get(i) + "'}, true],                                                                                                                             ");
                out.println("   			  ['Nota media Trimestre 2', {v:" + notaMediaTrimestre2.get(i) + ", f: '" + notaMediaTrimestre2.get(i) + "'}, true],                                                                                                                             ");
                out.println("   			  ['Nota media Trimestre 3', {v:" + notaMediaTrimestre3.get(i) + ", f: '" + notaMediaTrimestre3.get(i) + "'}, true],                                                                                                                             ");
                out.println("   			  ['Nota media curso', {v:" + notaMediaCurso.get(i) + ", f: '" + notaMediaCurso.get(i) + "'}, true],                                                                                                                             ");
                out.println("   			]);                                                                                                                                                                                 ");
                out.println("   	                                                                                                                                                                                            ");
                out.println("   			var table = new google.visualization.Table(document.getElementById('table_divT1" + i + "'));                                                                                                   ");
                out.println("   	                                                                                                                                                                                            ");
                out.println("   			table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});                                                                                                             ");
                out.println("   		  }                                                                                                                                                                                     ");

            }
// TABLA RESUMEN T1 COLECTIVO ---------------------------------------------------------------------------------------------------
            out.println("   		  google.charts.load('current', {'packages':['table']});                                                                                                                                ");
            out.println("   		  google.charts.setOnLoadCallback(drawTableResumenT1);                                                                                                                                           ");
            out.println("   	                                                                                                                                                                                            ");

            out.println("   		  function drawTableResumenT1() {                                                                                                                                                                ");
            out.println("   			var data = new google.visualization.DataTable();                                                                                                                                    ");
            out.println("   			data.addColumn('string', 'Exámen');                                                                                                                                             ");
            out.println("   			data.addColumn('number', 'Nota colectiva');                                                                                                                                                   ");
            out.println("   			data.addColumn('boolean', 'Aprobado');                                                                                                                                              ");
            out.println("   			data.addRows([                                                                                                                                                                      ");
            for (int i = 1; i < hmNombreExamenesT1.size() + 1; i++) {
                out.println("   			  ['" + hmNombreExamenesT1.get(i) + "', {v:" + notaMediaPorExamenT1.get(i) + ", f: '" + notaMediaPorExamenT1.get(i) + "'}, true],                                                                                                                             ");
            }
            out.println("   			]);                                                                                                                                                                                 ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			var table = new google.visualization.Table(document.getElementById('drawTableResumenT1'));                                                                                                   ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});                                                                                                             ");
            out.println("   		  }                                                                                                                                                                                     ");

// TABLA RESUMEN T2 COLECTIVO ---------------------------------------------------------------------------------------------------
            out.println("   		  google.charts.load('current', {'packages':['table']});                                                                                                                                ");
            out.println("   		  google.charts.setOnLoadCallback(drawTableResumenT2);                                                                                                                                           ");
            out.println("   	                                                                                                                                                                                            ");

            out.println("   		  function drawTableResumenT2() {                                                                                                                                                                ");
            out.println("   			var data = new google.visualization.DataTable();                                                                                                                                    ");
            out.println("   			data.addColumn('string', 'Exámen');                                                                                                                                             ");
            out.println("   			data.addColumn('number', 'Nota colectiva');                                                                                                                                                   ");
            out.println("   			data.addColumn('boolean', 'Aprobado');                                                                                                                                              ");
            out.println("   			data.addRows([                                                                                                                                                                      ");
            for (int i = 1; i < hmNombreExamenesT2.size() + 1; i++) {
                out.println("   			  ['" + hmNombreExamenesT2.get(i) + "', {v:" + notaMediaPorExamenT2.get(i) + ", f: '" + notaMediaPorExamenT2.get(i) + "'}, true],                                                                                                                             ");
            }
            out.println("   			]);                                                                                                                                                                                 ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			var table = new google.visualization.Table(document.getElementById('drawTableResumenT2'));                                                                                                   ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});                                                                                                             ");
            out.println("   		  }                                                                                                                                                                                     ");

// TABLA RESUMEN T3 COLECTIVO ---------------------------------------------------------------------------------------------------
            out.println("   		  google.charts.load('current', {'packages':['table']});                                                                                                                                ");
            out.println("   		  google.charts.setOnLoadCallback(drawTableResumenT3);                                                                                                                                           ");
            out.println("   	                                                                                                                                                                                            ");

            out.println("   		  function drawTableResumenT3() {                                                                                                                                                                ");
            out.println("   			var data = new google.visualization.DataTable();                                                                                                                                    ");
            out.println("   			data.addColumn('string', 'Exámen');                                                                                                                                             ");
            out.println("   			data.addColumn('number', 'Nota colectiva');                                                                                                                                                   ");
            out.println("   			data.addColumn('boolean', 'Aprobado');                                                                                                                                              ");
            out.println("   			data.addRows([                                                                                                                                                                      ");
            for (int i = 1; i < hmNombreExamenesT3.size() + 1; i++) {
                out.println("   			  ['" + hmNombreExamenesT3.get(i) + "', {v:" + notaMediaPorExamenT3.get(i) + ", f: '" + notaMediaPorExamenT3.get(i) + "'}, true],                                                                                                                             ");
            }
            out.println("   			]);                                                                                                                                                                                 ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			var table = new google.visualization.Table(document.getElementById('drawTableResumenT3'));                                                                                                   ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});                                                                                                             ");
            out.println("   		  }                                                                                                                                                                                     ");

            // TABLA RESUMEN NOTAS CURSO COLECTIVO ---------------------------------------------------------------------------------------------------
            out.println("   		  google.charts.load('current', {'packages':['table']});                                                                                                                                ");
            out.println("   		  google.charts.setOnLoadCallback(drawTableResumenNotasColectivo);                                                                                                                                           ");
            out.println("   	                                                                                                                                                                                            ");

            out.println("   		  function drawTableResumenNotasColectivo() {                                                                                                                                                                ");
            out.println("   			var data = new google.visualization.DataTable();                                                                                                                                    ");
            out.println("   			data.addColumn('string', 'Exámen');                                                                                                                                             ");
            out.println("   			data.addColumn('string', 'Trimestre');                                                                                                                                             ");

            out.println("   			data.addColumn('number', 'Nota colectiva');                                                                                                                                                   ");
            out.println("   			data.addColumn('boolean', 'Aprobado');                                                                                                                                              ");
            out.println("   			data.addRows([                                                                                                                                                                      ");
            for (int i = 1; i < hmNombreExamenesT1.size() + 1; i++) {
                out.println("   			  ['" + hmNombreExamenesT1.get(i) + "', 'Trimestre 1', {v:" + notaMediaPorExamenT1.get(i) + ", f: '" + notaMediaPorExamenT1.get(i) + "'}, true],                                                                                                                             ");
            }

            for (int i = 1; i < hmNombreExamenesT2.size() + 1; i++) {
                out.println("   			  ['" + hmNombreExamenesT2.get(i) + "', 'Trimestre 2', {v:" + notaMediaPorExamenT2.get(i) + ", f: '" + notaMediaPorExamenT2.get(i) + "'}, true],                                                                                                                             ");
            }

            for (int i = 1; i < hmNombreExamenesT3.size() + 1; i++) {
                out.println("   			  ['" + hmNombreExamenesT3.get(i) + "', 'Trimestre 3', {v:" + notaMediaPorExamenT3.get(i) + ", f: '" + notaMediaPorExamenT3.get(i) + "'}, true],                                                                                                                             ");
            }
            out.println("   			]);                                                                                                                                                                                 ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			var table = new google.visualization.Table(document.getElementById('drawTableResumenNotasColectivo'));                                                                                                   ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});                                                                                                             ");
            out.println("   		  }                                                                                                                                                                                     ");

//TABLA RESUMEN DEL CURSO COLECTIVO -------------------------------------------------------------------------------
            out.println("   		  google.charts.load('current', {'packages':['table']});                                                                                                                                ");

            out.println("   		  google.charts.setOnLoadCallback(drawTableResumenCurso);                                                                                                                                           ");
            out.println("   	                                                                                                                                                                                            ");

            out.println("   		  function drawTableResumenCurso() {                                                                                                                                                                ");
            out.println("   			var data = new google.visualization.DataTable();                                                                                                                                    ");
            out.println("   			data.addColumn('string', 'Nota Media');                                                                                                                                             ");
            out.println("   			data.addColumn('number', 'Nota');                                                                                                                                                   ");
            out.println("   			data.addColumn('boolean', 'Aprobado');                                                                                                                                              ");
            out.println("   			data.addRows([                                                                                                                                                                      ");
            out.println("   			  ['Nota media Trimestre 1', {v:" + notaMediaT1Colectivo + ", f: '" + notaMediaT1Colectivo + "'}, true],                                                                                                                             ");
            out.println("   			  ['Nota media Trimestre 2', {v:" + notaMediaT2Colectivo + ", f: '" + notaMediaT2Colectivo + "'}, true],                                                                                                                             ");
            out.println("   			  ['Nota media Trimestre 3', {v:" + notaMediaT3Colectivo + ", f: '" + notaMediaT3Colectivo + "'}, true],                                                                                                                             ");
            out.println("   			  ['Nota media curso', {v:" + notaMediaCursoColectivo + ", f: '" + notaMediaCursoColectivo + "'}, true],                                                                                                                             ");
            out.println("   			]);                                                                                                                                                                                 ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			var table = new google.visualization.Table(document.getElementById('table_divResumenCurso'));                                                                                                   ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("   			table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});                                                                                                             ");
            out.println("   		  }                                                                                                                                                                                     ");

            out.println("                                                                                                                                                                                                 ");
            out.println("   	google.charts.load('current', { 'packages': ['corechart'] });                                                                                                                               ");

            for (int i = 1; i < hmAlumnos.size() + 1; i++) {
                out.println("      google.charts.setOnLoadCallback(drawChart" + i + " );                                                                                                                                                ");
                out.println("                                                                                                                                                                                                 ");

                out.println("      function drawChart" + i + "() {                                                                                                                                                                     ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
                out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
                out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
                out.println("        data.addRows([                                                                                                                                                                           ");
                List<Double> notaAlumnoT1 = mmNotasT1.get(i);

                for (int x = 0; x < hmNombreExamenesT1.size(); x++) {
                    out.println("          ['" + hmNombreExamenesT1.get(x + 1) + "', " + notaAlumnoT1.get(x) + " ],                                                                                                                                                                  ");

                }
                out.println("        ]);                                                                                                                                                                                      ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var options = {                                                                                                                                                                          ");
                out.println("          'title': 'Nota',                                                                                                                                                                       ");
                out.println("          'width': 1000,                                                                                                                                                                         ");
                out.println("          'height': 400,                                                                                                                                                                         ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        };                                                                                                                                                                                       ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var chart = new google.visualization.LineChart(document.getElementById('chart" + i + "'));                                                                                                        ");
                out.println("        chart.draw(data, options);                                                                                                                                                               ");
                out.println("      }                                                                                                                                                                                          ");
            }
//GRAFICIA INDIVIDUAL TRIMESTRE 2 ----------------------------------------------------------------------------------------------

            for (int i = 1; i < hmAlumnos.size() + 1; i++) {
                out.println("      google.charts.setOnLoadCallback(drawChartT2" + i + " );                                                                                                                                                ");
                out.println("                                                                                                                                                                                                 ");

                out.println("      function drawChartT2" + i + "() {                                                                                                                                                                     ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
                out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
                out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
                out.println("        data.addRows([                                                                                                                                                                           ");
                List<Double> notaAlumnoT2 = mmNotasT2.get(i);
                for (int x = 0; x < hmNombreExamenesT2.size(); x++) {
                    out.println("          ['" + hmNombreExamenesT2.get(x + 1) + "', " + notaAlumnoT2.get(x) + " ],                                                                                                                                                                  ");

                }
                out.println("        ]);                                                                                                                                                                                      ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var options = {                                                                                                                                                                          ");
                out.println("          'title': 'Nota',                                                                                                                                                                       ");
                out.println("          'width': 1000,                                                                                                                                                                         ");
                out.println("          'height': 400,                                                                                                                                                                         ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        };                                                                                                                                                                                       ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var chart = new google.visualization.LineChart(document.getElementById('chartT2" + i + "'));                                                                                                        ");
                out.println("        chart.draw(data, options);                                                                                                                                                               ");
                out.println("      }                                                                                                                                                                                          ");
            }
            //GRAFICIA INDIVIDUAL TRIMESTRE 3 ----------------------------------------------------------------------------------------------

            for (int i = 1; i < hmAlumnos.size() + 1; i++) {
                out.println("      google.charts.setOnLoadCallback(drawChartT3" + i + " );                                                                                                                                                ");
                out.println("                                                                                                                                                                                                 ");

                out.println("      function drawChartT3" + i + "() {                                                                                                                                                                     ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
                out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
                out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
                out.println("        data.addRows([                                                                                                                                                                           ");
                List<Double> notaAlumnoT3 = mmNotasT3.get(i);
                for (int x = 0; x < hmNombreExamenesT3.size(); x++) {
                    out.println("          ['" + hmNombreExamenesT3.get(x + 1) + "', " + notaAlumnoT3.get(x) + " ],                                                                                                                                                                  ");

                }
                out.println("        ]);                                                                                                                                                                                      ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var options = {                                                                                                                                                                          ");
                out.println("          'title': 'Nota',                                                                                                                                                                       ");
                out.println("          'width': 1000,                                                                                                                                                                         ");
                out.println("          'height': 400,                                                                                                                                                                         ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        };                                                                                                                                                                                       ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var chart = new google.visualization.LineChart(document.getElementById('chartT3" + i + "'));                                                                                                        ");
                out.println("        chart.draw(data, options);                                                                                                                                                               ");
                out.println("      }                                                                                                                                                                                          ");
            }
            //GRAFICIA INDIVIDUAL CURSO ----------------------------------------------------------------------------------------------

            for (int i = 1; i < hmAlumnos.size() + 1; i++) {
                out.println("      google.charts.setOnLoadCallback(drawChartC" + i + " );                                                                                                                                                ");
                out.println("                                                                                                                                                                                                 ");

                out.println("      function drawChartC" + i + "() {                                                                                                                                                                     ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
                out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
                out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
                out.println("        data.addRows([                                                                                                                                                                           ");
                List<Double> notaAlumnoCurso = mmNotasCurso.get(i);
                for (int x = 0; x < hmNombreExamenesCurso.size(); x++) {
                    out.println("          ['" + hmNombreExamenesCurso.get(x + 1) + "', " + notaAlumnoCurso.get(x) + " ],                                                                                                                                                                  ");

                }
                out.println("        ]);                                                                                                                                                                                      ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var options = {                                                                                                                                                                          ");
                out.println("          'title': 'Nota',                                                                                                                                                                       ");
                out.println("          'width': 1000,                                                                                                                                                                         ");
                out.println("          'height': 400,                                                                                                                                                                         ");
                out.println("           vAxis: { ");
                out.println("           minValue: 0,");
                out.println("           textPosition: 'in',");

                out.println("           maxValue: 10,");

                out.println("               },");
                out.println("           hAxis: {");
                out.println("               slantedTextAngle: 90,");
                out.println("               slantedText: true,");

                out.println("               maxTextLines: 100,");
                out.println("               textStyle: {");
                out.println("               fontSize: 10,");
                out.println("                       }");  // or the number you want}
                out.println("               },");

                out.println("                                                                                                                                                                                                 ");
                out.println("        };                                                                                                                                                                                       ");
                out.println("                                                                                                                                                                                                 ");
                out.println("        var chart = new google.visualization.LineChart(document.getElementById('chartC" + i + "'));                                                                                                        ");
                out.println("        chart.draw(data, options);                                                                                                                                                               ");
                out.println("      }                                                                                                                                                                                          ");
            }

            //GRAFICA COLECTIVA TRIMESTRE 1 ------------------------------------------------------------------------------------------------------
            out.println("      google.charts.setOnLoadCallback(drawChartColect1);                                                                                                                                                ");
            out.println("                                                                                                                                                                                                 ");

            out.println("      function drawChartColect1() {                                                                                                                                                                     ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
            out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
            out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
            out.println("        data.addRows([                                                                                                                                                                           ");

            for (int x = 0; x < hmNombreExamenesT1.size(); x++) {
                out.println("          ['" + hmNombreExamenesCurso.get(x + 1) + "', " + notaMediaPorExamenT1.get(x + 1) + " ],                                                                                                                                                                  ");

            }
            out.println("        ]);                                                                                                                                                                                      ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var options = {                                                                                                                                                                          ");
            out.println("          'title': 'Nota',                                                                                                                                                                       ");
            out.println("          'width': 1000,                                                                                                                                                                         ");
            out.println("          'height': 400,                                                                                                                                                                         ");
            out.println("           vAxis: { ");
            out.println("           minValue: 0,");
            out.println("           textPosition: 'in',");

            out.println("           maxValue: 10,");

            out.println("               },");
            out.println("           hAxis: {");
            out.println("               slantedTextAngle: 90,");
            out.println("               slantedText: false,");

            out.println("               maxTextLines: 100,");
            out.println("               textStyle: {");
            out.println("               fontSize: 10,");
            out.println("                       }");  // or the number you want}
            out.println("               },");

            out.println("                                                                                                                                                                                                 ");
            out.println("        };                                                                                                                                                                                       ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var chart = new google.visualization.LineChart(document.getElementById('drawChartColect1'));                                                                                                        ");
            out.println("        chart.draw(data, options);                                                                                                                                                               ");
            out.println("      }                                                                                                                                                                                          ");
            //GRAFICA COLECTIVA TRIMESTRE 2 ------------------------------------------------------------------------------------------------------
            out.println("      google.charts.setOnLoadCallback(drawChartColect2);                                                                                                                                                ");
            out.println("                                                                                                                                                                                                 ");

            out.println("      function drawChartColect2() {                                                                                                                                                                     ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
            out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
            out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
            out.println("        data.addRows([                                                                                                                                                                           ");

            for (int x = 0; x < hmNombreExamenesT2.size(); x++) {
                out.println("          ['" + hmNombreExamenesT2.get(x + 1) + "', " + notaMediaPorExamenT2.get(x + 1) + " ],                                                                                                                                                                  ");

            }
            out.println("        ]);                                                                                                                                                                                      ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var options = {                                                                                                                                                                          ");
            out.println("          'title': 'Nota',                                                                                                                                                                       ");
            out.println("          'width': 1000,                                                                                                                                                                         ");
            out.println("          'height': 400,                                                                                                                                                                         ");
            out.println("           vAxis: { ");
            out.println("           minValue: 0,");
            out.println("           textPosition: 'in',");

            out.println("           maxValue: 10,");

            out.println("               },");
            out.println("           hAxis: {");
            out.println("               slantedTextAngle: 90,");
            out.println("               slantedText: false,");

            out.println("               maxTextLines: 100,");
            out.println("               textStyle: {");
            out.println("               fontSize: 10,");
            out.println("                       }");  // or the number you want}
            out.println("               },");

            out.println("                                                                                                                                                                                                 ");
            out.println("        };                                                                                                                                                                                       ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var chart = new google.visualization.LineChart(document.getElementById('drawChartColect2'));                                                                                                        ");
            out.println("        chart.draw(data, options);                                                                                                                                                               ");
            out.println("      }                                                                                                                                                                                          ");

//GRAFICA COLECTIVA TRIMESTRE 3 ------------------------------------------------------------------------------------------------------
            out.println("      google.charts.setOnLoadCallback(drawChartColect3);                                                                                                                                                ");
            out.println("                                                                                                                                                                                                 ");

            out.println("      function drawChartColect3() {                                                                                                                                                                     ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
            out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
            out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
            out.println("        data.addRows([                                                                                                                                                                           ");

            for (int x = 0; x < hmNombreExamenesT3.size(); x++) {
                out.println("          ['" + hmNombreExamenesT3.get(x + 1) + "', " + notaMediaPorExamenT3.get(x + 1) + " ],                                                                                                                                                                  ");

            }
            out.println("        ]);                                                                                                                                                                                      ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var options = {                                                                                                                                                                          ");
            out.println("          'title': 'Nota',                                                                                                                                                                       ");
            out.println("          'width': 1000,                                                                                                                                                                         ");
            out.println("          'height': 400,                                                                                                                                                                         ");
            out.println("           vAxis: { ");
            out.println("           minValue: 0,");
            out.println("           textPosition: 'in',");

            out.println("           maxValue: 10,");

            out.println("               },");
            out.println("           hAxis: {");
            out.println("               slantedTextAngle: 90,");
            out.println("               slantedText: false,");

            out.println("               maxTextLines: 100,");
            out.println("               textStyle: {");
            out.println("               fontSize: 10,");
            out.println("                       }");  // or the number you want}
            out.println("               },");

            out.println("                                                                                                                                                                                                 ");
            out.println("        };                                                                                                                                                                                       ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var chart = new google.visualization.LineChart(document.getElementById('drawChartColect3'));                                                                                                        ");
            out.println("        chart.draw(data, options);                                                                                                                                                               ");
            out.println("      }                                                                                                                                                                                          ");
//GRAFICA COLECTIVA CURSO ------------------------------------------------------------------------------------------------------
            out.println("      google.charts.setOnLoadCallback(drawChartColectCurso);                                                                                                                                                ");
            out.println("                                                                                                                                                                                                 ");

            out.println("      function drawChartColectCurso() {                                                                                                                                                                     ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var data = new google.visualization.DataTable();                                                                                                                                         ");
            out.println("        data.addColumn('string', 'Topping');                                                                                                                                                     ");
            out.println("        data.addColumn('number', 'Nota');                                                                                                                                                        ");
            out.println("        data.addRows([                                                                                                                                                                           ");
            for (int x = 0; x < hmNombreExamenesT1.size(); x++) {
                out.println("          ['" + hmNombreExamenesT1.get(x + 1) + "', " + notaMediaPorExamenT1.get(x + 1) + " ],                                                                                                                                                                  ");

            }

            for (int x = 0; x < hmNombreExamenesT2.size(); x++) {
                out.println("          ['" + hmNombreExamenesT2.get(x + 1) + "', " + notaMediaPorExamenT2.get(x + 1) + " ],                                                                                                                                                                  ");

            }
            for (int x = 0; x < hmNombreExamenesT3.size(); x++) {
                out.println("          ['" + hmNombreExamenesT3.get(x + 1) + "', " + notaMediaPorExamenT3.get(x + 1) + " ],                                                                                                                                                                  ");

            }
            out.println("        ]);                                                                                                                                                                                      ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var options = {                                                                                                                                                                          ");
            out.println("          'title': 'Nota',                                                                                                                                                                       ");
            out.println("          'width': 1000,                                                                                                                                                                         ");
            out.println("          'height': 400,                                                                                                                                                                         ");
            out.println("           vAxis: { ");
            out.println("           minValue: 0,");
            out.println("           textPosition: 'in',");

            out.println("           maxValue: 10,");

            out.println("               },");
            out.println("           hAxis: {");
            out.println("               slantedTextAngle: 90,");
            out.println("               slantedText: true,");

            out.println("               maxTextLines: 100,");
            out.println("               textStyle: {");
            out.println("               fontSize: 10,");
            out.println("                       }");  // or the number you want}
            out.println("               },");

            out.println("                                                                                                                                                                                                 ");
            out.println("        };                                                                                                                                                                                       ");
            out.println("                                                                                                                                                                                                 ");
            out.println("        var chart = new google.visualization.LineChart(document.getElementById('drawChartColectCurso'));                                                                                                        ");
            out.println("        chart.draw(data, options);                                                                                                                                                               ");
            out.println("      }                                                                                                                                                                                          ");

            // ------------------------------------------------------------------------------------------------------------
            out.println("   		                                                                                                                                                                                        ");
            out.println("   		</script>                                                                                                                                                                               ");
            out.println("   	                                                                                                                                                                                            ");
            out.println("                                                                                                                                                                                                 ");
            out.println("   		<style>                                                                                                                                                                                 ");
            out.println("   			.accordion {                                                                                                                                                                        ");
            out.println("   			  background-color: #eee;                                                                                                                                                           ");
            out.println("   			  color: #444;                                                                                                                                                                      ");
            out.println("   			  cursor: pointer;                                                                                                                                                                  ");
            out.println("   			  padding: 18px;                                                                                                                                                                    ");
            out.println("   			  width: 100%;                                                                                                                                                                      ");
            out.println("   			  border: none;                                                                                                                                                                     ");
            out.println("   			  text-align: left;                                                                                                                                                                 ");
            out.println("   			  outline: none;                                                                                                                                                                    ");
            out.println("   			  font-size: 15px;                                                                                                                                                                  ");
            out.println("   			  transition: 0.4s;                                                                                                                                                                 ");
            out.println("   			}                                                                                                                                                                                   ");
            out.println("   																				                                                                                                                ");
            out.println("   			.active, .accordion:hover {                                                                                                                                                         ");
            out.println("   			  background-color: #ccc;                                                                                                                                                           ");
            out.println("   			}                                                                                                                                                                                   ");
            out.println("   																				                                                                                                                ");
            out.println("   			.panel {                                                                                                                                                                            ");
            out.println("   			  padding: 0 18px;                                                                                                                                                                  ");
            out.println("   			  display: none;                                                                                                                                                                    ");
            out.println("   			  background-color: white;                                                                                                                                                          ");
            out.println("   			  overflow: hidden;                                                                                                                                                                 ");
            out.println("   			}                                                                                                                                                                                   ");
            out.println("   		                                                                                                                                                                                        ");
            out.println("                                                                                                                                                                                                 ");
            out.println("   			</style>                                                                                                                                                                            ");
            out.println("   	</head>                                                                                                                                                                                     ");
            out.println("   	<body>                                                                                                                                                                                      ");
            out.println("   		<div class='container'>                                                                                                                                                                 ");
            out.println("   			<header class='clearfix'>                                                                                                                                                           ");
            out.println("   				<span>Data Analyzer <span class='bp-icon bp-icon-about' data-content='Analiza la nota de tus alumnos con todo lujo de detalles'></span></span>                                  ");
            out.println("                                                                                                                                                                                                 ");
            out.println("   			</header>	                                                                                                                                                                        ");
            out.println("   			<div class='filler-above'>                                                                                                                                                          ");
            out.println("   				<h2>Resultados:</h2>                                                                                                                                                            ");
            out.println("                                                                                                                                                                                                 ");
            out.println("   				<button class='accordion'>Datos individuales</button>                                                                                                                           ");
            out.println("   				<div class='panel'>                                                                                                                                                             ");
            for (int i = 1; i < hmAlumnos.size() + 1; i++) {

                out.println("   				    <button class='accordion'>" + hmAlumnos.get(i) + "</button>                                                                                                                          ");
                out.println("   			    	<div class='panel'>                                                                                                                                                         ");
                out.println("   					  <button class='accordion'>Tabla Resumen</button>                                                                                                                          ");
                out.println("   					   <div class='panel'>                                                                                                                                                      ");
                out.println("   							<div id='table_divT1" + i + "'></div>                                                                                                                                          ");
                out.println("   					   </div>                                                                                                                                                                   ");
                out.println("                                                                                                                                                                                                 ");
                out.println("   					   <button class='accordion'>Graficas de evolución</button>                                                                                                                 ");
                out.println("   					   <div class='panel'>                                                                                                                                                      ");
                out.println("                        <b>Trimestre 1 de " + hmAlumnos.get(i) + "</b>    ");
                out.println("   							<div id='chart" + i + "'></div>                                                                                                                                              ");
                out.println("                        <b>Trimestre 2 de " + hmAlumnos.get(i) + "</b>    ");
                out.println("   							<div id='chartT2" + i + "'></div>                                                                                                                                              ");
                out.println("                        <b>Trimestre 3 de " + hmAlumnos.get(i) + "</b>     ");
                out.println("   							<div id='chartT3" + i + "'></div>                                                                                                                                              ");

                out.println("                        <b>Evolución de " + hmAlumnos.get(i) + " en el curso</b>     ");
                out.println("   							<div id='chartC" + i + "'></div> ");
                out.println("   					   </div>                                                                                                                                                                   ");
                out.println("                                                                                                                                                                                                 ");
                out.println("   	                                                                                                                                                                                            ");
                out.println("   				<div id='chart'></div>                                                                                                                                                          ");
                out.println("   				</div>                                                                                                                                                                          ");
                out.println("   			                                                                                                                                                                                    ");
            }
            out.println("   																					                                                                                                            ");
            out.println("   				</div>                                                                                                                                                                          ");
            out.println("   																					                                                                                                            ");
            out.println("   				<button class='accordion'>Datos colectivos</button>                                                                                                                             ");
            out.println("   				<div class='panel'>                                                                                                                                                             ");
            out.println("   				<button class='accordion'>Tablas resumen</button>                                                                                                                             ");
            out.println("   				<div class='panel'>                                                                                                                                                             ");
//DATOS COLECTIVOS -----------------------------------------------------------------------------------------------------------------------
            out.println("   				<p> Estos son los datos colectivos de los alumnos:</p>                                                                                                                          ");
            out.println("                               <div id='table_divResumenCurso'></div>                                                                 ");
            out.println("   				<p> Resumen Trimestre 1:</p>                                                                                                                          ");
            out.println("                               <div id='drawTableResumenT1'></div>                                                                 ");
            out.println("   				<p> Resumen Trimestre 2:</p>                                                                                                                          ");
            out.println("                               <div id='drawTableResumenT2'></div>                                                                 ");
            out.println("   				<p> Resumen Trimestre 3:</p>                                                                                                                          ");
            out.println("                               <div id='drawTableResumenT3'></div>                                                                 ");
            out.println("   				<p> Resumen de todos los exámenes:</p>                                                                                                                          ");
            out.println("                               <div id='drawTableResumenNotasColectivo'></div>                                                                 ");

            out.println("   				</div>                                                                                                                                                                          ");
            out.println("   				<button class='accordion'>Gráficas de evolución</button>                                                                                                                             ");
            out.println("   				<div class='panel'>                                                                                                                                                             ");
            out.println("   				<p> Gráfica de evolución del primer trimestre:</p>                                                                                                                          ");
            out.println("                               <div id='drawChartColect1'></div>                                                                 ");
            out.println("   				<p> Gráfica de evolución del segundo trimestre:</p>                                                                                                                          ");
            out.println("                               <div id='drawChartColect2'></div>                                                                 ");
            out.println("   				<p> Gráfica de evolución del tercer trimestre:</p>                                                                                                                          ");
            out.println("                               <div id='drawChartColect3'></div>                                                                 ");
            out.println("   				<p> Gráfica de evolución del curso:</p>                                                                                                                          ");
            out.println("                               <div id='drawChartColectCurso'></div>                                                                 ");

            out.println("   				</div>                                                                                                                                                                          ");
            out.println("   				</div>                                                                                                                                                                          ");
            out.println("   	<p> Creado por: <b>Pedro Carnerero Martínez</b> </p>                                                                                                                                                                                     ");

            out.println("   																					                                                                                                            ");
            out.println("   																					                                                                                                            ");
            out.println("   				<script>                                                                                                                                                                        ");
            out.println("   				var acc = document.getElementsByClassName('accordion');                                                                                                                         ");
            out.println("   				var i;                                                                                                                                                                          ");
            out.println("   																					                                                                                                            ");
            out.println("   				for (i = 0; i < acc.length; i++) {                                                                                                                                              ");
            out.println("   				  acc[i].addEventListener('click', function() {                                                                                                                                 ");
            out.println("   					this.classList.toggle('active');                                                                                                                                            ");
            out.println("   					var panel = this.nextElementSibling;                                                                                                                                        ");
            out.println("   					if (panel.style.display === 'block') {                                                                                                                                      ");
            out.println("   					  panel.style.display = 'none';                                                                                                                                             ");
            out.println("   					} else {                                                                                                                                                                    ");
            out.println("   					  panel.style.display = 'block';                                                                                                                                            ");
            out.println("   					}                                                                                                                                                                           ");
            out.println("   				  });                                                                                                                                                                           ");
            out.println("   				}                                                                                                                                                                               ");
            out.println("   				</script>                                                                                                                                                                       ");
            out.println("   			</div>                                                                                                                                                                              ");
            out.println("   		</div>                                                                                                                                                                                  ");
            out.println("   		<script src='js/cbpTooltipMenu.min.js'></script>                                                                                                                                        ");
            out.println("   		<script>                                                                                                                                                                                ");
            out.println("   			var menu = new cbpTooltipMenu( document.getElementById( 'cbp-tm-menu' ) );                                                                                                          ");
            out.println("   		</script>                                                                                                                                                                               ");
            out.println("   	</body>                                                                                                                                                                                     ");

            out.println("   </html>                                                                                                                                                                                       ");
            out.println("                                                                                                                                                                                                 ");
        }

    }

    private void analizaDatosTrimestrales(Part datosTrim) {

        String nombreAlumno = "";
        int numCeldas = 0;

        String strComent = "";

        //Recorremos cada archivo
        try {
            FileInputStream file = new FileInputStream(datosTrim.getSubmittedFileName());
            XSSFWorkbook libro = new XSSFWorkbook(file);
            for (int i = 0; i < 3; i++) {
                XSSFSheet hoja = libro.getSheetAt(i);
                XSSFCell celda;
                numeroFilas = hoja.getLastRowNum();

                //Estamos a nivel de fila
                for (int x = 0; x < numeroFilas + 1; x++) {

                    XSSFRow fila = hoja.getRow(x);
                    numCeldas = fila.getLastCellNum();
                    hmCoords.put(fila.getRowNum(), numCeldas);

                    //Bajamos a nivel de celda
                    for (int w = 0; w < numCeldas + 1; w++) {

                        //Obtenemos todas las celdas
                        celda = fila.getCell(w);
                        //Guardamos la fila 0 para constantes 
                        if (x != 0) {

                            //Guardamos la celda 0 para los nombres de los alumnos
                            if (celda != null) {
                                if (w == 0) {

                                    if (celda.getCellType() == Cell.CELL_TYPE_STRING) {

                                        hmAlumnos.put(fila.getRowNum(), celda.getStringCellValue());
                                    }
                                } else {

                                    //Miramos cual es el trimestre
                                    //Guardamos notas de los alumnos en el multimap
                                    if (celda.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                                        switch (i) {
                                            case 0:
                                                mmNotasT1.put(fila.getRowNum(), celda.getNumericCellValue());

                                                break;
                                            case 1:

                                                mmNotasT2.put(fila.getRowNum(), celda.getNumericCellValue());

                                                break;

                                            case 2:

                                                mmNotasT3.put(fila.getRowNum(), celda.getNumericCellValue());

                                                break;

                                        }
                                    }

                                }
                            }

                            //Cuando x sea 0, significa que la fila es la primera, para recuperar el nombre del examen
                            //La celda 0 está reservada a la constante 'Nombre de alumno'
                        } else {

                            if (celda != null) {

                                if (w != 0) {

                                    if (celda.getCellType() == Cell.CELL_TYPE_STRING) {

                                        switch (i) {
                                            case 0:
                                                hmNombreExamenesT1.put(celda.getColumnIndex(), celda.getStringCellValue());
                                                break;

                                            case 1:
                                                hmNombreExamenesT2.put(celda.getColumnIndex(), celda.getStringCellValue());

                                                break;

                                            case 2:
                                                hmNombreExamenesT3.put(celda.getColumnIndex(), celda.getStringCellValue());

                                                break;
                                        }

                                    }
                                } else {
                                    //En el 0,0 se encuentra el comentario que nos dirá que trimestre es

                                }
                            }

                        }

                    }

                }
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    //Calcula la nota media del trimestre individualmente
    private HashMap<Integer, Double> calculaNotaMediaTrimestre(HashMap<Integer, String> hmAlumnos, ListMultimap<Integer, Double> mmNotas) {

        HashMap<Integer, Double> notaMediaCurso = new HashMap<>();

        double sumatoria = 0;
        double notaMedia = 0;
        for (int i = 1; i < hmAlumnos.size() + 1; i++) {

            sumatoria = 0;
            List<Double> lNotasAlumno = new ArrayList<>();
            lNotasAlumno = mmNotas.get(i);

            for (int x = 0; x < lNotasAlumno.size(); x++) {

                sumatoria += lNotasAlumno.get(x);

            }

            notaMedia = sumatoria / (lNotasAlumno.size());

            notaMediaCurso.put(i, notaMedia);

        }

        return notaMediaCurso;
    }

    //Calcula la nota media individualmente
    private HashMap<Integer, Double> calculaNotaMediaCurso(HashMap<Integer, String> hmAlumnos, ListMultimap<Integer, Double> notasT1, ListMultimap<Integer, Double> notasT2, ListMultimap<Integer, Double> notasT3) {

        HashMap<Integer, Double> notaMediaCurso = new HashMap<>();

        double sumatoria = 0;
        double notaMedia = 0;
        for (int i = 1; i < hmAlumnos.size() + 1; i++) {

            sumatoria = 0;
            List<Double> lNotasTotales = new ArrayList<>();
            List<Double> lNotasAlumno = new ArrayList<>();
            lNotasAlumno = notasT1.get(i);

            //Creamos dos nuevos hashMap con las notas de los otros trimestres para añadirlos a la lista principal
            List<Double> lNotasAlumnoT2 = new ArrayList<>();
            lNotasAlumnoT2 = notasT2.get(i);

            List<Double> lNotasAlumnoT3 = new ArrayList<>();
            lNotasAlumnoT3 = notasT3.get(i);

            lNotasTotales.addAll(lNotasAlumno);
            lNotasTotales.addAll(lNotasAlumnoT2);
            lNotasTotales.addAll(lNotasAlumnoT3);

            for (int x = 0; x < lNotasTotales.size(); x++) {

                sumatoria += lNotasTotales.get(x);

            }

            notaMedia = sumatoria / (lNotasTotales.size());

            notaMediaCurso.put(i, notaMedia);

        }

        return notaMediaCurso;
    }

    //Calcula la nota media de cada examen por trimestre
    private HashMap<Integer, Double> calculaNotaMediaPorExamenTrimestre(HashMap<Integer, String> hmExamenes, ListMultimap<Integer, Double> hmNotaAlumnos, HashMap<Integer, String> hmAlumnos) {

        HashMap<Integer, Double> hmNotaMediaPorExamen = new HashMap<>();
        List<Double> lNotasTrimestre = new ArrayList<>();
        double notaExamen = 0;
        double sumatoria = 0;
        double notaMedia = 0;

        for (int i = 0; i < hmExamenes.size(); i++) {

            sumatoria = 0;

            for (int x = 1; x < hmAlumnos.size() + 1; x++) {
                lNotasTrimestre = hmNotaAlumnos.get(x);
                notaExamen = lNotasTrimestre.get(i);
                sumatoria += notaExamen;

            }

            notaMedia = sumatoria / (hmAlumnos.size());

            hmNotaMediaPorExamen.put(i + 1, notaMedia);

        }

        return hmNotaMediaPorExamen;
    }

    //Calcula la nota media del trimestre en general
    private double calculaNotaMediaTrimestreColectivo(HashMap<Integer, Double> notaTrimestreAlumnos) {
        double notaMedia = 0;
        double sumatoria = 0;

        for (int i = 1; i < notaTrimestreAlumnos.size() + 1; i++) {

            sumatoria += notaTrimestreAlumnos.get(i);

        }

        notaMedia = sumatoria / notaTrimestreAlumnos.size();

        return notaMedia;

    }

    private double calculaNotaMediaCursoColectivo(double notaT1, double notaT2, double notaT3) {

        double notaMedia = 0;
        double sumatoria = notaT1 + notaT2 + notaT3;

        notaMedia = sumatoria / 3;

        return notaMedia;
    }

    private ListMultimap<Integer, Double> dameTodasNotasCurso(ListMultimap<Integer, Double> notasT1, ListMultimap<Integer, Double> notasT2, ListMultimap<Integer, Double> notasT3) {

        ListMultimap mmNotasCursoEntero = ArrayListMultimap.create();
        for (int i = 0; i < notasT1.size() + 1; i++) {

            List<Double> lNotasT1 = notasT1.get(i);
            for (int x = 0; x < lNotasT1.size(); x++) {
                mmNotasCursoEntero.put(i, lNotasT1.get(x));
            }
        }

        for (int i = 0; i < notasT2.size() + 1; i++) {

            List<Double> lNotasT2 = notasT2.get(i);
            for (int x = 0; x < lNotasT2.size(); x++) {
                mmNotasCursoEntero.put(i, lNotasT2.get(x));
            }
        }

        for (int i = 0; i < notasT3.size() + 1; i++) {

            List<Double> lNotasT3 = notasT3.get(i);
            for (int x = 0; x < lNotasT3.size(); x++) {
                mmNotasCursoEntero.put(i, lNotasT3.get(x));
            }
        }

        return mmNotasCursoEntero;
    }

    private HashMap<Integer, String> dameNombreExamenesCurso(HashMap<Integer, String> hmNombresT1, HashMap<Integer, String> hmNombresT2, HashMap<Integer, String> hmNombresT3) {

        HashMap<Integer, String> hmNombreExamenesCurso = new HashMap<>();

        for (int i = 1; i < hmNombresT1.size() + 1; i++) {
            hmNombreExamenesCurso.put(i, hmNombresT1.get(i));
        }

        for (int i = 1; i < hmNombresT2.size() + 1; i++) {
            hmNombreExamenesCurso.put(i + hmNombresT1.size(), hmNombresT2.get(i));
        }

        for (int i = 1; i < hmNombresT3.size() + 1; i++) {
            hmNombreExamenesCurso.put(i + hmNombresT1.size() + hmNombresT2.size(), hmNombresT3.get(i));
        }

        return hmNombreExamenesCurso;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}


package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.dto.RelatorioDTO;
import com.example.demo.dto.RelatorioResponseDTO;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Relatorio;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.RelatorioRepository;

import jakarta.transaction.Transactional;

@Service
public class RelatorioService {

    @Autowired
    private RelatorioRepository relatorioRepository;

    @Autowired
    private PostoRepository postoRepository;

    @Transactional
    public RelatorioResponseDTO salvar(RelatorioDTO dto) {

        Posto posto = postoRepository.findById(dto.getPostoId())
                .orElseThrow(() -> new RuntimeException("Posto não encontrado com id: " + dto.getPostoId()));

        LocalDate hoje = LocalDate.now();

        Optional<Relatorio> existente = relatorioRepository
                .findByPostoIdAndData(dto.getPostoId(), hoje);

        Relatorio relatorio = existente.orElseGet(Relatorio::new);

        relatorio.setPosto(posto);
        relatorio.setData(hoje);
        relatorio.setAtaquesManha(dto.getAtaquesManha());
        relatorio.setPrevencoesManha(dto.getPrevencoesManha());
        relatorio.setAtaquesTarde(dto.getAtaquesTarde());
        relatorio.setPrevencoesTarde(dto.getPrevencoesTarde());
        relatorio.setObservacoes(dto.getObservacoes());

        Relatorio salvo = relatorioRepository.save(relatorio);

        return toResponse(salvo);
    }

    public boolean existeHoje(Long postoId) {
        return relatorioRepository.existsByPostoIdAndData(postoId, LocalDate.now());
    }

    private RelatorioResponseDTO toResponse(Relatorio r) {
        RelatorioResponseDTO dto = new RelatorioResponseDTO();
        dto.setId(r.getId());
        dto.setPostoId(r.getPosto().getId());
        dto.setPosto(r.getPosto().getNome());
        dto.setData(r.getData());
        dto.setAtaquesManha(r.getAtaquesManha());
        dto.setPrevencoesManha(r.getPrevencoesManha());
        dto.setAtaquesTarde(r.getAtaquesTarde());
        dto.setPrevencoesTarde(r.getPrevencoesTarde());
        dto.setObservacoes(r.getObservacoes());
        return dto;
    }

    public List<RelatorioResponseDTO> listarTodos() {
        return relatorioRepository.findAllByOrderByPosto_IdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void ocultarTodos() {
        List<Relatorio> lista = relatorioRepository.findAll();

        for (Relatorio r : lista) {
            r.setVisivelAdmin(false);
        }

        relatorioRepository.saveAll(lista);
    }

    @Transactional
    public void ocultar(Long id) {
        Relatorio r = relatorioRepository.findById(id).orElseThrow();
        r.setVisivelAdmin(false);
        relatorioRepository.save(r);
    }

    public RelatorioResponseDTO buscarHoje(Long postoId) {
        return relatorioRepository
                .findByPostoIdAndData(postoId, LocalDate.now())
                .map(this::toDto)
                .orElse(null);
    }

    public List<RelatorioResponseDTO> listarPorPosto(Long postoId) {
        return relatorioRepository.findByPostoId(postoId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private RelatorioResponseDTO toDto(Relatorio relatorio) {
        RelatorioResponseDTO dto = new RelatorioResponseDTO();
        dto.setId(relatorio.getId());
        dto.setPostoId(relatorio.getPosto().getId());
        dto.setPosto(relatorio.getPosto().getNome());
        dto.setData(relatorio.getData());
        dto.setAtaquesManha(relatorio.getAtaquesManha());
        dto.setPrevencoesManha(relatorio.getPrevencoesManha());
        dto.setAtaquesTarde(relatorio.getAtaquesTarde());
        dto.setPrevencoesTarde(relatorio.getPrevencoesTarde());
        dto.setObservacoes(relatorio.getObservacoes());
        return dto;
    }

    public ResponseEntity<byte[]> exportarExcel(LocalDate inicio, LocalDate fim) {
    List<Relatorio> relatorios = relatorioRepository
            .findByDataBetweenOrderByDataAscPostoNomeAsc(inicio, fim);

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Relatório Operacional");

        // ── HELPERS ───────────────────────────────────────────────────────────

        // Cria XSSFColor a partir de hex
        // Uso: color("1B2A4A")
        // Requer cast para XSSFCellStyle nos métodos setFillForegroundColor

        // ── LARGURAS DAS COLUNAS ──────────────────────────────────────────────
        // A=Data, B=Posto, C=Prev.Mat, D=Les.Mat, E=Prev.Ves, F=Les.Ves, G=TotalPrev, H=Obs
        int[] colWidths = {13*256, 13*256, 16*256, 16*256, 16*256, 16*256, 14*256, 36*256};
        for (int i = 0; i < colWidths.length; i++) {
            sheet.setColumnWidth(i, colWidths[i]);
        }

        // ── FONTES ────────────────────────────────────────────────────────────
        Font fontTitulo = workbook.createFont();
        fontTitulo.setFontName("Arial");
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 16);
        fontTitulo.setColor(IndexedColors.WHITE.getIndex());

        Font fontSubtitulo = workbook.createFont();
        fontSubtitulo.setFontName("Arial");
        fontSubtitulo.setFontHeightInPoints((short) 10);
        fontSubtitulo.setColor(IndexedColors.WHITE.getIndex());

        Font fontHeader = workbook.createFont();
        fontHeader.setFontName("Arial");
        fontHeader.setBold(true);
        fontHeader.setFontHeightInPoints((short) 10);
        fontHeader.setColor(IndexedColors.WHITE.getIndex());

        Font fontDados = workbook.createFont();
        fontDados.setFontName("Arial");
        fontDados.setFontHeightInPoints((short) 10);
        ((XSSFFont) fontDados).setColor(new XSSFColor(hexToBytes("1C2833"), null));

        Font fontSecaoTitulo = workbook.createFont();
        fontSecaoTitulo.setFontName("Arial");
        fontSecaoTitulo.setBold(true);
        fontSecaoTitulo.setFontHeightInPoints((short) 11);
        fontSecaoTitulo.setColor(IndexedColors.WHITE.getIndex());

        Font fontSubHeader = workbook.createFont();
        fontSubHeader.setFontName("Arial");
        fontSubHeader.setBold(true);
        fontSubHeader.setFontHeightInPoints((short) 10);
        fontSubHeader.setColor(IndexedColors.WHITE.getIndex());

        Font fontLabelGreen = workbook.createFont();
        fontLabelGreen.setFontName("Arial");
        fontLabelGreen.setBold(true);
        fontLabelGreen.setFontHeightInPoints((short) 10);
        ((XSSFFont) fontLabelGreen).setColor(new XSSFColor(hexToBytes("1A6B3C"), null));

        Font fontLabelRed = workbook.createFont();
        fontLabelRed.setFontName("Arial");
        fontLabelRed.setBold(true);
        fontLabelRed.setFontHeightInPoints((short) 10);
        ((XSSFFont) fontLabelRed).setColor(new XSSFColor(hexToBytes("C0392B"), null));

        Font fontTotalGreen = workbook.createFont();
        fontTotalGreen.setFontName("Arial");
        fontTotalGreen.setBold(true);
        fontTotalGreen.setFontHeightInPoints((short) 10);
        ((XSSFFont) fontTotalGreen).setColor(new XSSFColor(hexToBytes("145A32"), null));

        Font fontTotalRed = workbook.createFont();
        fontTotalRed.setFontName("Arial");
        fontTotalRed.setBold(true);
        fontTotalRed.setFontHeightInPoints((short) 10);
        ((XSSFFont) fontTotalRed).setColor(new XSSFColor(hexToBytes("922B21"), null));

        // ── ESTILOS BASE ──────────────────────────────────────────────────────

        // Título navy
        XSSFCellStyle sTitulo = (XSSFCellStyle) workbook.createCellStyle();
        sTitulo.setFillForegroundColor(new XSSFColor(hexToBytes("1B2A4A"), null));
        sTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sTitulo.setAlignment(HorizontalAlignment.LEFT);
        sTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        sTitulo.setFont(fontTitulo);
        sTitulo.setIndention((short) 2);
        setBorderMedium(sTitulo);

        // Subtítulo teal esquerda
        XSSFCellStyle sSubLeft = (XSSFCellStyle) workbook.createCellStyle();
        sSubLeft.setFillForegroundColor(new XSSFColor(hexToBytes("2E86AB"), null));
        sSubLeft.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sSubLeft.setAlignment(HorizontalAlignment.LEFT);
        sSubLeft.setVerticalAlignment(VerticalAlignment.CENTER);
        sSubLeft.setFont(fontSubtitulo);
        sSubLeft.setIndention((short) 2);
        setBorderMedium(sSubLeft);

        // Subtítulo teal direita
        XSSFCellStyle sSubRight = (XSSFCellStyle) workbook.createCellStyle();
        sSubRight.setFillForegroundColor(new XSSFColor(hexToBytes("2E86AB"), null));
        sSubRight.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sSubRight.setAlignment(HorizontalAlignment.RIGHT);
        sSubRight.setVerticalAlignment(VerticalAlignment.CENTER);
        sSubRight.setFont(fontSubtitulo);
        sSubRight.setIndention((short) 2);
        setBorderMedium(sSubRight);

        // Cabeçalho da tabela
        XSSFCellStyle sHeader = (XSSFCellStyle) workbook.createCellStyle();
        sHeader.setFillForegroundColor(new XSSFColor(hexToBytes("1B2A4A"), null));
        sHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sHeader.setAlignment(HorizontalAlignment.CENTER);
        sHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        sHeader.setFont(fontHeader);
        setBorderMedium(sHeader);

        // Dado branco centro
        XSSFCellStyle sDadoBranco = (XSSFCellStyle) workbook.createCellStyle();
        sDadoBranco.setFillForegroundColor(new XSSFColor(hexToBytes("FFFFFF"), null));
        sDadoBranco.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sDadoBranco.setAlignment(HorizontalAlignment.CENTER);
        sDadoBranco.setVerticalAlignment(VerticalAlignment.CENTER);
        sDadoBranco.setFont(fontDados);
        setBorderThin(sDadoBranco);

        // Dado cinza centro
        XSSFCellStyle sDadoCinza = (XSSFCellStyle) workbook.createCellStyle();
        sDadoCinza.setFillForegroundColor(new XSSFColor(hexToBytes("F4F6F9"), null));
        sDadoCinza.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sDadoCinza.setAlignment(HorizontalAlignment.CENTER);
        sDadoCinza.setVerticalAlignment(VerticalAlignment.CENTER);
        sDadoCinza.setFont(fontDados);
        setBorderThin(sDadoCinza);

        // Dado branco esquerda
        XSSFCellStyle sDadoBrancoEsq = (XSSFCellStyle) workbook.createCellStyle();
        sDadoBrancoEsq.cloneStyleFrom(sDadoBranco);
        sDadoBrancoEsq.setAlignment(HorizontalAlignment.LEFT);
        sDadoBrancoEsq.setIndention((short) 1);

        // Dado cinza esquerda
        XSSFCellStyle sDadoCinzaEsq = (XSSFCellStyle) workbook.createCellStyle();
        sDadoCinzaEsq.cloneStyleFrom(sDadoCinza);
        sDadoCinzaEsq.setAlignment(HorizontalAlignment.LEFT);
        sDadoCinzaEsq.setIndention((short) 1);

        // Dado branco borda inferior média (última linha)
        XSSFCellStyle sDadoBrancoBottom = (XSSFCellStyle) workbook.createCellStyle();
        sDadoBrancoBottom.cloneStyleFrom(sDadoBranco);
        sDadoBrancoBottom.setBorderBottom(BorderStyle.MEDIUM);

        XSSFCellStyle sDadoBrancoBottomEsq = (XSSFCellStyle) workbook.createCellStyle();
        sDadoBrancoBottomEsq.cloneStyleFrom(sDadoBrancoEsq);
        sDadoBrancoBottomEsq.setBorderBottom(BorderStyle.MEDIUM);

        XSSFCellStyle sDadoCinzaBottom = (XSSFCellStyle) workbook.createCellStyle();
        sDadoCinzaBottom.cloneStyleFrom(sDadoCinza);
        sDadoCinzaBottom.setBorderBottom(BorderStyle.MEDIUM);

        XSSFCellStyle sDadoCinzaBottomEsq = (XSSFCellStyle) workbook.createCellStyle();
        sDadoCinzaBottomEsq.cloneStyleFrom(sDadoCinzaEsq);
        sDadoCinzaBottomEsq.setBorderBottom(BorderStyle.MEDIUM);

        // Título da seção de resumo
        XSSFCellStyle sSecaoTitulo = (XSSFCellStyle) workbook.createCellStyle();
        sSecaoTitulo.setFillForegroundColor(new XSSFColor(hexToBytes("1B2A4A"), null));
        sSecaoTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sSecaoTitulo.setAlignment(HorizontalAlignment.LEFT);
        sSecaoTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        sSecaoTitulo.setFont(fontSecaoTitulo);
        sSecaoTitulo.setIndention((short) 2);
        setBorderMedium(sSecaoTitulo);

        // Sub-header navy vazio (label A-B)
        XSSFCellStyle sSubHeaderNavy = (XSSFCellStyle) workbook.createCellStyle();
        sSubHeaderNavy.setFillForegroundColor(new XSSFColor(hexToBytes("1B2A4A"), null));
        sSubHeaderNavy.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorderMedium(sSubHeaderNavy);

        // Sub-header teal (Matutino / Vespertino)
        XSSFCellStyle sSubHeaderTeal = (XSSFCellStyle) workbook.createCellStyle();
        sSubHeaderTeal.setFillForegroundColor(new XSSFColor(hexToBytes("2E86AB"), null));
        sSubHeaderTeal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sSubHeaderTeal.setAlignment(HorizontalAlignment.CENTER);
        sSubHeaderTeal.setVerticalAlignment(VerticalAlignment.CENTER);
        sSubHeaderTeal.setFont(fontSubHeader);
        setBorderMedium(sSubHeaderTeal);

        // Sub-header verde (Total Geral)
        XSSFCellStyle sSubHeaderGreen = (XSSFCellStyle) workbook.createCellStyle();
        sSubHeaderGreen.setFillForegroundColor(new XSSFColor(hexToBytes("1A6B3C"), null));
        sSubHeaderGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sSubHeaderGreen.setAlignment(HorizontalAlignment.CENTER);
        sSubHeaderGreen.setVerticalAlignment(VerticalAlignment.CENTER);
        sSubHeaderGreen.setFont(fontSubHeader);
        setBorderMedium(sSubHeaderGreen);

        // Label Prevenções (verde claro)
        XSSFCellStyle sLabelPrev = (XSSFCellStyle) workbook.createCellStyle();
        sLabelPrev.setFillForegroundColor(new XSSFColor(hexToBytes("EAF5EE"), null));
        sLabelPrev.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sLabelPrev.setAlignment(HorizontalAlignment.LEFT);
        sLabelPrev.setVerticalAlignment(VerticalAlignment.CENTER);
        sLabelPrev.setFont(fontLabelGreen);
        sLabelPrev.setIndention((short) 2);
        setBorderThin(sLabelPrev);

        // Valor Prevenções normal
        XSSFCellStyle sValPrev = (XSSFCellStyle) workbook.createCellStyle();
        sValPrev.setFillForegroundColor(new XSSFColor(hexToBytes("EAF5EE"), null));
        sValPrev.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sValPrev.setAlignment(HorizontalAlignment.CENTER);
        sValPrev.setVerticalAlignment(VerticalAlignment.CENTER);
        sValPrev.setFont(fontDados);
        setBorderThin(sValPrev);

        // Valor Total Prevenções (verde mais escuro)
        XSSFCellStyle sValPrevTotal = (XSSFCellStyle) workbook.createCellStyle();
        sValPrevTotal.setFillForegroundColor(new XSSFColor(hexToBytes("C2E0CC"), null));
        sValPrevTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sValPrevTotal.setAlignment(HorizontalAlignment.CENTER);
        sValPrevTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        sValPrevTotal.setFont(fontTotalGreen);
        setBorderThin(sValPrevTotal);

        // Label Lesões (vermelho claro)
        XSSFCellStyle sLabelLes = (XSSFCellStyle) workbook.createCellStyle();
        sLabelLes.setFillForegroundColor(new XSSFColor(hexToBytes("FDEDEC"), null));
        sLabelLes.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sLabelLes.setAlignment(HorizontalAlignment.LEFT);
        sLabelLes.setVerticalAlignment(VerticalAlignment.CENTER);
        sLabelLes.setFont(fontLabelRed);
        sLabelLes.setIndention((short) 2);
        setBorderThin(sLabelLes);

        // Valor Lesões normal
        XSSFCellStyle sValLes = (XSSFCellStyle) workbook.createCellStyle();
        sValLes.setFillForegroundColor(new XSSFColor(hexToBytes("FDEDEC"), null));
        sValLes.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sValLes.setAlignment(HorizontalAlignment.CENTER);
        sValLes.setVerticalAlignment(VerticalAlignment.CENTER);
        sValLes.setFont(fontDados);
        setBorderThin(sValLes);

        // Valor Total Lesões (vermelho mais escuro)
        XSSFCellStyle sValLesTotal = (XSSFCellStyle) workbook.createCellStyle();
        sValLesTotal.setFillForegroundColor(new XSSFColor(hexToBytes("F5B7B1"), null));
        sValLesTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sValLesTotal.setAlignment(HorizontalAlignment.CENTER);
        sValLesTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        sValLesTotal.setFont(fontTotalRed);
        setBorderThin(sValLesTotal);

        // ── LINHA 1: margem topo ──────────────────────────────────────────────
        sheet.createRow(0).setHeightInPoints(10);

        // ── LINHA 2: TÍTULO ───────────────────────────────────────────────────
        Row rowTitulo = sheet.createRow(1);
        rowTitulo.setHeightInPoints(36);
        Cell cTitulo = rowTitulo.createCell(0);
        cTitulo.setCellValue("RELATÓRIO OPERACIONAL");
        cTitulo.setCellStyle(sTitulo);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
        applyBorderMerged(sheet, workbook, 1, 1, 0, 7, BorderStyle.MEDIUM);

        // ── LINHA 3: PERÍODO + EMISSÃO ────────────────────────────────────────
        Row rowSub = sheet.createRow(2);
        rowSub.setHeightInPoints(18);

        Cell cPeriodo = rowSub.createCell(0);
        cPeriodo.setCellValue("Período de referência:  " +
                inicio.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                "  –  " +
                fim.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cPeriodo.setCellStyle(sSubLeft);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));
        applyBorderMerged(sheet, workbook, 2, 2, 0, 3, BorderStyle.MEDIUM);

        Cell cEmissao = rowSub.createCell(4);
        cEmissao.setCellValue("Emitido em:  " +
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cEmissao.setCellStyle(sSubRight);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 7));
        applyBorderMerged(sheet, workbook, 2, 2, 4, 7, BorderStyle.MEDIUM);

        // ── LINHA 4: espaço ───────────────────────────────────────────────────
        sheet.createRow(3).setHeightInPoints(10);

        // ── LINHA 5: CABEÇALHO DA TABELA ─────────────────────────────────────
        Row rowHeader = sheet.createRow(4);
        rowHeader.setHeightInPoints(24);
        String[] colunas = {
            "Data", "Posto",
            "Prev. Matutino", "Lesões Matutino",
            "Prev. Vespertino", "Lesões Vespertino",
            "Total Prev.", "Observações"
        };
        for (int i = 0; i < colunas.length; i++) {
            Cell c = rowHeader.createCell(i);
            c.setCellValue(colunas[i]);
            c.setCellStyle(sHeader);
        }

        // ── DADOS ─────────────────────────────────────────────────────────────
        int ROW_START = 5; // índice 0-based (linha 6 no Excel)
        int rowNum = ROW_START;
        long totalPrevMat = 0, totalPrevVes = 0;
        long totalLesMat  = 0, totalLesVes  = 0;

        List<Relatorio> lista = relatorios;
        for (int i = 0; i < lista.size(); i++) {
            Relatorio r  = lista.get(i);
            boolean par  = (i % 2 == 0);
            boolean last = (i == lista.size() - 1);
            Row row      = sheet.createRow(rowNum);
            row.setHeightInPoints(18);

            long pm = r.getPrevencoesManha();
            long lm = r.getAtaquesManha();
            long pt = r.getPrevencoesTarde();
            long lt = r.getAtaquesTarde();
            totalPrevMat += pm; totalPrevVes += pt;
            totalLesMat  += lm; totalLesVes  += lt;

            // estilos desta linha
            XSSFCellStyle sEsq    = last ? (par ? sDadoBrancoBottomEsq : sDadoCinzaBottomEsq)
                                         : (par ? sDadoBrancoEsq       : sDadoCinzaEsq);
            XSSFCellStyle sCentro = last ? (par ? sDadoBrancoBottom     : sDadoCinzaBottom)
                                         : (par ? sDadoBranco           : sDadoCinza);

            Cell c0 = row.createCell(0); c0.setCellValue(r.getData().toString());      c0.setCellStyle(sEsq);
            Cell c1 = row.createCell(1); c1.setCellValue(r.getPosto().getNome());      c1.setCellStyle(sEsq);
            Cell c2 = row.createCell(2); c2.setCellValue(pm);                          c2.setCellStyle(sCentro);
            Cell c3 = row.createCell(3); c3.setCellValue(lm);                          c3.setCellStyle(sCentro);
            Cell c4 = row.createCell(4); c4.setCellValue(pt);                          c4.setCellStyle(sCentro);
            Cell c5 = row.createCell(5); c5.setCellValue(lt);                          c5.setCellStyle(sCentro);
            Cell c6 = row.createCell(6); c6.setCellValue(pm + pt);                     c6.setCellStyle(sCentro);
            Cell c7 = row.createCell(7);
            c7.setCellValue(r.getObservacoes() != null ? r.getObservacoes() : "");
            c7.setCellStyle(sEsq);

            rowNum++;
        }

        int ROW_END = rowNum - 1; // índice 0-based da última linha de dados

        // ── ESPAÇO ────────────────────────────────────────────────────────────
        sheet.createRow(rowNum).setHeightInPoints(14);
        rowNum++;

        // ── RESUMO: título da seção ───────────────────────────────────────────
        int SEC = rowNum;
        Row rowSecTitulo = sheet.createRow(SEC);
        rowSecTitulo.setHeightInPoints(22);
        Cell cSecTit = rowSecTitulo.createCell(0);
        cSecTit.setCellValue("RESUMO DO PERÍODO");
        cSecTit.setCellStyle(sSecaoTitulo);
        sheet.addMergedRegion(new CellRangeAddress(SEC, SEC, 0, 7));
        applyBorderMerged(sheet, workbook, SEC, SEC, 0, 7, BorderStyle.MEDIUM);
        rowNum++;

        // ── RESUMO: sub-cabeçalhos ────────────────────────────────────────────
        int SH = rowNum;
        Row rowSH = sheet.createRow(SH);
        rowSH.setHeightInPoints(20);

        // A-B vazio navy
        rowSH.createCell(0).setCellStyle(sSubHeaderNavy);
        rowSH.createCell(1).setCellStyle(sSubHeaderNavy);
        sheet.addMergedRegion(new CellRangeAddress(SH, SH, 0, 1));
        applyBorderMerged(sheet, workbook, SH, SH, 0, 1, BorderStyle.MEDIUM);

        // C-D Matutino
        Cell cMat = rowSH.createCell(2);
        cMat.setCellValue("Matutino");
        cMat.setCellStyle(sSubHeaderTeal);
        rowSH.createCell(3).setCellStyle(sSubHeaderTeal);
        sheet.addMergedRegion(new CellRangeAddress(SH, SH, 2, 3));
        applyBorderMerged(sheet, workbook, SH, SH, 2, 3, BorderStyle.MEDIUM);

        // E-F Vespertino
        Cell cVes = rowSH.createCell(4);
        cVes.setCellValue("Vespertino");
        cVes.setCellStyle(sSubHeaderTeal);
        rowSH.createCell(5).setCellStyle(sSubHeaderTeal);
        sheet.addMergedRegion(new CellRangeAddress(SH, SH, 4, 5));
        applyBorderMerged(sheet, workbook, SH, SH, 4, 5, BorderStyle.MEDIUM);

        // G-H Total Geral
        Cell cTotGeral = rowSH.createCell(6);
        cTotGeral.setCellValue("Total Geral");
        cTotGeral.setCellStyle(sSubHeaderGreen);
        rowSH.createCell(7).setCellStyle(sSubHeaderGreen);
        sheet.addMergedRegion(new CellRangeAddress(SH, SH, 6, 7));
        applyBorderMerged(sheet, workbook, SH, SH, 6, 7, BorderStyle.MEDIUM);
        rowNum++;

        // ── RESUMO: linha Prevenções ──────────────────────────────────────────
        int PR = rowNum;
        Row rowPrev = sheet.createRow(PR);
        rowPrev.setHeightInPoints(20);

        Cell cLabelPrev = rowPrev.createCell(0);
        cLabelPrev.setCellValue("Prevenções");
        cLabelPrev.setCellStyle(sLabelPrev);
        rowPrev.createCell(1).setCellStyle(sLabelPrev);
        sheet.addMergedRegion(new CellRangeAddress(PR, PR, 0, 1));
        applyBorderMerged(sheet, workbook, PR, PR, 0, 1, BorderStyle.THIN);

        Cell cPrevMat = rowPrev.createCell(2);
        cPrevMat.setCellValue(totalPrevMat);
        cPrevMat.setCellStyle(sValPrev);
        rowPrev.createCell(3).setCellStyle(sValPrev);
        sheet.addMergedRegion(new CellRangeAddress(PR, PR, 2, 3));
        applyBorderMerged(sheet, workbook, PR, PR, 2, 3, BorderStyle.THIN);

        Cell cPrevVes = rowPrev.createCell(4);
        cPrevVes.setCellValue(totalPrevVes);
        cPrevVes.setCellStyle(sValPrev);
        rowPrev.createCell(5).setCellStyle(sValPrev);
        sheet.addMergedRegion(new CellRangeAddress(PR, PR, 4, 5));
        applyBorderMerged(sheet, workbook, PR, PR, 4, 5, BorderStyle.THIN);

        Cell cPrevTotal = rowPrev.createCell(6);
        cPrevTotal.setCellValue(totalPrevMat + totalPrevVes);
        cPrevTotal.setCellStyle(sValPrevTotal);
        rowPrev.createCell(7).setCellStyle(sValPrevTotal);
        sheet.addMergedRegion(new CellRangeAddress(PR, PR, 6, 7));
        applyBorderMerged(sheet, workbook, PR, PR, 6, 7, BorderStyle.THIN);
        rowNum++;

        // ── RESUMO: linha Lesões ──────────────────────────────────────────────
        int LR = rowNum;
        Row rowLes = sheet.createRow(LR);
        rowLes.setHeightInPoints(20);

        Cell cLabelLes = rowLes.createCell(0);
        cLabelLes.setCellValue("Lesões por águas-vivas");
        cLabelLes.setCellStyle(sLabelLes);
        rowLes.createCell(1).setCellStyle(sLabelLes);
        sheet.addMergedRegion(new CellRangeAddress(LR, LR, 0, 1));
        applyBorderMerged(sheet, workbook, LR, LR, 0, 1, BorderStyle.THIN);

        Cell cLesMat = rowLes.createCell(2);
        cLesMat.setCellValue(totalLesMat);
        cLesMat.setCellStyle(sValLes);
        rowLes.createCell(3).setCellStyle(sValLes);
        sheet.addMergedRegion(new CellRangeAddress(LR, LR, 2, 3));
        applyBorderMerged(sheet, workbook, LR, LR, 2, 3, BorderStyle.THIN);

        Cell cLesVes = rowLes.createCell(4);
        cLesVes.setCellValue(totalLesVes);
        cLesVes.setCellStyle(sValLes);
        rowLes.createCell(5).setCellStyle(sValLes);
        sheet.addMergedRegion(new CellRangeAddress(LR, LR, 4, 5));
        applyBorderMerged(sheet, workbook, LR, LR, 4, 5, BorderStyle.THIN);

        Cell cLesTotal = rowLes.createCell(6);
        cLesTotal.setCellValue(totalLesMat + totalLesVes);
        cLesTotal.setCellStyle(sValLesTotal);
        rowLes.createCell(7).setCellStyle(sValLesTotal);
        sheet.addMergedRegion(new CellRangeAddress(LR, LR, 6, 7));
        applyBorderMerged(sheet, workbook, LR, LR, 6, 7, BorderStyle.THIN);

        // borda externa do bloco de resumo inteiro
        applyBorderMerged(sheet, workbook, SEC, LR, 0, 7, BorderStyle.MEDIUM);

        // ── FREEZE ────────────────────────────────────────────────────────────
        sheet.createFreezePane(0, 5); // congela acima da linha 6

        // ── ÁREA DE IMPRESSÃO ─────────────────────────────────────────────────
        workbook.setPrintArea(0, 0, 7, 0, LR);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
        sheet.setFitToPage(true);

        // ── OUTPUT ────────────────────────────────────────────────────────────
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                "relatorio_" + inicio + "_" + fim + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(out.toByteArray());

    } catch (Exception e) {
        throw new RuntimeException("Erro ao gerar Excel", e);
    }
}

// ── MÉTODOS AUXILIARES (adicione na mesma classe) ─────────────────────────────

/**
 * Converte string hex "RRGGBB" em byte[] {R, G, B} para XSSFColor.
 */
private byte[] hexToBytes(String hex) {
    return new byte[]{
        (byte) Integer.parseInt(hex.substring(0, 2), 16),
        (byte) Integer.parseInt(hex.substring(2, 4), 16),
        (byte) Integer.parseInt(hex.substring(4, 6), 16)
    };
}

/**
 * Aplica BorderStyle.THIN nos 4 lados de um CellStyle.
 */
private void setBorderThin(XSSFCellStyle style) {
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
}

/**
 * Aplica BorderStyle.MEDIUM nos 4 lados de um CellStyle.
 */
private void setBorderMedium(XSSFCellStyle style) {
    style.setBorderTop(BorderStyle.MEDIUM);
    style.setBorderBottom(BorderStyle.MEDIUM);
    style.setBorderLeft(BorderStyle.MEDIUM);
    style.setBorderRight(BorderStyle.MEDIUM);
}

/**
 * Aplica bordas externas corretas em regiões mescladas,
 * sem criar linhas no meio da célula mesclada.
 */
private void applyBorderMerged(Sheet sheet, Workbook workbook,
                                int r1, int r2, int c1, int c2,
                                BorderStyle bStyle) {
    for (int r = r1; r <= r2; r++) {
        Row row = sheet.getRow(r);
        if (row == null) row = sheet.createRow(r);
        for (int c = c1; c <= c2; c++) {
            Cell cell = row.getCell(c);
            if (cell == null) cell = row.createCell(c);
            CellStyle existing = cell.getCellStyle();
            XSSFCellStyle ns = (XSSFCellStyle) workbook.createCellStyle();
            ns.cloneStyleFrom(existing);
            if (r == r1) ns.setBorderTop(bStyle);
            if (r == r2) ns.setBorderBottom(bStyle);
            if (c == c1) ns.setBorderLeft(bStyle);
            if (c == c2) ns.setBorderRight(bStyle);
            cell.setCellStyle(ns);
        }
    }
}

}
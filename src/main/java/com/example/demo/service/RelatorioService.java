
package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
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

        // Busca relatório existente do dia para sobrescrever, ou cria novo
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

    // Converte dto
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

    // ==============exportacao===============

    public ResponseEntity<byte[]> exportarExcel(LocalDate inicio, LocalDate fim) {
        List<Relatorio> relatorios = relatorioRepository
                .findByDataBetweenOrderByDataAscPostoNomeAsc(inicio, fim);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatórios");

            // Cabeçalho
            Row header = sheet.createRow(0);
            String[] colunas = { "Data", "Posto", "Prev. Manhã", "Lesões Manhã", "Prev. Tarde", "Lesões Tarde",
                    "Total Prev.", "Total Lesões", "Observações" };
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Dados
            int rowNum = 1;
            for (Relatorio r : relatorios) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getData().toString());
                row.createCell(1).setCellValue(r.getPosto().getNome());
                row.createCell(2).setCellValue(r.getPrevencoesManha());
                row.createCell(3).setCellValue(r.getAtaquesManha());
                row.createCell(4).setCellValue(r.getPrevencoesTarde());
                row.createCell(5).setCellValue(r.getAtaquesTarde());
                row.createCell(6).setCellValue(r.getPrevencoesManha() + r.getPrevencoesTarde());
                row.createCell(7).setCellValue(r.getAtaquesManha() + r.getAtaquesTarde());
                row.createCell(8).setCellValue(r.getObservacoes() != null ? r.getObservacoes() : "");
            }

            // Auto-size colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "relatorios_" + inicio + "_" + fim + ".xlsx");

            return ResponseEntity.ok().headers(headers).body(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel", e);
        }
    }

}
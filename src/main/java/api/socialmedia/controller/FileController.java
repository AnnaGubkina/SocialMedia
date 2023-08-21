package api.socialmedia.controller;


import api.socialmedia.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/file")
@Tag(name = "File Controller")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;


    /**
     * Данный метод получает файл по ID поста
     * Метод выделен в отдельный класс контроллера, чтобы не нагружать пост-контроллер и чтобы
     * файл можно было получить по отдельному запросу, без выгрузки его сразу в посте.
     */
    @Operation(summary = "Download a file", description = "Download a file by post ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        byte[]file = fileService.downloadFile(id);
        log.info("File %s loaded successfully");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .body(file);
    }
}

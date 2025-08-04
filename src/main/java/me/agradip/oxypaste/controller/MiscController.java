package me.agradip.oxypaste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.service.PasteService;
import me.agradip.oxypaste.service.UserService;
import me.agradip.oxypaste.util.VersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Misc", description = "Retrieve statistics")
public class MiscController {

    private final UserService userService;
    private final PasteService pasteService;
    private final VersionProvider versionProvider;

    @Autowired
    public MiscController(UserService userService, PasteService pasteService, VersionProvider versionProvider) {
        this.userService = userService;
        this.pasteService = pasteService;
        this.versionProvider = versionProvider;
    }

    @Operation(summary = "Get general application statistics")
    @GetMapping("/stats")
    public ResponsesDto.StatisticsResponse getStats() {
        return new ResponsesDto.StatisticsResponse(userService.countUsers(), pasteService.countPastes());
    }

    @Operation(summary = "Get the version of the backend")
    @GetMapping("/version")
    public ResponsesDto.VersionResponse getVersion() {
        return new ResponsesDto.VersionResponse(versionProvider.getVersion());
    }
}

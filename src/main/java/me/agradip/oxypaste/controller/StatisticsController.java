package me.agradip.oxypaste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.service.PasteService;
import me.agradip.oxypaste.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Misc", description = "Retrieve statistics")
public class StatisticsController {

    private final UserService userService;
    private final PasteService pasteService;

    @Autowired
    public StatisticsController(UserService userService, PasteService pasteService) {
        this.userService = userService;
        this.pasteService = pasteService;
    }

    @Operation(summary = "Get general application statistics")
    @GetMapping
    public ResponsesDto.StatisticsResponse getStats() {
        return new ResponsesDto.StatisticsResponse(userService.countUsers(), pasteService.countPastes());
    }
}

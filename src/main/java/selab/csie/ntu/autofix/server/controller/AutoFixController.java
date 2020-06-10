package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import selab.csie.ntu.autofix.server.exception.BadRequestException;
import selab.csie.ntu.autofix.server.model.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.CmakeAutoFixService;
import selab.csie.ntu.autofix.server.service.GradleAutoFixService;
import selab.csie.ntu.autofix.server.service.PipAutoFixService;

@RestController
@RequestMapping("/autofix")
public class AutoFixController {

    private CmakeAutoFixService cmakeAutoFixService;
    private GradleAutoFixService gradleAutoFixService;
    private PipAutoFixService pipAutoFixService;

    @Autowired
    public AutoFixController(
            CmakeAutoFixService cmakeAutoFixService,
            GradleAutoFixService gradleAutoFixService,
            PipAutoFixService pipAutoFixService
    ) {
        this.cmakeAutoFixService = cmakeAutoFixService;
        this.gradleAutoFixService = gradleAutoFixService;
        this.pipAutoFixService = pipAutoFixService;
    }

    @PostMapping(value = "/cmake", consumes = "application/json")
    public void invokeCmakeFix(@RequestBody AutoFixInvokeMessage message) {
        if ( message.getUrl() == null )
            throw new BadRequestException("Requires a valid URL.");
        FixingRecord record = cmakeAutoFixService.generateNewRecord(message.getUrl());
        cmakeAutoFixService.invokeAutoFix(message, record);
    }

    @PostMapping(value = "/gradle", consumes = "application/json")
    public void invokeGradleFix(@RequestBody AutoFixInvokeMessage message) {
        if ( message.getUrl() == null )
            throw new BadRequestException("Requires a valid URL.");
        FixingRecord record = gradleAutoFixService.generateNewRecord(message.getUrl());
        gradleAutoFixService.invokeAutoFix(message, record);
    }

    @PostMapping(value = "/pip", consumes = "application/json")
    public void invokePipFix(AutoFixInvokeMessage message) {
        if ( message.getUrl() == null )
            throw new BadRequestException("Requires a valid URL.");
        FixingRecord record = pipAutoFixService.generateNewRecord(message.getUrl());
        pipAutoFixService.invokeAutoFix(message, record);
    }

}

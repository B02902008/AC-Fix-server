package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import selab.csie.ntu.autofix.server.service.AutoFixService;
import selab.csie.ntu.autofix.server.service.exception.BadRequestException;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.service.CmakeAutoFixService;
import selab.csie.ntu.autofix.server.service.GradleAutoFixService;
import selab.csie.ntu.autofix.server.service.PipAutoFixService;

import java.util.Map;

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

    @PostMapping(value = "/{tool}", consumes = "application/json")
    public void invokeAutoFix(@PathVariable String tool, @RequestBody AutoFixInvokeMessage message) {
        if ( message.getUrl() == null )
            throw new BadRequestException("Requires a valid URL.");
        AutoFixService service = getServiceByTool(tool);
        if ( service == null )
            throw new BadRequestException("No matching Auto-Fix service.");
        service.invokeAutoFix(message, service.generateNewRecord(message.getUrl()));
    }

    @GetMapping(value = "/loading/{tool}", produces = "application/json")
    public Map<String, Integer> getAutoFixServiceLoading(@PathVariable String tool) {
        AutoFixService service = getServiceByTool(tool);
        if ( service == null )
            throw new BadRequestException("No matching Auto-Fix service.");
        return service.getLoading();
    }

    private AutoFixService getServiceByTool(String tool) {
        AutoFixService service;
        switch ( tool.toLowerCase() ) {
            case "cmake":
                service = this.cmakeAutoFixService;
                break;
            case "gradle":
                service = this.gradleAutoFixService;
                break;
            case "pip":
                service = this.pipAutoFixService;
                break;
            default:
                service = null;
                break;
        }
        return service;
    }

}

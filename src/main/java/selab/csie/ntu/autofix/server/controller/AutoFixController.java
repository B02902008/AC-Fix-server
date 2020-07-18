package selab.csie.ntu.autofix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import selab.csie.ntu.autofix.server.service.AutoFixService;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.service.CmakeAutoFixService;
import selab.csie.ntu.autofix.server.service.GradleAutoFixService;
import selab.csie.ntu.autofix.server.service.PipAutoFixService;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

@RestController
@CrossOrigin("*")
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

    @PostMapping(value = "/{tool}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Integer invokeAutoFix(@PathVariable String tool, @RequestBody AutoFixInvokeMessage message) {
        if ( message.getUrl() == null || message.getUrl().length() == 0 )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requires a valid URL.");
        AutoFixService service = getServiceByTool(tool);
        if ( service == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching Auto-Fix service.");
        try {
            return service.invokeAutoFix(message, service.generateNewRecord(message.getUrl()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requires a valid URL.");
        } catch (RejectedExecutionException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Auto-Fix service reached system load limit, please retry later.");
        }
    }

    @GetMapping(value = "/loading/{tool}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Integer> getAutoFixServiceLoading(@PathVariable String tool) {
        AutoFixService service = getServiceByTool(tool);
        if ( service == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching Auto-Fix service.");
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

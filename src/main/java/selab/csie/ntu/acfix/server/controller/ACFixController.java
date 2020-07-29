package selab.csie.ntu.acfix.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import selab.csie.ntu.acfix.server.service.ACFixService;
import selab.csie.ntu.acfix.server.model.message.ACFixInvokeMessage;
import selab.csie.ntu.acfix.server.service.CmakeACFixService;
import selab.csie.ntu.acfix.server.service.GradleACFixService;
import selab.csie.ntu.acfix.server.service.PipACFixService;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

@RestController
@CrossOrigin("*")
@RequestMapping("/acfix")
public class ACFixController {

    private CmakeACFixService cmakeService;
    private GradleACFixService gradleService;
    private PipACFixService pipService;

    @Autowired
    public ACFixController(
            CmakeACFixService cmakeService,
            GradleACFixService gradleService,
            PipACFixService pipService
    ) {
        this.cmakeService = cmakeService;
        this.gradleService = gradleService;
        this.pipService = pipService;
    }

    @PostMapping(value = "/{tool}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Integer invokeACFix(@PathVariable String tool, @RequestBody ACFixInvokeMessage message) {
        if (message.getUrl() == null || message.getUrl().length() == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requires a non-empty URL.");
        ACFixService service = getServiceByTool(tool);
        if (service == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching AC-Fix service.");
        try {
            return service.invokeACFix(message, service.generateNewRecord(message.getUrl()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requires a valid URL.");
        } catch (RejectedExecutionException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AC-Fix service reached system load limit, please retry later.");
        }
    }

    @GetMapping(value = "/loading/{tool}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Integer> getACFixServiceLoading(@PathVariable String tool) {
        ACFixService service = getServiceByTool(tool);
        if (service == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching AC-Fix service.");
        return service.getLoading();
    }

    private ACFixService getServiceByTool(String tool) {
        switch (tool.toLowerCase()) {
            case "cmake":
                return this.cmakeService;
            case "gradle":
                return this.gradleService;
            case "pip":
                return this.pipService;
            default:
                return null;
        }
    }

}

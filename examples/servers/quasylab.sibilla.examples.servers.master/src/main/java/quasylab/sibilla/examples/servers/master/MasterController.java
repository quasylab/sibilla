package quasylab.sibilla.examples.servers.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quasylab.sibilla.core.server.master.MasterState;

@RestController
@RequestMapping("/master")
public class MasterController {

    @Autowired
    MonitoringServerComponent monitoringServerComponent;

    @GetMapping("/state")
    public MasterState getMasterState() {
        return monitoringServerComponent.getMasterState();
    }
}

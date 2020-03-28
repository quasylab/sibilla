package quasylab.sibilla.core.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import quasylab.sibilla.core.simulator.newserver.MasterState;

import java.util.Map;

@Controller
public class MasterController {

    @Autowired
    MonitoringServerComponent monitoringServerComponent;

    @GetMapping("/")
    public Map<String, MasterState> getMasterState(){
        return monitoringServerComponent.getMasterEnvironment().getMonitoringServer().getStateMap();
    }

}

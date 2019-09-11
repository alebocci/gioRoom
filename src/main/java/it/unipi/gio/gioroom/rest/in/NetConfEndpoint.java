package it.unipi.gio.gioroom.rest.in;

import it.unipi.gio.gioroom.rest.out.ShutterDriver;
import it.unipi.gio.gioroom.rest.out.VaseDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conf")
public class NetConfEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(NetConfEndpoint.class);

    private VaseDriver vase;
    private ShutterDriver shutter;

    @Autowired
    public NetConfEndpoint(VaseDriver vase, ShutterDriver shutter) {
        this.vase=vase;
        this.shutter=shutter;
    }


    @RequestMapping(value="/vase/ip",method = RequestMethod.GET)
    public ResponseEntity getVaseIp() {
        LOG.info("Get vase ip request");
        return ResponseEntity.ok(vase.getIp());
    }

    @RequestMapping(value="/vase/port",method = RequestMethod.GET)
    public ResponseEntity getVasePort() {
        LOG.info("Get vase port request");
        return ResponseEntity.ok(vase.getPort());
    }

    @RequestMapping(value="/shutter/ip",method = RequestMethod.GET)
    public ResponseEntity getShutterIp() {
        LOG.info("Get shutter ip request");
        return ResponseEntity.ok(shutter.getIp());
    }

    @RequestMapping(value="/shutter/port",method = RequestMethod.GET)
    public ResponseEntity getShutterPort() {
        LOG.info("Get shutter port request");
        return ResponseEntity.ok(shutter.getIp());
    }

    @RequestMapping(value={"vase/ip","vase/port","vase/ipport"},method = RequestMethod.PUT)
    public ResponseEntity setVaseIpPort(@RequestBody BodyIpPort body) {
        LOG.info("New ip port request, ip: {} port: {}",body.ip,body.port);
        vase.setIpPort(body.ip,body.port);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value={"shutter/ip","shutter/port","shutter/ipport"},method = RequestMethod.PUT)
    public ResponseEntity setShutterIpPort(@RequestBody BodyIpPort body) {
        LOG.info("New ip port request, ip: {} port: {}",body.ip,body.port);
        shutter.setIpPort(body.ip,body.port);
        return ResponseEntity.ok().build();
    }

    public static class BodyIpPort{
        String ip;
        Integer port;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }
}

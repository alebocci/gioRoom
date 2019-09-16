package it.unipi.gio.gioroom;

import it.unipi.gio.gioroom.drivers.LightHourDriver;
import it.unipi.gio.gioroom.management.Planner;
import it.unipi.gio.gioroom.model.Goal;
import it.unipi.gio.gioroom.drivers.PresenceDriver;
import it.unipi.gio.gioroom.model.User;
import it.unipi.gio.gioroom.rest.out.ShutterDriver;
import it.unipi.gio.gioroom.rest.out.VaseDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class GioroomApplication {

	public static void main(String[] args) {
		SpringApplication.run(GioroomApplication.class, args);
	}

	@Bean
	public CopyOnWriteArrayList<Goal> goalList(){return new CopyOnWriteArrayList<>();}

	@Bean
	public ShutterDriver shutterDriver(Environment env, RestTemplate restTemplate){
		String ip = env.getProperty("shutter_driver.ip");
		Integer port = env.getProperty("shutter_driver.port",Integer.class);
		String serverPort = env.getProperty("server.port");
		if(port==null){
			port=8080;
		}

		ShutterDriver sh=null;
		try {
			String localIp = InetAddress.getLocalHost().getHostAddress();
			sh = new ShutterDriver(InetAddress.getByName(ip),port, restTemplate, localIp, serverPort);
		} catch (UnknownHostException e) {
			System.exit(-2);
		}
		return sh;
	}

	@Bean
	public VaseDriver vaseDriver(Environment env, RestTemplate restTemplate){
		String ip = env.getProperty("vase_driver.ip");
		Integer port = env.getProperty("vase_driver.port",Integer.class);
		String serverPort = env.getProperty("server.port");
		if(port==null){
			port=8090;
		}
		VaseDriver vase=null;
		try {
			String localIp = InetAddress.getLocalHost().getHostAddress();
			vase = new VaseDriver(InetAddress.getByName(ip),port, restTemplate,localIp,serverPort);
		} catch (UnknownHostException | IllegalArgumentException e) {
			System.exit(-3);
		}
		return vase;
	}

	@Bean
	public PresenceDriver presenceDriver(List<User> userList){return new PresenceDriver(userList);}

	@Bean
	public LightHourDriver lightHourDriver(RestTemplate restTemplate){return new LightHourDriver(restTemplate);}

	@Bean //TODO request users list to db
	public List<User> userList(){return Collections.synchronizedList(new ArrayList<>());}

	@Bean
	public Planner planner(ShutterDriver shutter, VaseDriver vase, PresenceDriver presence, LightHourDriver daytime){
		return new Planner(shutter, vase, presence,daytime);
	}

	@Bean
	public RestTemplate restTemplate(
			RestTemplateBuilder restTemplateBuilder) {

		return restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(2))
				.setReadTimeout(Duration.ofSeconds(2))
				.build();
	}
}

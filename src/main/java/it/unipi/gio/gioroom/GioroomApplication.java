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
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
	public ShutterDriver shutterDriver(Environment env){
		String ip = env.getProperty("shutter_driver.ip");
		Integer port = env.getProperty("shutter_driver.port",Integer.class);
		ShutterDriver sh=null;
		try {
			sh = new ShutterDriver(InetAddress.getByName(ip),port);
		} catch (UnknownHostException e) {
			System.exit(-2);
		}
		return sh;
	}

	@Bean
	public VaseDriver vaseDriver(Environment env){
		String ip = env.getProperty("vase_driver.ip");
		Integer port = env.getProperty("vase_driver.port",Integer.class);
		VaseDriver vase=null;
		try {
			vase = new VaseDriver(InetAddress.getByName(ip),port);
		} catch (UnknownHostException | IllegalArgumentException e) {
			System.exit(-3);
		}
		return vase;
	}

	@Bean
	public PresenceDriver presenceDriver(List<User> userList){return new PresenceDriver(userList);}

	@Bean
	public LightHourDriver lightHourDriver(){return new LightHourDriver();}

	@Bean //TODO request users list to db
	public List<User> userList(){return Collections.synchronizedList(new ArrayList<>());}

	@Bean
	public Planner planner(ShutterDriver shutter, VaseDriver vase, PresenceDriver presence, LightHourDriver daytime){
		return new Planner(shutter, vase, presence,daytime);
	}
}

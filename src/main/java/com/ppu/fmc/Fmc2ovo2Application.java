package com.ppu.fmc;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ppu.fmc.util.NetUtils;
import com.ppu.fmc.util.Utils;

@SpringBootApplication
@EnableScheduling
@PropertySources({
	@PropertySource("classpath:config.properties"),
})
public class Fmc2ovo2Application implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(Fmc2ovo2Application.class);
	public static final String POM_LOCATION = "/META-INF/maven/com.ppu.fmc/fmc2ovo2/pom.properties";

	public static long eventIdIncrementer = 0; // diisi dgn timestamp wkt service up
	
	@Value("${ovo.ws.url}")
	private String url;

	public static void main(String[] args) {
		new SpringApplicationBuilder(Fmc2ovo2Application.class).web(false).run(args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		eventIdIncrementer = System.currentTimeMillis();

		if (NetUtils.isSSL(url))
			NetUtils.trustSelfSignedSSL();
		
		displayStat();
		
		log.debug("ovo.ws.url :: {}", url);

		log.info("FMC to OVO ready");
	}
	
	private void displayStat() {
		StringBuilder sbAppInfo = new StringBuilder();
		
		try {
			Properties p = new Properties();
			InputStream is = Fmc2ovo2Application.class.getResourceAsStream(Fmc2ovo2Application.POM_LOCATION);
			if (is != null) {
				p.load(is);
				sbAppInfo.append("artifactId=").append(p.getProperty("artifactId", ""));
				
				sbAppInfo.append(", version=").append(p.getProperty("version", ""));
			}
			is.close();
			is = null;
		} catch (Exception e) {
			// ignore
		}

		Utils.printStat("(" + sbAppInfo.toString() + ")");
		
		log.info("...Waiting for next cycle...\n");
	}	
}

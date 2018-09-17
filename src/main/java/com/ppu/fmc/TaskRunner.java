package com.ppu.fmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ppu.fmc.handler.Job1;
import com.ppu.fmc.handler.Job2;

@Component
@PropertySources({
	@PropertySource("classpath:config.properties"),
})
public class TaskRunner {
	private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);
	
	@Value("${developer:false}")
	private boolean developer;
	
	@Value("${scheduler.disable:false}")
	private boolean disableJob;

	@Value("${fmc.schedule1.delay.seconds}")
	private long schedule1Seconds;
	
	@Value("${fmc.schedule2.delay.seconds}")
	private long schedule2Seconds;
	
	@Autowired
	Job1 job1;
	
	@Autowired
	Job2 job2;
	
	@Scheduled(initialDelay = 1000, fixedDelayString = "${fmc.schedule1.delay.seconds:20}000")
	public void runJob1() throws Exception{
		
		if (disableJob) {
			log.warn("Scheduled Job is currently disabled. Please run the program again or set scheduler.disable=false");
			System.exit(0);
			
			return;
		}
		
		log.debug("fmc.schedule1.delay.seconds :: {}", schedule1Seconds);
		
		try {
			boolean result = job1.execute();
			log.info(Job1.class.getSimpleName() + " return " + result + "\n");
//		}catch(SQLGrammerException e) {
//			log.error(e.getMessage(), e);
//			System.exit(0);
//			throw e;
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	@Scheduled(initialDelay = 500, fixedDelayString = "${fmc.schedule2.delay.seconds:25}000")
	public void runJob2() throws Exception{
		
		if (disableJob) {
			log.warn("Scheduled Job is currently disabled. Please run the program again or set scheduler.disable=false");
			System.exit(0);
			
			return;
		}
		
		log.debug("fmc.schedule2.delay.seconds :: {}", schedule2Seconds);
		
		try {
			boolean result = job2.execute();
			log.info(Job2.class.getSimpleName() + " return " + result + "\n");
//		}catch(SQLGrammerException e) {
//			log.error(e.getMessage(), e);
//			System.exit(0);
//			throw e;
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}

}

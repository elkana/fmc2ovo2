package com.ppu.fmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ppu.fmc.handler.Job1;
import com.ppu.fmc.handler.Job2;
import com.ppu.fmc.handler.Job3;

@Component
//@PropertySources({
//	@PropertySource("classpath:config.properties"),
//})
public class TaskRunner {
	private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);
	
	@Value("${developer:false}")
	private boolean developer;
	
	@Value("${fmc.job1.disable:false}")
	private boolean disableJob1;

	@Value("${fmc.job2.disable:false}")
	private boolean disableJob2;

	@Value("${fmc.job3.disable:false}")
	private boolean disableJob3;

	@Value("${fmc.job1.delay.seconds}")
	private long job1Seconds;
	
	@Value("${fmc.job2.delay.seconds}")
	private long job2Seconds;
	
	@Value("${fmc.job3.delay.seconds}")
	private long job3Seconds;

	@Autowired
	Job1 job1;
	
	@Autowired
	Job2 job2;
	
	@Autowired
	Job3 job3;
	
	@Scheduled(initialDelay = 1000, fixedDelayString = "${fmc.job1.delay.seconds:20}000")
	public void runJob1() throws Exception{
		
		log.debug("fmc.job1.disable :: {}", disableJob1);

		if (disableJob1) {
			log.warn("Scheduled {} is currently disabled.", Job1.class.getSimpleName());
			return;
		}
		
		log.debug("fmc.job1.delay.seconds :: {}", job1Seconds);
		
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
	
	@Scheduled(initialDelay = 500, fixedDelayString = "${fmc.job2.delay.seconds:25}000")
	public void runJob2() throws Exception{
		
		log.debug("fmc.job2.disable :: {}", disableJob2);

		if (disableJob2) {
			log.warn("Scheduled {} is currently disabled.", Job2.class.getSimpleName());
			return;
		}
		
		log.debug("fmc.job2.delay.seconds :: {}", job2Seconds);
		
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

	@Scheduled(initialDelay = 500, fixedDelayString = "${fmc.job3.delay.seconds:10}000")
	public void runJob3() throws Exception{
		
		log.debug("fmc.job3.disable :: {}", disableJob3);

		if (disableJob3) {
			log.warn("Scheduled {} is currently disabled.", Job3.class.getSimpleName());
			return;
		}
		
		log.debug("fmc.job3.delay.seconds :: {}", job3Seconds);
		
		try {
			boolean result = job3.execute();
			log.info(Job3.class.getSimpleName() + " return " + result + "\n");
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

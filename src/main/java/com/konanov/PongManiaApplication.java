package com.konanov;

import com.konanov.model.league.PublicLeague;
import com.konanov.model.league.PublicLeagueType;
import com.konanov.repository.PublicLeagueRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PongManiaApplication {

	private final PublicLeagueRepository leagueRepository;

	@Autowired
	public PongManiaApplication(PublicLeagueRepository leagueRepository) {
		this.leagueRepository = leagueRepository;
	}

	@Bean
	CommandLineRunner preLoadMongo() {
		return args -> {
			/*leagueRepository.insert(new PublicLeague(new ObjectId(), PublicLeagueType.JUNIOR))
					.doOnNext(league -> System.out.println(league.getType() + " league created")).block();
			leagueRepository.insert(new PublicLeague(new ObjectId(), PublicLeagueType.MIDDLE))
					.doOnNext(league -> System.out.println(league.getType() + " league created")).block();
			leagueRepository.insert(new PublicLeague(new ObjectId(), PublicLeagueType.PRO))
					.doOnNext(league -> System.out.println(league.getType() + " league created")).block();*/
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(PongManiaApplication.class, args);
	}
}

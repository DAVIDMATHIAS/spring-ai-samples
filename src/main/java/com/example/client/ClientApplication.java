package com.example.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class ClientApplication {
	static final String FOLDER = "/home/davidmathias/test/user-context";
	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

}

@Configuration
class LocalToolsAutoconfiguration {

	@Component
	static class Tools{

		@Tool(description = "returns all the files in the "+ClientApplication.FOLDER+" folder")
		String[] listFiles() {
			System.out.println("Listing files in " + ClientApplication.FOLDER);
			return new java.io.File(ClientApplication.FOLDER).list();
		}

	}
	@Bean
	NamedMCPClientRunner namedMCPClientRunner(ChatClient.Builder builder, Tools tools) {
		return new NamedMCPClientRunner(builder.defaultTools(tools));
	}

}

class NamedMCPClientRunner  implements ApplicationRunner, BeanNameAware {

	private final AtomicReference<String> beanName = new AtomicReference<>();

	private final ChatClient.Builder builder;

    NamedMCPClientRunner(ChatClient.Builder builder) {
        this.builder = builder;
    }

    @Override
	public void setBeanName(String name) {
		this.beanName.set(name);

	}


	@Override
	public void run(ApplicationArguments args) throws Exception {

		var prompt = """
				What files are in the user-context folder, and of those files, 
				which have a name that corresponds to a chinese greeting?
				""";

		var response = this.builder.build().prompt(prompt).call().entity(ChineseFile.class);

		System.out.println("Response: " + response);
	}
}

record ChineseFile(String fileName, String wording) {
	@Override
	public String toString() {
		return "ChineseFile{" +
				"fileName='" + fileName + '\'' +
				", wording='" + wording + '\'' +
				'}';
	}
}
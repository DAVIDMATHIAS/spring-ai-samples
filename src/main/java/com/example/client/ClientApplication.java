package com.example.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class ClientApplication {
	static final String FOLDER = "/home/davidmathias/work/mcp/mcp-client-demo-01/user-context";
	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

}

@Configuration
class ThirdPartyConfiguration{
	@Bean
	NamedMCPClientRunner namedMCPClientRunner(ChatClient.Builder builder, McpSyncClient mcpSyncClient) {
		var tools = new SyncMcpToolCallbackProvider(mcpSyncClient);
		return new NamedMCPClientRunner(builder.defaultTools(tools));
	}
	@Bean
	McpSyncClient mcpSyncClient(@Value(ClientApplication.FOLDER) File root) {
		ServerParameters serverParameters = ServerParameters
				.builder("npx")
				.args("-y", "@modelcontextprotocol/server-filesystem", root.getAbsolutePath())
				.build();
		McpSyncClient mcp = McpClient.sync(new StdioClientTransport(serverParameters)).build();
		mcp.initialize();
		return mcp;
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
package com.example.client;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class ClientApplication {
	static final String FOLDER = "/home/davidmathias/work/mcp/mcp-client-demo-01/user-context";
	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

}

@Configuration
class ServerMCPClientConfiguration {

	@Bean
	McpSyncClient mcpClient(@Value("${mcp.servers.server1.url}") String url) {
	    McpSyncClient	mcp = McpClient.sync(new HttpClientSseClientTransport(url)).build();
		mcp.initialize();
		return mcp;
	}
	@Bean
	NamedMCPClientRunner namedMCPClientRunner(ChatClient.Builder builder, McpSyncClient mcpClient) {
		ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(mcpClient);

		return new NamedMCPClientRunner(builder.defaultTools(provider));
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
				Can you give me the name and gpa of students who got above 3 gpa?
				""";

		var response = this.builder.build().prompt(prompt).call().entity(Student.class);

		System.out.println("Response: " + response);
	}
}

record Student(String name, int  gpa) {
	@Override
	public String toString() {
		return "Student{" +
				"Name='" + name + '\'' +
				", gpa='" + gpa + '\'' +
				'}';
	}
}
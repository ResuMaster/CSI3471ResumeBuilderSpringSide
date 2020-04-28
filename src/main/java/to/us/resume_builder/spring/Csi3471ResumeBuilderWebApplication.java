package to.us.resume_builder.spring;

import java.io.InputStreamReader;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Csi3471ResumeBuilderWebApplication {
    private static final String SHUTDOWN_CODE = "5349";

    /**
     * Starts the server
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Csi3471ResumeBuilderWebApplication.class, args);
    }

    /**
     * Handle to set up the program to be killable
     *
     * @param ctx The context of the application, needed to kill the process
     *
     * @return A process to run on the command line (?)
     */
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            awaitKill(ctx);
        };
    }

    private static boolean keepAlive = true;

    /**
     * Provides a handle to end the application. Currently requires input from
     * the console it was started on.
     *
     * @param ctx The context of the application to kill
     */
    private static void awaitKill(ApplicationContext ctx) {
        Scanner s = new Scanner(new InputStreamReader(System.in));
        System.out.printf("Running application %1$s%n", Csi3471ResumeBuilderWebApplication.class.getName());

        // Block waiting for the server to be shut down
        String inputLine;
        while (keepAlive || ((inputLine = s.nextLine()) != null && !s.nextLine().equals(SHUTDOWN_CODE))) {
            System.out.println("WRONG!");
        }
        SpringApplication.exit(ctx, () -> 0);
    }
}

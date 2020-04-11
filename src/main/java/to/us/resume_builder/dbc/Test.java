package to.us.resume_builder.dbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import to.us.resume_builder.dbc.resume_user.ResumeUserDBC;

public class Test {
    private static final String QUIT = "quit";

    public static void main(String[] args) throws IOException, InterruptedException {
        ResumeUserDBC dbc = new ResumeUserDBC();

        // Handle input
        @SuppressWarnings("resource")
        Scanner s = new Scanner(new InputStreamReader(System.in));
        String line;
        while (true) {
            System.out.println("Enter a command:");
            line = s.nextLine();

            if (line.equalsIgnoreCase("GET")) {
                line = s.nextLine();
                System.out.println(dbc.hasUser(line));
            } else if (line.equalsIgnoreCase("PUT")) {
                line = s.nextLine();
                System.out.println(dbc.addUser(line));
            } else if (line.equalsIgnoreCase(QUIT))
                break;
        }
    }
}

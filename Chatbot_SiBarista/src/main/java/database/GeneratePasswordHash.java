package database;

import org.mindrot.jbcrypt.BCrypt;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        String passwordAsli = "admin123";
        String hash = BCrypt.hashpw(passwordAsli, BCrypt.gensalt());

        System.out.println(hash);
    }
}